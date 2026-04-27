package io.jwt4j.lite.core.exception.common;

import org.springframework.http.HttpStatus;
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
	/**
	 * 공통 서브 모듈 예외 처리
	 *
	 * @param ex
	 * @param request
	 * @return
	 */
	@ExceptionHandler(DefaultException.class)
	public ResponseEntity<Map<String, Object>> handleDefaultException(DefaultException ex,
																	  HttpServletRequest request) {

		Map<String, Object> body = new HashMap<>();
		body.put("code", ex.getError() != null ? ex.getError().getCode() : "DEF-UNKNOWN");
		body.put("message", ex.getMessage());
		body.put("path", request.getRequestURI());

		HttpStatus status = ex.getError() != null ? ex.getError().getHttpStatus() : HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status).body(body);
	}

	/**
	 * 기타 일반 예외 처리
	 *
	 * @param ex
	 * @param request
	 * @return
	 */
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