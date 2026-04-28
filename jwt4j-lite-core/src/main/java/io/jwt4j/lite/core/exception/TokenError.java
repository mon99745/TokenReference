package io.jwt4j.lite.core.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import io.jwt4j.lite.core.exception.common.Error;

@Getter
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public enum TokenError
		implements Error {
	INVALID_KEY_INPUT(TokenError.CODE_PREFIX + "01-00", "잘못된 키 입력입니다.", HttpStatus.BAD_REQUEST),
	INVALID_TOKEN(TokenError.CODE_PREFIX + "01-01", "토큰이 위변조 되었습니다.", HttpStatus.BAD_REQUEST),
	MISSING_KEY(TokenError.CODE_PREFIX + "01-02", "필수 키 값이 누락되었습니다.", HttpStatus.BAD_REQUEST),
	MISSING_CLAIM(TokenError.CODE_PREFIX + "01-03", "필수 JWT Claim이 누락되었습니다.", HttpStatus.BAD_REQUEST),
	INVALID_CLAIM_FORMAT(TokenError.CODE_PREFIX + "01-04", "토큰 구조가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
	KEY_ENCODING_FAILED(TokenError.CODE_PREFIX + "02-01", "키 인코딩 중 시스템 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	BASE58_ENCODING_FAILED(TokenError.CODE_PREFIX + "02-02", "Base58 인코딩 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	JWT_CREATION_FAILED(TokenError.CODE_PREFIX + "02-03", "JWT 생성 중 시스템 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	FAILED_ENCRYPT(TokenError.CODE_PREFIX + "02-03", "Failed to encrypt", HttpStatus.INTERNAL_SERVER_ERROR),
	RSA_ALGORITHM_NOT_FOUND(TokenError.CODE_PREFIX + "01-01", "RSA 알고리즘을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
	INVALID_PRIVATE_KEY_SPEC(TokenError.CODE_PREFIX + "01-02", "잘못된 Private Key 형식입니다.", HttpStatus.BAD_REQUEST),
	INVALID_PRIVATE_KEY(TokenError.CODE_PREFIX + "01-03", "Private Key가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
	INVALID_PUBLIC_KEY_SPEC(TokenError.CODE_PREFIX + "01-04", "잘못된 Public Key 형식입니다.", HttpStatus.BAD_REQUEST),
	INVALID_PUBLIC_KEY(TokenError.CODE_PREFIX + "01-05", "Public Key가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
	INVALID_PADDING(TokenError.CODE_PREFIX + "01-06", "RSA 패딩 설정이 잘못되었습니다.", HttpStatus.BAD_REQUEST),
	ENCRYPTION_FAILED(TokenError.CODE_PREFIX + "01-07", "암호화 처리 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
	DECRYPTION_FAILED(TokenError.CODE_PREFIX + "01-08", "복호화 처리 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
	UNKNOWN_ENCRYPTION_ERROR(TokenError.CODE_PREFIX + "01-09", "Private 키 암호화 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	UNKNOWN_DECRYPTION_ERROR(TokenError.CODE_PREFIX + "01-10", "Public 키 복호화 중 알 수 없는 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	EXPIRED_TOKEN(TokenError.CODE_PREFIX + "01-11", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
	INVALID_CLAIM_TIME_FORMAT(TokenError.CODE_PREFIX + "01-12", "Claim 시간 형식이 올바르지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_ENCRYPTED_TEXT(TokenError.CODE_PREFIX + "02-05", "암호문이 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
	KEY_GENERATION_FAILED(TokenError.CODE_PREFIX + "02-08", "RSA 키 생성에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	PRIVATE_KEY_LOAD_FAILED(TokenError.CODE_PREFIX + "02-09", "Private Key 로딩에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	PUBLIC_KEY_LOAD_FAILED(TokenError.CODE_PREFIX + "02-10", "Public Key 로딩에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
	INVALID_KEY_FORMAT(TokenError.CODE_PREFIX + "01-05", "키 값의 인코딩 형식이 잘못되었습니다.", HttpStatus.BAD_REQUEST),

	;

	public static final String CODE_PREFIX = "TK-";

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

	@Override
	public String toString() {
		return toCodeString();
	}
}
