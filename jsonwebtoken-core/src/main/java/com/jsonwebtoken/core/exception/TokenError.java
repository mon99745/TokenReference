package com.jsonwebtoken.core.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import com.jsonwebtoken.core.exception.common.Error;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum TokenError
		implements Error {

	SAMPE(TokenError.CODE_PREFIX + "00-00", "sample-message", HttpStatus.BAD_REQUEST);

	public static final String CODE_PREFIX = "Token-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}
