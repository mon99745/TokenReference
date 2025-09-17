package com.jsonwebtoken.app.controller;

import com.jsonwebtoken.core.model.dto.reponse.CreateTokenResponse;
import com.jsonwebtoken.core.model.dto.reponse.ExtractClaimResponse;
import com.jsonwebtoken.core.model.dto.reponse.VerifyTokenResponse;
import com.jsonwebtoken.core.service.TokenService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = TokenRestController.TAG)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(TokenRestController.PATH)
public class TokenRestController {
	public static final String TAG = "JWT Manager API";
	public static final String PATH = "/api/v1";
	private static final String JWT_FIELD_NAME = "jwt";
	protected final TokenService tokenService;


	/**
	 * 1. 토큰 발행
	 *
	 * @param requestClaim to include in JWT
	 * @return CreateTokenResponse
	 */
	@PostMapping("createToken")
	@Operation(summary = "1. 토큰(JWT) 발행")
	public CreateTokenResponse createToken(@RequestBody Map<String, String> requestClaim) {
		log.info("Request Claim : ", requestClaim);

		return tokenService.createJwt(requestClaim);
	}

	/**
	 * 2. 토큰 검증
	 *
	 * @param request Request with JWT
	 * @return VerifyTokenResponse
	 */
	@PostMapping("verifyToken")
	@Operation(summary = "2. 토큰(JWT) 검증")
	public VerifyTokenResponse verifyToken(@RequestBody Map<String, String> request) {
		log.info("Request JWT : " + request.get(JWT_FIELD_NAME));

		return tokenService.verifyJwt(request.get(JWT_FIELD_NAME));
	}

	/**
	 * 3. 토큰 정보 추출
	 *
	 * @param request Request with JWT
	 * @return ExtractClaimResponse
	 */
	@PostMapping("extractClaim")
	@Operation(summary = "3. 토큰(JWT)에서 클레임 추출")
	public ExtractClaimResponse extractClaimToJwt(@RequestBody Map<String, String> request) {
		log.info("Request JWT : " + request.get(JWT_FIELD_NAME));

		return tokenService.extractClaimToJwt(request.get(JWT_FIELD_NAME));
	}
}