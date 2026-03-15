package com.SHIVA.puja.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
public class DatabaseMigrationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseMigrationConfig.class);

    @Bean
    public ApplicationRunner databaseMigrationRunner(DataSource dataSource) {
        return args -> {
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.setContinueOnError(false);
            populator.addScript(new ClassPathResource("sql/shop-registration-schema.sql"));
            populator.addScript(new ClassPathResource("sql/users-otp-columns.sql"));
            populator.addScript(new ClassPathResource("sql/seller-dashboard-schema.sql"));
            populator.addScript(new ClassPathResource("sql/security-and-audit-schema.sql"));
            populator.addScript(new ClassPathResource("sql/customer-marketplace-schema.sql"));
            populator.execute(dataSource);

            LOGGER.info("Applied startup database migrations for shop registration, OTP, seller dashboard, security schema, and customer marketplace schema.");
        };
    }
}