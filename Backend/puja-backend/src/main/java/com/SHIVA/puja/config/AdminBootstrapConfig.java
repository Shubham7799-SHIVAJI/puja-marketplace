package com.SHIVA.puja.config;

import java.time.LocalDateTime;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.repository.UserRepository;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    public ApplicationRunner adminBootstrapRunner(AppProperties appProperties, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        return args -> userRepository.findTopByEmailOrderByIdDesc(appProperties.getSecurity().getAdminEmail())
                .orElseGet(() -> {
                    User admin = new User();
                    admin.setFullName(appProperties.getSecurity().getAdminName());
                    admin.setEmail(appProperties.getSecurity().getAdminEmail());
                    admin.setRole("ADMIN");
                    admin.setStatus("ACTIVE");
                    admin.setPhoneVerified(true);
                    admin.setEmailVerified(true);
                    admin.setPasswordHash(passwordEncoder.encode(appProperties.getSecurity().getAdminPassword()));
                    admin.setCreatedAt(LocalDateTime.now());
                    admin.setUpdatedAt(LocalDateTime.now());
                    return userRepository.save(admin);
                });
    }
}
