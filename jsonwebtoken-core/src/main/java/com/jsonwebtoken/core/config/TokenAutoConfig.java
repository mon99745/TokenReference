package com.jsonwebtoken.core.config;

import com.jsonwebtoken.core.service.KeyPairService;
import com.jsonwebtoken.core.service.TokenService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TokenProperties.class)
public class TokenAutoConfig {
	@Bean
	@ConditionalOnMissingBean(RsaKeyGenerator.class)
	public RsaKeyGenerator rsaKeyGenerator(TokenProperties tokenProperties) {
		return new RsaKeyGenerator(tokenProperties);
	}

	@Bean
	@ConditionalOnMissingBean(KeyPairService.class)
	public KeyPairService keyPairService(RsaKeyGenerator rsaKeyGenerator) {
		return new KeyPairService(rsaKeyGenerator);
	}

	@Bean
	@ConditionalOnMissingBean(TokenService.class)
	public TokenService tokenService(RsaKeyGenerator rsaKeyGenerator, TokenProperties tokenProperties,
									 KeyPairService keyPairService) {
		return new TokenService(rsaKeyGenerator, tokenProperties, keyPairService);
	}
}