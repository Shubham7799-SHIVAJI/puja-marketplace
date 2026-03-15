package com.SHIVA.puja.serviceimpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.SHIVA.puja.dto.ShopFileUploadResponse;
import com.SHIVA.puja.dto.ShopOtpResponse;
import com.SHIVA.puja.dto.ShopPhoneVerificationConfirmRequest;
import com.SHIVA.puja.dto.ShopOtpSendRequest;
import com.SHIVA.puja.dto.ShopOtpVerifyRequest;
import com.SHIVA.puja.dto.ShopRegistrationRequest;
import com.SHIVA.puja.dto.ShopRegistrationResponse;
import com.SHIVA.puja.entity.OtpPurpose;
import com.SHIVA.puja.entity.OtpRequest;
import com.SHIVA.puja.entity.ShopRegistration;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.exception.RequestValidationException;
import com.SHIVA.puja.repository.OtpRequestRepository;
import com.SHIVA.puja.repository.ShopRegistrationRepository;
import com.SHIVA.puja.service.ShopRegistrationService;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@Transactional
public class ShopRegistrationServiceImpl implements ShopRegistrationService {

    private static final int OTP_BOUND = 900000;
    private static final int OTP_OFFSET = 100000;
    private static final long OTP_EXPIRY_MINUTES = 5L;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9][0-9]{9}$");
    private static final Pattern OTP_PATTERN = Pattern.compile("^[0-9]{6}$");
    private static final Pattern AADHAR_PATTERN = Pattern.compile("^[0-9]{12}$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");
    private static final Pattern GST_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[A-Z0-9]{3}$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[0-9]{6}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[0-9]{9,18}$");
    private static final Pattern IFSC_PATTERN = Pattern.compile("^[A-Z]{4}0[A-Z0-9]{6}$");
    private static final Pattern UPI_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{2,256}@[A-Za-z]{2,64}$");
    private static final Set<String> ALLOWED_UPLOAD_FIELDS = Set.of(
            "profilePhoto",
            "ownerAadharPhoto",
            "ownerPanPhoto",
            "ownerSelfieWithId",
            "gstCertificateUpload",
            "cancelledChequePhoto");
        private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp");
        private static final Set<String> DOCUMENT_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        private static final Set<String> PHOTO_UPLOAD_FIELDS = Set.of(
            "profilePhoto",
            "ownerAadharPhoto",
            "ownerPanPhoto",
            "ownerSelfieWithId",
            "cancelledChequePhoto");
    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    private final ShopRegistrationRepository shopRegistrationRepository;
    private final OtpRequestRepository otpRequestRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Path uploadDirectory = Paths.get("uploads", "shop-registration");

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.logo-path:src/main/java/com/SHIVA/puja/images/LORD-NATRAJ.png}")
    private String logoPath;

    @Value("${app.mail.subject:Puja Marketplace Login OTP}")
    private String otpMailSubject;

    @Value("${app.twilio.account-sid:}")
    private String twilioAccountSid;

    @Value("${app.twilio.auth-token:}")
    private String twilioAuthToken;

    @Value("${app.twilio.from-number:}")
    private String twilioFromNumber;

    public ShopRegistrationServiceImpl(
            ShopRegistrationRepository shopRegistrationRepository,
            OtpRequestRepository otpRequestRepository,
            JavaMailSender mailSender) {
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.otpRequestRepository = otpRequestRepository;
        this.mailSender = mailSender;
    }

    @Override
    public ShopRegistrationResponse saveDraft(ShopRegistrationRequest request) {
        ShopRegistration registration = findOrCreateDraft(request.getRegistrationId());
        applyRequestToEntity(registration, request, false);
        registration.setStatus("DRAFT");
        registration.setUpdatedAt(LocalDateTime.now());

        if (registration.getCreatedAt() == null) {
            registration.setCreatedAt(LocalDateTime.now());
        }

        ShopRegistration savedRegistration = shopRegistrationRepository.save(registration);
        return mapToResponse(savedRegistration);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopRegistrationResponse getDraft(String registrationId) {
        ShopRegistration registration = shopRegistrationRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SHOP_REGISTRATION_NOT_FOUND",
                        "No shop registration draft was found."));

        return mapToResponse(registration);
    }

    @Override
    public ShopRegistrationResponse submit(ShopRegistrationRequest request) {
        ShopRegistration registration = findOrCreateDraft(request.getRegistrationId());
        applyRequestToEntity(registration, request, true);
        validateForSubmission(registration, request);

        registration.setStatus("SUBMITTED");
        registration.setSubmittedAt(LocalDateTime.now());
        registration.setUpdatedAt(LocalDateTime.now());

        ShopRegistration savedRegistration = shopRegistrationRepository.save(registration);
        return mapToResponse(savedRegistration);
    }

    @Override
    public ShopOtpResponse sendOtp(ShopOtpSendRequest request) {
        String channel = normalizeChannel(request.getChannel());
        String contact = normalize(request.getContact());

        if (!hasText(contact)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Contact is required for OTP.");
        }

        ShopRegistration registration = findOrCreateDraft(request.getRegistrationId());
        validateOtpContact(channel, contact);

        if ("EMAIL".equals(channel)) {
            if (!contact.equals(registration.getEmail())) {
                registration.setEmail(contact);
                registration.setEmailOtp(null);
                registration.setEmailOtpVerified(false);
            }
        } else {
            if (!contact.equals(registration.getPhoneNumber())) {
                registration.setPhoneNumber(contact);
                registration.setPhoneOtp(null);
                registration.setPhoneOtpVerified(false);
            }
        }

        String otp = generateOtp();
        LocalDateTime now = LocalDateTime.now();

        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setEmail("EMAIL".equals(channel) ? contact : null);
        otpRequest.setPhoneNumber("PHONE".equals(channel) ? contact : null);
        otpRequest.setOtpHash(hashOtp(otp));
        otpRequest.setPurpose(resolveOtpPurpose(channel));
        otpRequest.setExpiresAt(now.plusMinutes(OTP_EXPIRY_MINUTES));
        otpRequest.setVerified(false);
        otpRequest.setAttempts(0);
        otpRequest.setCreatedAt(now);
        otpRequestRepository.save(otpRequest);

        if ("EMAIL".equals(channel)) {
            sendOtpEmail(contact, normalize(request.getOwnerFullName()), otp);
        } else {
            sendOtpSms(contact, otp);
        }

        registration.setUpdatedAt(now);
        ShopRegistration savedRegistration = shopRegistrationRepository.save(registration);

        return ShopOtpResponse.builder()
                .registrationId(savedRegistration.getRegistrationId())
                .channel(channel)
                .contact(contact)
                .verified(false)
                .message("EMAIL".equals(channel)
            ? "Email OTP sent successfully. Check your inbox and spam folder."
                : "Phone OTP sent successfully. Check your SMS inbox.")
            .previewOtp(null)
                .build();
    }

    @Override
    public ShopOtpResponse verifyOtp(ShopOtpVerifyRequest request) {
        String channel = normalizeChannel(request.getChannel());
        String contact = normalize(request.getContact());
        String otp = normalize(request.getOtp());

        if (!hasText(contact) || !hasText(otp)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Contact and OTP are required.");
        }

        validateOtpContact(channel, contact);

        if (!OTP_PATTERN.matcher(otp).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "OTP must be a 6 digit number.");
        }

        ShopRegistration registration = findOrCreateDraft(request.getRegistrationId());
        OtpRequest otpRequest = loadOtpRequest(channel, contact);

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

        if ("EMAIL".equals(channel)) {
            registration.setEmail(contact);
            registration.setEmailOtp(otp);
            registration.setEmailOtpVerified(true);
        } else {
            registration.setPhoneNumber(contact);
            registration.setPhoneOtp(otp);
            registration.setPhoneOtpVerified(true);
        }

        registration.setUpdatedAt(LocalDateTime.now());
        ShopRegistration savedRegistration = shopRegistrationRepository.save(registration);

        return ShopOtpResponse.builder()
                .registrationId(savedRegistration.getRegistrationId())
                .channel(channel)
                .contact(contact)
                .verified(true)
                .message("EMAIL".equals(channel) ? "Email OTP verified." : "Phone OTP verified.")
                .previewOtp(null)
                .build();
    }

    @Override
    public ShopOtpResponse confirmPhoneVerification(ShopPhoneVerificationConfirmRequest request) {
        String phoneNumber = normalize(request.getPhoneNumber());

        if (!hasText(phoneNumber)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Phone number is required.");
        }

        validateOtpContact("PHONE", phoneNumber);

        ShopRegistration registration = findOrCreateDraft(request.getRegistrationId());
        registration.setPhoneNumber(phoneNumber);
        registration.setPhoneOtp("FIREBASE_VERIFIED");
        registration.setPhoneOtpVerified(true);
        registration.setUpdatedAt(LocalDateTime.now());

        ShopRegistration savedRegistration = shopRegistrationRepository.save(registration);

        return ShopOtpResponse.builder()
                .registrationId(savedRegistration.getRegistrationId())
                .channel("PHONE")
                .contact(phoneNumber)
                .verified(true)
                .message("Phone number verified with Firebase Authentication.")
                .previewOtp(null)
                .build();
    }

    @Override
    public ShopFileUploadResponse uploadFile(String registrationId, String fieldName, MultipartFile file) {
        if (!ALLOWED_UPLOAD_FIELDS.contains(fieldName)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_UPLOAD_FIELD",
                    "The selected upload field is not supported.");
        }

        validateUpload(fieldName, file);

        ShopRegistration registration = findOrCreateDraft(registrationId);
        ensureUploadDirectoryExists();

        String originalFileName = file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename();
        String fileExtension = extractFileExtension(originalFileName);
        String storedFileName = String.format("%s-%s-%d%s",
                registration.getRegistrationId(),
                fieldName,
                System.currentTimeMillis(),
                fileExtension);

        Path targetPath = uploadDirectory.resolve(storedFileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_UPLOAD_FAILED",
                    "Unable to store the uploaded file right now.");
        }

        String storedFilePath = targetPath.toString().replace('\\', '/');
        setUploadedFilePath(registration, fieldName, storedFilePath);
        registration.setUpdatedAt(LocalDateTime.now());

        ShopRegistration savedRegistration = shopRegistrationRepository.save(registration);

        return ShopFileUploadResponse.builder()
                .registrationId(savedRegistration.getRegistrationId())
                .shopUniqueId(savedRegistration.getShopUniqueId())
                .fieldName(fieldName)
                .fileName(originalFileName)
                .filePath(storedFilePath)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    private ShopRegistration findOrCreateDraft(String registrationId) {
        if (hasText(registrationId)) {
            return shopRegistrationRepository.findByRegistrationId(registrationId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SHOP_REGISTRATION_NOT_FOUND",
                            "No shop registration draft was found."));
        }

        ShopRegistration registration = new ShopRegistration();
        LocalDateTime now = LocalDateTime.now();
        registration.setRegistrationId(UUID.randomUUID().toString());
        registration.setShopUniqueId(generateShopUniqueId());
        registration.setStatus("DRAFT");
        registration.setCurrentStep(1);
        registration.setCountry("India");
        registration.setEmailOtpVerified(false);
        registration.setPhoneOtpVerified(false);
        registration.setCreatedAt(now);
        registration.setUpdatedAt(now);
        return registration;
    }

    private void applyRequestToEntity(ShopRegistration registration, ShopRegistrationRequest request, boolean allowPasswordUpdate) {
        String previousEmail = registration.getEmail();
        String previousPhoneNumber = registration.getPhoneNumber();
        String normalizedEmail = normalize(request.getEmail());
        String normalizedPhoneNumber = normalize(request.getPhoneNumber());

        registration.setCurrentStep(request.getCurrentStep() == null ? registration.getCurrentStep() : request.getCurrentStep());
        registration.setOwnerFullName(normalize(request.getOwnerFullName()));
        registration.setEmail(normalizedEmail);
        registration.setPhoneNumber(normalizedPhoneNumber);
        registration.setEmailOtp(normalize(request.getEmailOtp()));
        registration.setPhoneOtp(normalize(request.getPhoneOtp()));
        registration.setProfilePhoto(normalize(request.getProfilePhoto()));
        registration.setShopName(normalize(request.getShopName()));
        registration.setShopCategory(normalize(request.getShopCategory()));
        registration.setShopDescription(normalize(request.getShopDescription()));
        registration.setAddressLine1(normalize(request.getAddressLine1()));
        registration.setAddressLine2(normalize(request.getAddressLine2()));
        registration.setCity(normalize(request.getCity()));
        registration.setState(normalize(request.getState()));
        registration.setPincode(normalize(request.getPincode()));
        registration.setCountry(hasText(request.getCountry()) ? request.getCountry().trim() : registration.getCountry());
        registration.setLandmark(normalize(request.getLandmark()));
        registration.setLatitude(request.getLatitude());
        registration.setLongitude(request.getLongitude());
        registration.setShopPhoneNumber(normalize(request.getShopPhoneNumber()));
        registration.setShopEmail(normalize(request.getShopEmail()));
        registration.setWhatsappNumber(normalize(request.getWhatsappNumber()));
        registration.setOwnerAadharNumber(normalize(request.getOwnerAadharNumber()));
        registration.setOwnerPanNumber(normalizeUpper(request.getOwnerPanNumber()));
        registration.setOwnerAadharPhoto(normalize(request.getOwnerAadharPhoto()));
        registration.setOwnerPanPhoto(normalize(request.getOwnerPanPhoto()));
        registration.setOwnerSelfieWithId(normalize(request.getOwnerSelfieWithId()));
        registration.setGstNumber(normalizeUpper(request.getGstNumber()));
        registration.setGstCertificateUpload(normalize(request.getGstCertificateUpload()));
        registration.setBusinessRegistrationNumber(normalize(request.getBusinessRegistrationNumber()));
        registration.setAccountHolderName(normalize(request.getAccountHolderName()));
        registration.setBankName(normalize(request.getBankName()));
        registration.setAccountNumber(normalize(request.getAccountNumber()));
        registration.setIfscCode(normalizeUpper(request.getIfscCode()));
        registration.setUpiId(normalize(request.getUpiId()));
        registration.setCancelledChequePhoto(normalize(request.getCancelledChequePhoto()));
        registration.setAcceptTermsAndConditions(Boolean.TRUE.equals(request.getAcceptTermsAndConditions()));
        registration.setAcceptPrivacyPolicy(Boolean.TRUE.equals(request.getAcceptPrivacyPolicy()));
        registration.setAcceptCommissionPolicy(Boolean.TRUE.equals(request.getAcceptCommissionPolicy()));

        if (registration.getEmailOtpVerified() == null) {
            registration.setEmailOtpVerified(false);
        }

        if (registration.getPhoneOtpVerified() == null) {
            registration.setPhoneOtpVerified(false);
        }

        if ((previousEmail == null && normalizedEmail != null) || (previousEmail != null && !previousEmail.equals(normalizedEmail))) {
            registration.setEmailOtpVerified(false);
            registration.setEmailOtp(null);
        }

        if ((previousPhoneNumber == null && normalizedPhoneNumber != null)
                || (previousPhoneNumber != null && !previousPhoneNumber.equals(normalizedPhoneNumber))) {
            registration.setPhoneOtpVerified(false);
            registration.setPhoneOtp(null);
        }

        if (allowPasswordUpdate && hasText(request.getPassword())) {
            registration.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }
    }

    private void validateForSubmission(ShopRegistration registration, ShopRegistrationRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        requireText(registration.getOwnerFullName(), "ownerFullName", "Owner full name is required", fieldErrors);
        requireEmail(registration.getEmail(), "email", true, fieldErrors);
        requirePhone(registration.getPhoneNumber(), "phoneNumber", true, fieldErrors);
        requireText(request.getPassword(), "password", "Password is required", fieldErrors);
        requireText(request.getConfirmPassword(), "confirmPassword", "Confirm password is required", fieldErrors);

        if (hasText(request.getPassword()) && request.getPassword().trim().length() < 8) {
            fieldErrors.putIfAbsent("password", "Password must be at least 8 characters long");
        }

        if (hasText(request.getPassword()) && hasText(request.getConfirmPassword())
                && !request.getPassword().trim().equals(request.getConfirmPassword().trim())) {
            fieldErrors.putIfAbsent("confirmPassword", "Passwords do not match");
        }

        if (!Boolean.TRUE.equals(registration.getEmailOtpVerified()) && !Boolean.TRUE.equals(registration.getPhoneOtpVerified())) {
            fieldErrors.putIfAbsent("emailOtp", "Verify either email OTP or phone OTP before continuing");
            fieldErrors.putIfAbsent("phoneOtp", "Verify either email OTP or phone OTP before continuing");
        }

        requireText(registration.getProfilePhoto(), "profilePhoto", "Profile photo is required", fieldErrors);
        requireText(registration.getShopName(), "shopName", "Shop name is required", fieldErrors);
        requireText(registration.getShopCategory(), "shopCategory", "Shop category is required", fieldErrors);
        requireText(registration.getShopDescription(), "shopDescription", "Shop description is required", fieldErrors);
        requireText(registration.getAddressLine1(), "addressLine1", "Address line 1 is required", fieldErrors);
        requireText(registration.getCity(), "city", "City is required", fieldErrors);
        requireText(registration.getState(), "state", "State is required", fieldErrors);
        requireText(registration.getCountry(), "country", "Country is required", fieldErrors);
        requirePattern(registration.getPincode(), PINCODE_PATTERN, "pincode", "Enter a valid 6 digit pincode", fieldErrors);
        requirePhone(registration.getShopPhoneNumber(), "shopPhoneNumber", true, fieldErrors);
        requireEmail(registration.getShopEmail(), "shopEmail", true, fieldErrors);
        requirePhone(registration.getWhatsappNumber(), "whatsappNumber", true, fieldErrors);
        requirePattern(registration.getOwnerAadharNumber(), AADHAR_PATTERN, "ownerAadharNumber",
                "Enter a valid 12 digit Aadhar number", fieldErrors);
        requirePattern(registration.getOwnerPanNumber(), PAN_PATTERN, "ownerPanNumber",
                "Enter a valid PAN number", fieldErrors);
        requireText(registration.getOwnerAadharPhoto(), "ownerAadharPhoto", "Aadhar photo is required", fieldErrors);
        requireText(registration.getOwnerPanPhoto(), "ownerPanPhoto", "PAN photo is required", fieldErrors);
        requireText(registration.getOwnerSelfieWithId(), "ownerSelfieWithId", "Selfie with ID is required", fieldErrors);
        requireText(registration.getBusinessRegistrationNumber(), "businessRegistrationNumber",
                "Business registration number is required", fieldErrors);

        if (hasText(registration.getGstNumber()) && !GST_PATTERN.matcher(registration.getGstNumber()).matches()) {
            fieldErrors.putIfAbsent("gstNumber", "Enter a valid GST number");
        }

        requireText(registration.getAccountHolderName(), "accountHolderName", "Account holder name is required", fieldErrors);
        requireText(registration.getBankName(), "bankName", "Bank name is required", fieldErrors);
        requirePattern(registration.getAccountNumber(), ACCOUNT_NUMBER_PATTERN, "accountNumber",
                "Enter a valid bank account number", fieldErrors);
        requireText(request.getConfirmAccountNumber(), "confirmAccountNumber", "Confirm account number is required", fieldErrors);

        if (hasText(registration.getAccountNumber()) && hasText(request.getConfirmAccountNumber())
                && !registration.getAccountNumber().equals(request.getConfirmAccountNumber().trim())) {
            fieldErrors.putIfAbsent("confirmAccountNumber", "Account numbers do not match");
        }

        requirePattern(registration.getIfscCode(), IFSC_PATTERN, "ifscCode", "Enter a valid IFSC code", fieldErrors);

        if (hasText(registration.getUpiId()) && !UPI_PATTERN.matcher(registration.getUpiId()).matches()) {
            fieldErrors.putIfAbsent("upiId", "Enter a valid UPI ID");
        }

        requireText(registration.getCancelledChequePhoto(), "cancelledChequePhoto",
                "Cancelled cheque upload is required", fieldErrors);

        if (!Boolean.TRUE.equals(registration.getAcceptTermsAndConditions())) {
            fieldErrors.putIfAbsent("acceptTermsAndConditions", "You must accept the terms and conditions");
        }

        if (!Boolean.TRUE.equals(registration.getAcceptPrivacyPolicy())) {
            fieldErrors.putIfAbsent("acceptPrivacyPolicy", "You must accept the privacy policy");
        }

        if (!Boolean.TRUE.equals(registration.getAcceptCommissionPolicy())) {
            fieldErrors.putIfAbsent("acceptCommissionPolicy", "You must accept the commission policy");
        }

        if (!fieldErrors.isEmpty()) {
            throw new RequestValidationException("Validation failed", fieldErrors);
        }
    }

    private void validateUpload(String fieldName, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "EMPTY_FILE", "Please choose a file to upload.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE",
                    "File size must be less than or equal to 5 MB.");
        }

        boolean isPhotoField = PHOTO_UPLOAD_FIELDS.contains(fieldName);
        Set<String> allowedContentTypes = isPhotoField ? IMAGE_CONTENT_TYPES : DOCUMENT_CONTENT_TYPES;

        if (!allowedContentTypes.contains(file.getContentType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE",
                isPhotoField
                    ? "Only JPG, PNG, and WEBP images are allowed for this upload."
                    : "Only PDF and DOCX files are allowed for this upload.");
        }
    }

    private void ensureUploadDirectoryExists() {
        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "UPLOAD_DIRECTORY_ERROR",
                    "Unable to prepare storage for file uploads.");
        }
    }

    private void setUploadedFilePath(ShopRegistration registration, String fieldName, String filePath) {
        switch (fieldName) {
            case "profilePhoto" -> registration.setProfilePhoto(filePath);
            case "ownerAadharPhoto" -> registration.setOwnerAadharPhoto(filePath);
            case "ownerPanPhoto" -> registration.setOwnerPanPhoto(filePath);
            case "ownerSelfieWithId" -> registration.setOwnerSelfieWithId(filePath);
            case "gstCertificateUpload" -> registration.setGstCertificateUpload(filePath);
            case "cancelledChequePhoto" -> registration.setCancelledChequePhoto(filePath);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_UPLOAD_FIELD",
                    "The selected upload field is not supported.");
        }
    }

    private ShopRegistrationResponse mapToResponse(ShopRegistration registration) {
        return ShopRegistrationResponse.builder()
                .registrationId(registration.getRegistrationId())
                .shopUniqueId(registration.getShopUniqueId())
                .status(registration.getStatus())
                .currentStep(registration.getCurrentStep())
                .ownerFullName(registration.getOwnerFullName())
                .email(registration.getEmail())
                .phoneNumber(registration.getPhoneNumber())
                .emailOtp(registration.getEmailOtp())
                .emailOtpVerified(registration.getEmailOtpVerified())
                .phoneOtp(registration.getPhoneOtp())
                .phoneOtpVerified(registration.getPhoneOtpVerified())
                .profilePhoto(registration.getProfilePhoto())
                .shopName(registration.getShopName())
                .shopCategory(registration.getShopCategory())
                .shopDescription(registration.getShopDescription())
                .addressLine1(registration.getAddressLine1())
                .addressLine2(registration.getAddressLine2())
                .city(registration.getCity())
                .state(registration.getState())
                .pincode(registration.getPincode())
                .country(registration.getCountry())
                .landmark(registration.getLandmark())
                .latitude(registration.getLatitude())
                .longitude(registration.getLongitude())
                .shopPhoneNumber(registration.getShopPhoneNumber())
                .shopEmail(registration.getShopEmail())
                .whatsappNumber(registration.getWhatsappNumber())
                .ownerAadharNumber(registration.getOwnerAadharNumber())
                .ownerPanNumber(registration.getOwnerPanNumber())
                .ownerAadharPhoto(registration.getOwnerAadharPhoto())
                .ownerPanPhoto(registration.getOwnerPanPhoto())
                .ownerSelfieWithId(registration.getOwnerSelfieWithId())
                .gstNumber(registration.getGstNumber())
                .gstCertificateUpload(registration.getGstCertificateUpload())
                .businessRegistrationNumber(registration.getBusinessRegistrationNumber())
                .accountHolderName(registration.getAccountHolderName())
                .bankName(registration.getBankName())
                .accountNumber(registration.getAccountNumber())
                .ifscCode(registration.getIfscCode())
                .upiId(registration.getUpiId())
                .cancelledChequePhoto(registration.getCancelledChequePhoto())
                .acceptTermsAndConditions(registration.getAcceptTermsAndConditions())
                .acceptPrivacyPolicy(registration.getAcceptPrivacyPolicy())
                .acceptCommissionPolicy(registration.getAcceptCommissionPolicy())
                .lastSavedAt(registration.getUpdatedAt())
                .submittedAt(registration.getSubmittedAt())
                .build();
    }

    private OtpRequest loadOtpRequest(String channel, String contact) {
        return "EMAIL".equals(channel)
                ? otpRequestRepository.findTopByEmailAndPurposeAndVerifiedFalseOrderByIdDesc(contact, resolveOtpPurpose(channel))
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP_NOT_FOUND",
                                "No email OTP was found. Please request a new OTP."))
                : otpRequestRepository.findTopByPhoneNumberAndPurposeAndVerifiedFalseOrderByIdDesc(contact, resolveOtpPurpose(channel))
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "OTP_NOT_FOUND",
                                "No phone OTP was found. Please request a new OTP."));
    }

    private void validateOtpContact(String channel, String contact) {
        if ("EMAIL".equals(channel) && !EMAIL_PATTERN.matcher(contact).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Enter a valid email address.");
        }

        if ("PHONE".equals(channel) && !PHONE_PATTERN.matcher(contact).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Enter a valid 10 digit mobile number.");
        }
    }

    private void requireText(String value, String field, String message, Map<String, String> fieldErrors) {
        if (!hasText(value)) {
            fieldErrors.putIfAbsent(field, message);
        }
    }

    private void requireEmail(String value, String field, boolean required, Map<String, String> fieldErrors) {
        if (!hasText(value)) {
            if (required) {
                fieldErrors.putIfAbsent(field, "Email is required");
            }
            return;
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            fieldErrors.putIfAbsent(field, "Enter a valid email address");
        }
    }

    private void requirePhone(String value, String field, boolean required, Map<String, String> fieldErrors) {
        if (!hasText(value)) {
            if (required) {
                fieldErrors.putIfAbsent(field, "Phone number is required");
            }
            return;
        }

        if (!PHONE_PATTERN.matcher(value).matches()) {
            fieldErrors.putIfAbsent(field, "Enter a valid 10 digit mobile number");
        }
    }

    private void requirePattern(String value, Pattern pattern, String field, String message,
            Map<String, String> fieldErrors) {
        if (!hasText(value)) {
            fieldErrors.putIfAbsent(field, message);
            return;
        }

        if (!pattern.matcher(value).matches()) {
            fieldErrors.putIfAbsent(field, message);
        }
    }

    private String normalizeChannel(String channel) {
        String normalizedChannel = normalizeUpper(channel);

        if (!"EMAIL".equals(normalizedChannel) && !"PHONE".equals(normalizedChannel)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Channel must be EMAIL or PHONE.");
        }

        return normalizedChannel;
    }

    private OtpPurpose resolveOtpPurpose(String channel) {
        return "PHONE".equals(channel) ? OtpPurpose.PHONE_VERIFY : OtpPurpose.REGISTER;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private String normalizeUpper(String value) {
        return hasText(value) ? value.trim().toUpperCase() : null;
    }

    private String toIndianE164(String phoneNumber) {
        String digits = phoneNumber == null ? "" : phoneNumber.replaceAll("\\D", "");

        if (!PHONE_PATTERN.matcher(digits).matches()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Enter a valid 10 digit mobile number.");
        }

        return "+91" + digits;
    }

    private String generateShopUniqueId() {
        String randomSuffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "SHOP-IND-" + randomSuffix;
    }

    private String extractFileExtension(String originalFileName) {
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex < 0) {
            return "";
        }

        return originalFileName.substring(extensionIndex);
    }

    private String generateOtp() {
        return String.valueOf(secureRandom.nextInt(OTP_BOUND) + OTP_OFFSET);
    }

    private void sendOtpSms(String recipientPhoneNumber, String otp) {
        if (!hasText(twilioAccountSid) || !hasText(twilioAuthToken) || !hasText(twilioFromNumber)) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TWILIO_NOT_CONFIGURED",
                    "Twilio phone OTP is not configured. Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER."
            );
        }

        String e164PhoneNumber = toIndianE164(recipientPhoneNumber);

        try {
            Twilio.init(twilioAccountSid.trim(), twilioAuthToken.trim());
            Message.creator(
                    new PhoneNumber(e164PhoneNumber),
                    new PhoneNumber(twilioFromNumber.trim()),
                    "Your Puja Marketplace shop registration OTP is " + otp + ". It is valid for " + OTP_EXPIRY_MINUTES + " minutes."
            ).create();
        } catch (Exception exception) {
            throw new ApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OTP_SMS_SEND_FAILED",
                    "Failed to send phone OTP through Twilio. Please verify Twilio configuration and phone number format."
            );
        }
    }

    private void sendOtpEmail(String recipientEmail, String fullName, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }

            helper.setTo(recipientEmail);
            helper.setSubject(otpMailSubject + " - Shop Registration");

            Path imageAbsolutePath = Path.of(logoPath).toAbsolutePath().normalize();
            boolean hasLogo = Files.exists(imageAbsolutePath);
            helper.setText(buildEmailBody(fullName, otp, hasLogo), true);

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
        String recipientName = hasText(fullName) ? fullName.trim() : "User";

        String logoSection = hasLogo
                ? "<div style='text-align:center;margin-bottom:20px;'><img src='cid:brandLogo' alt='Logo' style='max-width:180px;height:auto;display:inline-block;'/></div>"
                : "";

        return "<div style='font-family:Segoe UI,Arial,sans-serif;color:#1b1b1b;max-width:560px;margin:0 auto;padding:24px;border:1px solid #ececec;border-radius:10px;background:#ffffff;'>"
                + logoSection
                + "<p style='margin:0 0 12px;font-size:16px;'>Namaste 🙏 " + escapeHtml(recipientName) + ",</p>"
                + "<p style='margin:0 0 16px;font-size:15px;line-height:1.6;'>Use the following One Time Password (OTP) to verify your shop registration on Puja Marketplace.</p>"
                + "<div style='text-align:center;margin:18px 0;'>"
                + "<span style='display:inline-block;letter-spacing:4px;font-size:30px;font-weight:700;padding:10px 18px;border-radius:8px;background:#f5f5f5;border:1px solid #dcdcdc;'>"
                + escapeHtml(otp)
                + "</span></div>"
                + "<p style='margin:16px 0;font-size:14px;line-height:1.6;'>This OTP is valid for " + OTP_EXPIRY_MINUTES + " minutes. Please do not share it with anyone.</p>"
                + "<p style='margin:20px 0 4px;font-size:14px;'>Strength • Dharma • Truth</p>"
                + "<p style='margin:0 0 16px;font-size:15px;font-weight:600;'>Har Har Mahadev 🔱</p>"
                + "<p style='margin:0;font-size:14px;'>— Team Puja Marketplace</p>"
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

    private String hashOtp(String otp) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(otp.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "OTP_HASHING_FAILED", "Failed to process OTP.");
        }
    }

    private boolean matchesOtp(String otp, String otpHash) {
        return hashOtp(otp).equals(otpHash);
    }
}
