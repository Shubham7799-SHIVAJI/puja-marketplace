package com.SHIVA.puja.serviceimpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import com.SHIVA.puja.dto.LoginRequest;
import com.SHIVA.puja.dto.ResendOtpRequest;
import com.SHIVA.puja.dto.SetPasswordRequest;
import com.SHIVA.puja.dto.SignInRequest;
import com.SHIVA.puja.dto.VerifyOtpRequest;
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
import com.SHIVA.puja.service.RedisTokenBlacklistService;
import com.SHIVA.puja.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class UserServiceImpl implements UserService {

    private static final int OTP_BOUND = 900000;
    private static final int OTP_OFFSET = 100000;

    private final UserRepository userRepository;
    private final OtpRequestRepository otpRequestRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JavaMailSender mailSender;
    private final JwtService jwtService;
    private final RedisTokenBlacklistService redisTokenBlacklistService;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.otp.expiration-minutes:5}")
    private long otpExpirationMinutes;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.logo-path:src/main/java/com/SHIVA/puja/images/LORD-NATRAJ.png}")
    private String logoPath;

    @Value("${app.mail.subject:Puja Marketplace Login OTP}")
    private String otpMailSubject;

    @Value("${app.security.jwt-expiration-minutes:15}")
    private long jwtExpirationMinutes;

    @Value("${app.security.refresh-expiration-days:30}")
    private long refreshExpirationDays;

    @Value("${app.security.password-reset-token-expiration-minutes:10}")
    private long passwordResetTokenExpirationMinutes;

    public UserServiceImpl(
            UserRepository userRepository,
            OtpRequestRepository otpRequestRepository,
            RefreshTokenRepository refreshTokenRepository,
            JavaMailSender mailSender,
                JwtService jwtService,
                RedisTokenBlacklistService redisTokenBlacklistService
    ) {
        this.userRepository = userRepository;
        this.otpRequestRepository = otpRequestRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.mailSender = mailSender;
        this.jwtService = jwtService;
        this.redisTokenBlacklistService = redisTokenBlacklistService;
    }

    @Override
    @Transactional
    public void loginUser(LoginRequest request) {
        String name = normalize(request.getName());
        String email = normalize(request.getContact());
        LocalDateTime now = LocalDateTime.now();

        if (name == null || email == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Name and email are required.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "EMAIL_ALREADY_REGISTERED");
        }

        User user = new User();

        user.setFullName(name);
        user.setEmail(email);
        user.setPhoneNumber(null);
        user.setRole("CUSTOMER");
        user.setStatus("PENDING");
        user.setPhoneVerified(false);
        user.setEmailVerified(false);

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(now);
        }
        user.setUpdatedAt(now);

        User savedUser = userRepository.save(user);
        issueOtp(savedUser.getEmail(), savedUser.getFullName());
    }

    @Override
    @Transactional
    public void resendEmailOtp(ResendOtpRequest request) {
        String email = normalize(request.getContact());

        User user = userRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found for this email."));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMAIL_ALREADY_VERIFIED", "Email is already verified.");
        }

        user.setStatus("PENDING");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        issueOtp(user.getEmail(), user.getFullName());
    }

    @Override
    @Transactional
    public String verifyEmailOtp(VerifyOtpRequest request) {
        String email = normalize(request.getContact());
        String otp = normalize(request.getOtp());

        if (email == null || otp == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Email and OTP are required.");
        }

        User user = userRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found for this email."));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            return issuePasswordResetToken(user.getEmail());
        }

        OtpRequest otpRequest = otpRequestRepository
                .findTopByEmailAndPurposeAndVerifiedFalseOrderByIdDesc(email, OtpPurpose.REGISTER)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP_NOT_FOUND", "No OTP has been generated for this email."));

        if (otpRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP_EXPIRED", "OTP has expired. Please request a new OTP.");
        }

        if (!matchesOtp(otp, otpRequest.getOtpHash())) {
            otpRequest.setAttempts(otpRequest.getAttempts() + 1);
            otpRequestRepository.save(otpRequest);
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_OTP", "Invalid OTP.");
        }

        otpRequest.setVerified(true);
        otpRequestRepository.save(otpRequest);

        user.setEmailVerified(true);
        user.setStatus("ACTIVE");
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return issuePasswordResetToken(user.getEmail());
    }

    @Override
    @Transactional
    public void setPassword(SetPasswordRequest request) {
        String email = normalize(request.getContact());
        String password = normalize(request.getPassword());
        String confirmPassword = normalize(request.getConfirmPassword());
        String resetToken = normalize(request.getResetToken());

        if (email == null || password == null || confirmPassword == null || resetToken == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Email and password details are required.");
        }

        if (!jwtService.isScopedTokenValid(resetToken, "PASSWORD_RESET", email)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_RESET_TOKEN", "Password reset session is invalid or expired. Please verify OTP again.");
        }

        if (!password.equals(confirmPassword)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_MISMATCH", "Password and re-enter password do not match.");
        }

        User user = userRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found for this email."));

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMAIL_NOT_VERIFIED", "Please verify OTP before setting password.");
        }

        user.setPasswordHash(passwordEncoder.encode(password));
        user.setStatus("ACTIVE");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private String issuePasswordResetToken(String email) {
        return jwtService.generateScopedToken(
                email,
                "PASSWORD_RESET",
                passwordResetTokenExpirationMinutes,
                java.util.Map.of("purpose", "SET_PASSWORD"));
    }

    @Override
    @Transactional
    public AuthTokenResponse signIn(SignInRequest request) {
        String email = normalize(request.getContact());
        String password = normalize(request.getPassword());

        if (email == null || password == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Email and password are required.");
        }

        User user = userRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "Invalid email or password."));

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PASSWORD_NOT_SET", "Please set your password after OTP verification.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_CREDENTIALS", "Invalid email or password.");
        }

        String role = user.getRole() == null ? "" : user.getRole().trim().toUpperCase();
        if ("ADMIN".equals(role) || "SUPER_ADMIN".equals(role)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "ADMIN_2FA_REQUIRED",
                    "Use admin challenge endpoint to complete two-factor authentication.");
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return issueAuthTokens(user, "Sign-in successful");
    }

    @Override
    @Transactional
    public AuthTokenResponse refreshAccessToken(String refreshToken) {
        String normalizedToken = normalize(refreshToken);
        if (normalizedToken == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Refresh token is required.");
        }

        RefreshToken persistedToken = refreshTokenRepository.findByTokenHash(hashValue(normalizedToken, "REFRESH_TOKEN_HASHING_FAILED", "Failed to process refresh token."))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is invalid."));

        if (persistedToken.getRevokedAt() != null || persistedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "Refresh token is expired or revoked.");
        }

        User user = userRepository.findById(persistedToken.getUserId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "User not found for refresh token."));

        persistedToken.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(persistedToken);
        return issueAuthTokens(user, "Token refreshed successfully");
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String refreshToken) {
        String normalizedToken = normalize(refreshToken);
        if (normalizedToken == null) {
            return;
        }

        refreshTokenRepository.findByTokenHash(hashValue(normalizedToken, "REFRESH_TOKEN_HASHING_FAILED", "Failed to process refresh token."))
                .ifPresent(token -> {
                    token.setRevokedAt(LocalDateTime.now());
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public void blacklistAccessToken(String token) {
        String normalizedToken = normalize(token);
        if (normalizedToken == null) {
            return;
        }

        String tokenId = jwtService.extractTokenId(normalizedToken);
        long ttlSeconds = jwtService.remainingTtlSeconds(normalizedToken);
        if (tokenId != null && ttlSeconds > 0) {
            redisTokenBlacklistService.blacklistTokenId(tokenId, ttlSeconds);
        }
    }

    private AuthTokenResponse issueAuthTokens(User user, String message) {
        revokeActiveRefreshTokens(user.getId());

        String token = jwtService.generateToken(
            org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole())
                .build(),
            java.util.Map.of("role", user.getRole()));

        String refreshTokenValue = UUID.randomUUID() + "." + generateOtp() + '.' + System.currentTimeMillis();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hashValue(refreshTokenValue, "REFRESH_TOKEN_HASHING_FAILED", "Failed to process refresh token."));
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshToken.setExpiresAt(LocalDateTime.now().plus(refreshExpirationDays, ChronoUnit.DAYS));
        refreshToken.setRevokedAt(null);
        refreshTokenRepository.save(refreshToken);

        return AuthTokenResponse.builder()
            .token(token)
            .refreshToken(refreshTokenValue)
            .email(user.getEmail())
            .role(user.getRole())
            .expiresInMinutes(jwtExpirationMinutes)
            .refreshExpiresInDays(refreshExpirationDays)
            .message(message)
            .build();
    }

    private void revokeActiveRefreshTokens(Long userId) {
        refreshTokenRepository.findByUserIdAndRevokedAtIsNull(userId).forEach(token -> {
            if (token.getRevokedAt() == null) {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private void issueOtp(String recipientEmail, String fullName) {
        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail(recipientEmail);
        otpRequest.setOtpHash(hashOtp(otp));
        otpRequest.setPurpose(OtpPurpose.REGISTER);
        otpRequest.setExpiresAt(now.plusMinutes(otpExpirationMinutes));
        otpRequest.setVerified(false);
        otpRequest.setAttempts(0);
        otpRequest.setCreatedAt(now);
        otpRequestRepository.save(otpRequest);

        sendOtpEmail(recipientEmail, fullName, otp);
    }

    private void sendOtpEmail(String recipientEmail, String fullName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(recipientEmail);
            helper.setSubject(otpMailSubject);

            Path imageAbsolutePath = Path.of(logoPath).toAbsolutePath().normalize();
            boolean hasLogo = Files.exists(imageAbsolutePath);
            String htmlBody = buildEmailBody(fullName, otp, hasLogo);
            helper.setText(htmlBody, true);

            if (hasLogo) {
                helper.addInline("brandLogo", new FileSystemResource(imageAbsolutePath));
            }

            mailSender.send(message);
        } catch (MailException | MessagingException exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OTP_EMAIL_SEND_FAILED",
                    "Failed to send OTP email. Please check SMTP configuration."
            );
        }
    }

    private String buildEmailBody(String fullName, String otp, boolean hasLogo) {
        String recipientName = fullName == null || fullName.isBlank() ? "User" : fullName;

        String logoSection = hasLogo
                ? "<div style='text-align:center;margin-bottom:20px;'><img src='cid:brandLogo' alt='Logo' style='max-width:180px;height:auto;display:inline-block;'/></div>"
                : "";

        return "<div style='font-family:Segoe UI,Arial,sans-serif;color:#1b1b1b;max-width:560px;margin:0 auto;padding:24px;border:1px solid #ececec;border-radius:10px;background:#ffffff;'>"
                + logoSection
                + "<p style='margin:0 0 12px;font-size:16px;'>Namaste \uD83D\uDE4F " + escapeHtml(recipientName) + ",</p>"
                + "<p style='margin:0 0 16px;font-size:15px;line-height:1.6;'>Use the following One Time Password (OTP) to verify your login on Puja Marketplace.</p>"
                + "<div style='text-align:center;margin:18px 0;'>"
                + "<span style='display:inline-block;letter-spacing:4px;font-size:30px;font-weight:700;padding:10px 18px;border-radius:8px;background:#f5f5f5;border:1px solid #dcdcdc;'>"
                + escapeHtml(otp)
                + "</span></div>"
                + "<p style='margin:16px 0;font-size:14px;line-height:1.6;'>This OTP is valid for " + otpExpirationMinutes + " minutes. Please do not share it with anyone.</p>"
                + "<p style='margin:20px 0 4px;font-size:14px;'>Strength \u2022 Dharma \u2022 Truth</p>"
                + "<p style='margin:0 0 16px;font-size:15px;font-weight:600;'>Har Har Mahadev \uD83D\uDD31</p>"
                + "<p style='margin:0;font-size:14px;'>\u2014 Team Puja Marketplace</p>"
                + "</div>";
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String generateOtp() {
        return String.valueOf(secureRandom.nextInt(OTP_BOUND) + OTP_OFFSET);
    }

    private String hashOtp(String otp) {
        return hashValue(otp, "OTP_HASHING_FAILED", "Failed to process OTP.");
    }

    private boolean matchesOtp(String otp, String otpHash) {
        return hashOtp(otp).equals(otpHash);
    }

    private String hashValue(String value, String code, String message) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, code, message);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmedValue = value.trim();
        return trimmedValue.isEmpty() ? null : trimmedValue;
    }
}