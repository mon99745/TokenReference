package com.example.demo.exception;

public class CommonException extends RuntimeException{
	public CommonException(String message) {
		this(message, null);
	}

	public CommonException(Throwable cause) {
		this((String) null, cause);
	}

	public CommonException(String message, Throwable cause) {
		this(null, message, cause);
	}

	public CommonException(Error error) {
		this(error, (String) null);
	}

	public CommonException(Error error, String message) {
		this(error, message, null);
	}

	public CommonException(Error error, Throwable cause) {
		this(error, null, cause);
	}

	public CommonException(Error error, String message, Throwable cause) {
		super();
	}
}
