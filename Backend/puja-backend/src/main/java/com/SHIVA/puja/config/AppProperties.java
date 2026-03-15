package com.SHIVA.puja.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Mail mail = new Mail();
    private final Otp otp = new Otp();
    private final Twilio twilio = new Twilio();
    private final Security security = new Security();
    private final RateLimit rateLimit = new RateLimit();
    private final Payment payment = new Payment();

    @Getter
    @Setter
    public static class Mail {
        private String logoPath;
        private String subject;
    }

    @Getter
    @Setter
    public static class Otp {
        private Integer expirationMinutes;
    }

    @Getter
    @Setter
    public static class Twilio {
        private String accountSid;
        private String authToken;
        private String fromNumber;
    }

    @Getter
    @Setter
    public static class Security {
        private String jwtSecret;
        private Long jwtExpirationMinutes;
        private Long passwordResetTokenExpirationMinutes;
        private Long shopRegistrationSessionTokenExpirationMinutes;
        private String adminEmail;
        private String adminPassword;
        private String adminName;
        private String adminAllowedIps;
        private Integer adminLoginAttempts;
        private Integer adminLoginWindowSeconds;
        private Integer adminMfaOtpExpirationMinutes;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Integer requests;
        private Integer windowSeconds;
    }

    @Getter
    @Setter
    public static class Payment {
        private String keyId;
        private String keySecret;
        private String webhookSecret;
        private String currency;
        private String kafkaPaymentTopic;
        private String kafkaOrderTopic;
    }
}