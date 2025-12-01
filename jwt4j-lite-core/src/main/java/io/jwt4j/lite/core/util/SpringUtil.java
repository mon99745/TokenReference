package io.jwt4j.lite.core.util;

import com.google.common.annotations.VisibleForTesting;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static io.jwt4j.lite.core.config.TokenProperties.PROPERTY_PREFIX;

@Slf4j
@Component
public class SpringUtil {
	public static final String SPRING_CONFIG_NAME = PROPERTY_PREFIX;
	private static final ExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

	@Setter
	@VisibleForTesting
	private static Environment environment;

	public SpringUtil(Environment environment) {
		SpringUtil.environment = environment;
	}
	public static Optional<Environment> getEnvironment() {
		if (environment == null) {
			return Optional.empty();
		}
		return Optional.of(environment);
	}
}