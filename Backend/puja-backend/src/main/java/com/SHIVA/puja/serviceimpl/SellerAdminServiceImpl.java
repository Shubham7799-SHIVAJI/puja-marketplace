package com.SHIVA.puja.serviceimpl;

import java.time.LocalDateTime;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.SHIVA.puja.dto.SellerOnboardingRequest;
import com.SHIVA.puja.dto.SellerOnboardingResponse;
import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.entity.ShopRegistration;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.ShopRegistrationRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.service.SellerAdminService;

@Service
@Transactional
public class SellerAdminServiceImpl implements SellerAdminService {

    private final ShopRegistrationRepository shopRegistrationRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SellerAdminServiceImpl(ShopRegistrationRepository shopRegistrationRepository, SellerRepository sellerRepository,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.shopRegistrationRepository = shopRegistrationRepository;
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public SellerOnboardingResponse onboardSeller(SellerOnboardingRequest request) {
        ShopRegistration registration = shopRegistrationRepository.findByRegistrationId(request.getRegistrationId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "REGISTRATION_NOT_FOUND",
                        "Shop registration was not found."));

        if (!"SUBMITTED".equalsIgnoreCase(registration.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "REGISTRATION_NOT_SUBMITTED",
                    "Only submitted shop registrations can be onboarded.");
        }

        Seller seller = sellerRepository.findByRegistrationId(registration.getRegistrationId())
                .orElseGet(() -> createSeller(registration));

        boolean activateSellerUser = request.getActivateSellerUser() == null || request.getActivateSellerUser();
        User sellerUser = null;
        if (activateSellerUser) {
            sellerUser = userRepository.findTopByEmailOrderByIdDesc(resolveSellerEmail(request, registration))
                    .map(existing -> promoteToSeller(existing, registration, request))
                    .orElseGet(() -> createSellerUser(registration, request));
        }

        return SellerOnboardingResponse.builder()
                .sellerCode(seller.getSellerCode())
                .registrationId(registration.getRegistrationId())
                .shopName(seller.getShopName())
                .sellerEmail(sellerUser != null ? sellerUser.getEmail() : seller.getEmail())
                .role(sellerUser != null ? sellerUser.getRole() : "SELLER")
                .status(seller.getStatus())
                .message("Seller onboarded successfully.")
                .build();
    }

    private Seller createSeller(ShopRegistration registration) {
        Seller seller = new Seller();
        seller.setRegistrationId(registration.getRegistrationId());
        seller.setSellerCode("SELLER-" + registration.getShopUniqueId());
        seller.setShopName(defaultString(registration.getShopName(), "Seller Shop"));
        seller.setOwnerName(defaultString(registration.getOwnerFullName(), "Seller Admin"));
        seller.setEmail(defaultString(registration.getShopEmail(), registration.getEmail()));
        seller.setPhoneNumber(defaultString(registration.getShopPhoneNumber(), registration.getPhoneNumber()));
        seller.setStatus("ACTIVE");
        seller.setGstNumber(defaultString(registration.getGstNumber(), "GST-PENDING"));
        seller.setShopAddress(buildAddress(registration));
        seller.setShopLogo(initials(seller.getShopName()));
        seller.setShopBanner(defaultString(registration.getShopCategory(), "Seller storefront"));
        seller.setReturnPolicy("7-day returns for damaged shipments and unopened devotional products.");
        seller.setBankAccountMasked(maskAccount(registration.getAccountNumber()));
        seller.setCreatedAt(LocalDateTime.now());
        seller.setUpdatedAt(LocalDateTime.now());
        return sellerRepository.save(seller);
    }

    private User createSellerUser(ShopRegistration registration, SellerOnboardingRequest request) {
        User user = new User();
        user.setFullName(defaultString(registration.getOwnerFullName(), "Seller Admin"));
        user.setEmail(resolveSellerEmail(request, registration));
        user.setPhoneNumber(defaultString(registration.getPhoneNumber(), registration.getShopPhoneNumber()));
        user.setRole("SELLER");
        user.setStatus("ACTIVE");
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        user.setPasswordHash(resolvePasswordHash(registration, request));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User promoteToSeller(User user, ShopRegistration registration, SellerOnboardingRequest request) {
        user.setFullName(defaultString(registration.getOwnerFullName(), user.getFullName()));
        user.setPhoneNumber(defaultString(registration.getPhoneNumber(), user.getPhoneNumber()));
        user.setRole("SELLER");
        user.setStatus("ACTIVE");
        user.setPhoneVerified(true);
        user.setEmailVerified(true);
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.setPasswordHash(resolvePasswordHash(registration, request));
        }
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String resolveSellerEmail(SellerOnboardingRequest request, ShopRegistration registration) {
        return defaultString(request.getSellerEmail(), defaultString(registration.getShopEmail(), registration.getEmail())).toLowerCase(Locale.ROOT);
    }

    private String resolvePasswordHash(ShopRegistration registration, SellerOnboardingRequest request) {
        if (registration.getPasswordHash() != null && !registration.getPasswordHash().isBlank()) {
            return registration.getPasswordHash();
        }
        if (request.getTemporaryPassword() == null || request.getTemporaryPassword().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "TEMP_PASSWORD_REQUIRED",
                    "A temporary password is required because the registration has no stored password hash.");
        }
        return passwordEncoder.encode(request.getTemporaryPassword().trim());
    }

    private String buildAddress(ShopRegistration registration) {
        return java.util.List.of(registration.getAddressLine1(), registration.getCity(), registration.getState(), registration.getPincode())
                .stream()
                .filter(this::hasText)
                .collect(java.util.stream.Collectors.joining(", "));
    }

    private String initials(String value) {
        if (!hasText(value)) {
            return "SC";
        }
        String[] parts = value.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (builder.length() == 2) {
                break;
            }
        }
        return builder.toString();
    }

    private String maskAccount(String accountNumber) {
        if (!hasText(accountNumber) || accountNumber.length() < 4) {
            return "•••• 0000";
        }
        return "•••• " + accountNumber.substring(accountNumber.length() - 4);
    }

    private String defaultString(String primary, String fallback) {
        return hasText(primary) ? primary.trim() : fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
