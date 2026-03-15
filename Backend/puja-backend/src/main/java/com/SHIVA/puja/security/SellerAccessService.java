package com.SHIVA.puja.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.SHIVA.puja.entity.Seller;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.SellerRepository;
import com.SHIVA.puja.repository.UserRepository;

@Service
public class SellerAccessService {

    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;

    public SellerAccessService(SellerRepository sellerRepository, UserRepository userRepository) {
        this.sellerRepository = sellerRepository;
        this.userRepository = userRepository;
    }

    public Seller resolveManagedSeller(String sellerCode) {
        Authentication authentication = requireAuthentication();
        String email = authentication.getName();
        String role = userRepository.findTopByEmailOrderByIdDesc(email)
                .map(com.SHIVA.puja.entity.User::getRole)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user was not found."));

        if ("ADMIN".equalsIgnoreCase(role)) {
            if (sellerCode == null || sellerCode.isBlank()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "SELLER_CODE_REQUIRED", "Seller code is required for admin access.");
            }
            return sellerRepository.findBySellerCode(sellerCode.trim())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_FOUND", "Seller was not found."));
        }

        return sellerRepository.findTopByEmailOrderByIdDesc(email)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "SELLER_ACCESS_DENIED", "Seller profile was not found for the authenticated user."));
    }

    public Seller resolveSellerByRegistrationId(String registrationId) {
        Authentication authentication = requireAuthentication();
        String email = authentication.getName();
        String role = userRepository.findTopByEmailOrderByIdDesc(email)
            .map(com.SHIVA.puja.entity.User::getRole)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user was not found."));

        Seller seller = sellerRepository.findByRegistrationId(registrationId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SELLER_NOT_ONBOARDED",
                "Seller is not onboarded yet for this registration."));

        if ("ADMIN".equalsIgnoreCase(role)) {
            return seller;
        }

        if (seller.getEmail() != null && seller.getEmail().equalsIgnoreCase(email)) {
            return seller;
        }

        throw new ApiException(HttpStatus.FORBIDDEN, "SELLER_ACCESS_DENIED", "You do not have access to this seller workspace.");
    }

    public String currentEmail() {
        return requireAuthentication().getName();
    }

    private Authentication requireAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication is required.");
        }
        return authentication;
    }
}
