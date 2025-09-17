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

	/**
	 * USER ERROR
	 */
	INVALID_KEY_INPUT(TokenError.CODE_PREFIX + "01-01", "잘못된 키 입력입니다.", HttpStatus.BAD_REQUEST),
	MISSING_KEY(TokenError.CODE_PREFIX + "01-02", "필수 키 값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
	MISSING_CLAIM(TokenError.CODE_PREFIX + "01-03", "필수 JWT Claim이 누락되었습니다.", HttpStatus.BAD_REQUEST),
	INVALID_CLAIM_FORMAT(TokenError.CODE_PREFIX + "01-04", "JWT Claim 형식이 잘못되었습니다.", HttpStatus.BAD_REQUEST),

	/**
	 * SYSTEM ERROR
	 */
	KEY_ENCODING_FAILED(TokenError.CODE_PREFIX + "02-01", "키 인코딩 중 시스템 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	BASE58_ENCODING_FAILED(TokenError.CODE_PREFIX + "02-02", "Base58 인코딩 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	JWT_CREATION_FAILED(TokenError.CODE_PREFIX + "02-03", "JWT 생성 중 시스템 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

	;

	public static final String CODE_PREFIX = "Token-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}
