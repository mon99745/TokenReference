package io.jwt4j.lite.core.exception;

import io.jwt4j.lite.core.exception.common.DefaultException;
import io.jwt4j.lite.core.exception.common.Error;

public class TokenException
		extends DefaultException {

	public TokenException(String message) {
		this(message, null);
	}

	public TokenException(Throwable cause) {
		this((String) null, cause);
	}

	public TokenException(String message, Throwable cause) {
		this(Error.DefaultError.NONE, message, cause);
	}

	public TokenException(Error error) {
		this(error, (String) null);
	}

	public TokenException(Error error, String message) {
		this(error, message, null);
	}

	public TokenException(Error error, Throwable cause) {
		this(error, null, cause);
	}

	/**
	 * Issuer 예외 생성자
	 *
	 * @param error   에러
	 * @param message 메세지
	 * @param cause   원인 예외
	 */
	public TokenException(Error error, String message, Throwable cause) {
		super(error, message, cause);
	}
}
