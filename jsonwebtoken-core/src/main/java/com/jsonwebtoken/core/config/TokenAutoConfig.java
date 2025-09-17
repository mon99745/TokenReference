package com.jsonwebtoken.core.config;

import com.jsonwebtoken.core.service.KeyPairService;
import com.jsonwebtoken.core.service.TokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(VerifyProperties.class)
public class TokenAutoConfig {
	@Bean
	@ConditionalOnMissingBean(RsaKeyGenerator.class)
	public RsaKeyGenerator rsaKeyGenerator(VerifyProperties verifyProperties) {
		return new RsaKeyGenerator(verifyProperties);
	}

	@Bean
	@ConditionalOnMissingBean(KeyPairService.class)
	public KeyPairService keyPairService(RsaKeyGenerator rsaKeyGenerator) {
		return new KeyPairService(rsaKeyGenerator);
	}

	@Bean
	@ConditionalOnMissingBean(TokenService.class)
	public TokenService tokenService(RsaKeyGenerator rsaKeyGenerator, VerifyProperties verifyProperties,
									 KeyPairService keyPairService) {
		return new TokenService(rsaKeyGenerator, verifyProperties, keyPairService);
	}
}