package io.jwt4j.lite.core.exception.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 공통 모듈용 글로벌 예외 처리
 */
@RestControllerAdvice
public class CommonExceptionHandler {

	// 공통 서브 모듈 예외 처리
	@ExceptionHandler(DefaultException.class)
	public ResponseEntity<Map<String, Object>> handleDefaultException(DefaultException ex,
																	  HttpServletRequest request) {

		Map<String, Object> body = new HashMap<>();
		body.put("code", ex.getError() != null ? ex.getError().toCodeString() : "DEF-UNKNOWN");
		body.put("message", ex.getMessage());
		body.put("path", request.getRequestURI());

		// 상태코드는 Error 객체에서 가져오도록 구현 가능, 없으면 400
		return ResponseEntity.status(400).body(body);
	}

	// 기타 일반 예외 처리
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex,
																	  HttpServletRequest request) {

		Map<String, Object> body = new HashMap<>();
		body.put("code", "INTERNAL_ERROR");
		body.put("message", ex.getMessage());
		body.put("path", request.getRequestURI());

		return ResponseEntity.status(500).body(body);
	}
}