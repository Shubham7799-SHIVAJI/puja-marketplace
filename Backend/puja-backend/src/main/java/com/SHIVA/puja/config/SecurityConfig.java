package com.SHIVA.puja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import com.SHIVA.puja.security.AuditLoggingFilter;
import com.SHIVA.puja.security.AdminIpRestrictionFilter;
import com.SHIVA.puja.security.JwtAuthenticationFilter;
import com.SHIVA.puja.security.RateLimitingFilter;
import com.SHIVA.puja.security.SellerUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
            RateLimitingFilter rateLimitingFilter, AuditLoggingFilter auditLoggingFilter,
            AdminIpRestrictionFilter adminIpRestrictionFilter,
            AuthenticationProvider authenticationProvider) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .requireCsrfProtectionMatcher(new AndRequestMatcher(
                    CsrfFilter.DEFAULT_CSRF_MATCHER,
                    PathPatternRequestMatcher.withDefaults().matcher("/api/admin/**"))))
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/api/v1/auth/**", "/shop-registration/**", "/api/v1/shop-registration/**", "/actuator/**").permitAll()
                        .requestMatchers("/webhooks/**", "/api/v1/webhooks/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/marketplace/**", "/api/v1/marketplace/**").permitAll()
                    .requestMatchers("/marketplace/**", "/api/v1/marketplace/**").hasAnyRole("CUSTOMER", "SELLER", "ADMIN")
                        .requestMatchers("/payments/**", "/api/v1/payments/**").hasAnyRole("CUSTOMER", "ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/seller-dashboard/**", "/api/v1/seller-dashboard/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/seller-admin/**", "/api/v1/seller-admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/seller-api/**", "/api/v1/seller-api/**").hasAnyRole("SELLER", "ADMIN")
                        .requestMatchers("/admin-dashboard/**", "/api/v1/admin-dashboard/**", "/admin-control/**", "/api/v1/admin-control/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .addFilterAfter(adminIpRestrictionFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(auditLoggingFilter, JwtAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                        .contentTypeOptions(Customizer.withDefaults())
                    .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'; script-src 'self'; object-src 'none'; frame-ancestors 'none'; base-uri 'self'"))
                    .referrerPolicy(referrer -> referrer.policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .xssProtection(xss -> xss.disable())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)));
        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(SellerUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
