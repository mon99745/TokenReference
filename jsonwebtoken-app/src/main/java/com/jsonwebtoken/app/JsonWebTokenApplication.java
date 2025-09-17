package com.jsonwebtoken.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"com.jsonwebtoken.app", "com.jsonwebtoken.core"})
@ConfigurationPropertiesScan
public class JsonWebTokenApplication {
	public static void main(String[] args) {
		SpringApplication.run(JsonWebTokenApplication.class, args);
	}
}
