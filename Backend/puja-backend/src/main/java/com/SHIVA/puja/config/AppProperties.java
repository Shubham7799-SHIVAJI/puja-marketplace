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
        private String adminEmail;
        private String adminPassword;
        private String adminName;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private Integer requests;
        private Integer windowSeconds;
    }
}