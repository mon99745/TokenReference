package io.jwt4j.lite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = {"io.jwt4j.lite.app", "io.jwt4j.lite.core"})
@ConfigurationPropertiesScan
public class Jwt4jLiteAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(Jwt4jLiteAppApplication.class, args);
	}
}
