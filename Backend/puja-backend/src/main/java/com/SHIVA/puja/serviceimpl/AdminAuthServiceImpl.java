package com.SHIVA.puja.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SHIVA.puja.dto.AdminLoginChallengeRequest;
import com.SHIVA.puja.dto.AdminLoginChallengeResponse;
import com.SHIVA.puja.dto.AdminLoginVerifyRequest;
import com.SHIVA.puja.dto.AuthTokenResponse;
import com.SHIVA.puja.entity.OtpPurpose;
import com.SHIVA.puja.entity.OtpRequest;
import com.SHIVA.puja.entity.RefreshToken;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.OtpRequestRepository;
import com.SHIVA.puja.repository.RefreshTokenRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.security.JwtService;
import com.SHIVA.puja.service.AdminAuthService;
import com.SHIVA.puja.service.RedisRateLimiterService;

@Service
@Transactional
public class AdminAuthServiceImpl implements AdminAuthService {

    private final UserRepository userRepository;
    private final OtpRequestRepository otpRequestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final RedisRateLimiterService redisRateLimiterService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.security.jwt-expiration-minutes:15}")
    private long jwtExpirationMinutes;

    @Value("${app.security.refresh-expiration-days:30}")
    private long refreshExpirationDays;

    @Value("${app.security.admin-mfa-otp-expiration-minutes:5}")
    private long adminOtpExpirationMinutes;

    @Value("${app.security.admin-login-attempts:5}")
    private int adminLoginAttempts;

    @Value("${app.security.admin-login-window-seconds:600}")
    private int adminLoginWindowSeconds;

    @Value("${app.security.admin-allowed-ips:}")
    private String globalAdminAllowedIps;

    public AdminAuthServiceImpl(UserRepository userRepository,
            OtpRequestRepository otpRequestRepository,
            RefreshTokenRepository refreshTokenRepository,
            JwtService jwtService,
            JavaMailSender mailSender,
            RedisRateLimiterService redisRateLimiterService) {
        this.userRepository = userRepository;
        this.otpRequestRepository = otpRequestRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.mailSender = mailSender;
        this.redisRateLimiterService = redisRateLimiterService;
    }

    @Override
    public AdminLoginChallengeResponse startChallenge(AdminLoginChallengeRequest request, String clientIp) {
        String email = normalize(request.getContact());
        String password = normalize(request.getPassword());
        if (email == null || password == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Email and password are required.");
        }

        String limiterKey = "admin-login:" + email.toLowerCase() + ":" + sanitize(clientIp);
        boolean allowed = redisRateLimiterService.allowRequest(limiterKey, adminLoginAttempts, adminLoginWindowSeconds);
        if (!allowed) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "ADMIN_LOGIN_RATE_LIMITED",
                    "Too many admin login attempts. Please try again later.");
        }

        User user = userRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "Invalid email or password."));

        String role = normalizeRole(user.getRole());
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_ONLY", "Admin privileges are required.");
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "Invalid email or password.");
        }

        if (!isIpAllowed(user, clientIp)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_IP_RESTRICTED", "Admin login is not allowed from this IP.");
        }

        issueAdminOtp(user);

        String challengeToken = jwtService.generateScopedToken(
                user.getEmail(),
                "ADMIN_LOGIN_CHALLENGE",
                adminOtpExpirationMinutes,
                Map.of("ip", sanitize(clientIp), "nonce", UUID.randomUUID().toString(), "role", role));

        return AdminLoginChallengeResponse.builder()
                .mfaRequired(true)
                .challengeToken(challengeToken)
                .expiresInMinutes(adminOtpExpirationMinutes)
                .message("OTP sent to admin email")
                .build();
    }

    @Override
    public AuthTokenResponse verifyChallenge(AdminLoginVerifyRequest request, String clientIp) {
        String challengeToken = normalize(request.getChallengeToken());
        String otp = normalize(request.getOtp());

        if (challengeToken == null || otp == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Challenge token and OTP are required.");
        }

        String email = jwtService.extractUsername(challengeToken);
        if (email == null || !jwtService.isScopedTokenValid(challengeToken, "ADMIN_LOGIN_CHALLENGE", email)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_ADMIN_CHALLENGE", "Admin challenge is invalid or expired.");
        }

        String expectedIp = jwtService.extractClaim(challengeToken, claims -> claims.get("ip", String.class));
        if (expectedIp != null && !expectedIp.equals(sanitize(clientIp))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_IP_CHANGED", "Admin challenge IP mismatch.");
        }

        User user = userRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Admin user not found."));

        OtpRequest otpRequest = otpRequestRepository.findTopByEmailAndPurposeAndVerifiedFalseOrderByIdDesc(email, OtpPurpose.LOGIN)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP_NOT_FOUND", "Admin OTP not found."));

        if (otpRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "Admin OTP has expired.");
        }

        if (!hashValue(otp).equals(otpRequest.getOtpHash())) {
            otpRequest.setAttempts((otpRequest.getAttempts() == null ? 0 : otpRequest.getAttempts()) + 1);
            otpRequestRepository.save(otpRequest);
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid admin OTP.");
        }

        otpRequest.setVerified(true);
        otpRequestRepository.save(otpRequest);

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return issueAuthTokens(user, "Admin sign-in successful");
    }

    private void issueAdminOtp(User user) {
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        LocalDateTime now = LocalDateTime.now();

        OtpRequest request = new OtpRequest();
        request.setEmail(user.getEmail());
        request.setPhoneNumber(null);
        request.setOtpHash(hashValue(otp));
        request.setPurpose(OtpPurpose.LOGIN);
        request.setExpiresAt(now.plusMinutes(adminOtpExpirationMinutes));
        request.setVerified(false);
        request.setAttempts(0);
        request.setCreatedAt(now);
        otpRequestRepository.save(request);

        SimpleMailMessage mail = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            mail.setFrom(fromEmail);
        }
        mail.setTo(user.getEmail());
        mail.setSubject("Admin Sign-In OTP");
        mail.setText("Your admin login OTP is " + otp + ". It expires in " + adminOtpExpirationMinutes + " minutes.");
        try {
            mailSender.send(mail);
        } catch (MailException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "OTP_EMAIL_SEND_FAILED", "Failed to send admin OTP.");
        }
    }

    private AuthTokenResponse issueAuthTokens(User user, String message) {
        revokeActiveRefreshTokens(user.getId());

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                        .password(user.getPasswordHash() == null ? "" : user.getPasswordHash())
                        .authorities("ROLE_" + normalizeRole(user.getRole()))
                        .build(),
                Map.of("role", normalizeRole(user.getRole())));

        String refreshTokenValue = UUID.randomUUID() + "." + System.currentTimeMillis();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hashValue(refreshTokenValue));
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plus(refreshExpirationDays, ChronoUnit.DAYS));
        refreshToken.setRevokedAt(null);
        refreshTokenRepository.save(refreshToken);

        return AuthTokenResponse.builder()
                .token(token)
                .refreshToken(refreshTokenValue)
                .email(user.getEmail())
                .role(normalizeRole(user.getRole()))
                .expiresInMinutes(jwtExpirationMinutes)
                .refreshExpiresInDays(refreshExpirationDays)
                .message(message)
                .build();
    }

    private void revokeActiveRefreshTokens(Long userId) {
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId).forEach(token -> {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
        });
    }

    private boolean isIpAllowed(User user, String clientIp) {
        String configured = normalize(user.getAdminAllowedIps());
        if (configured == null) {
            configured = normalize(globalAdminAllowedIps);
        }
        if (configured == null) {
            return true;
        }

        String candidateIp = sanitize(clientIp);
        for (String allowed : configured.split(",")) {
            if (candidateIp.equals(allowed.trim())) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeRole(String role) {
        return role == null ? "CUSTOMER" : role.trim().toUpperCase();
    }

    private String sanitize(String value) {
        String normalized = normalize(value);
        return normalized == null ? "unknown" : normalized;
    }

    private String hashValue(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "HASHING_FAILED", "Failed to process secure value.");
        }
    }
}
