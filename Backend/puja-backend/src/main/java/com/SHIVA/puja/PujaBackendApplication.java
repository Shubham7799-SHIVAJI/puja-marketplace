package com.SHIVA.puja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PujaBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PujaBackendApplication.class, args);
	}

}
