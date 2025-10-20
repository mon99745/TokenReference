package com.jsonwebtoken.core.service;

import com.jsonwebtoken.core.config.TokenProperties;
import com.jsonwebtoken.core.config.RsaKeyGenerator;
import com.jsonwebtoken.core.exception.TokenError;
import com.jsonwebtoken.core.exception.TokenException;
import com.jsonwebtoken.core.model.dto.reponse.CreateTokenResponse;
import com.jsonwebtoken.core.model.dto.reponse.ExtractClaimResponse;
import com.jsonwebtoken.core.model.dto.reponse.VerifyTokenResponse;
import com.jsonwebtoken.core.model.dto.Claims;
import com.jsonwebtoken.core.model.dto.Token;
import com.jsonwebtoken.core.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {
	protected final RsaKeyGenerator rsaKeyGenerator;
	protected final TokenProperties tokenProperties;
	protected final KeyPairService keyPairService;

	/**
	 * Re-entry after Claim initialization
	 *
	 * @param requestClaim to include in JWT
	 * @return CreateTokenResponse
	 */
	public CreateTokenResponse createJwt(Map<String, String> requestClaim) {
		TokenUtil.validateNotEmpty(requestClaim, TokenError.MISSING_CLAIM);
		try {
			return this.createJwt(TokenUtil.setClaims(requestClaim));
		} catch (TokenException e) {
			throw e;
		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		} catch (Exception e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	/**
	 * Json Web Token 생성
	 *
	 * @param claims 인가 필수 정보
	 * @return CreateTokenResponse
	 */
	public CreateTokenResponse createJwt(Claims claims) {
		TokenUtil.validateNotEmpty(claims, TokenError.MISSING_CLAIM);
		try {
			/** Header 생성 */
			String header = TokenUtil.createHeader(tokenProperties.getTyp(), tokenProperties.getAlg());
			log.debug("header = {}, header byte = {}", header, header.getBytes().length);

			/** Payload 생성 */
			String payload = TokenUtil.createPayload(claims);
			log.debug("payload = {}, payload byte = {}", payload, payload.getBytes().length);

			/** VerifyCode 생성 */
			String verifyCode = TokenUtil.setVerifyCode(header, payload);
			log.debug("verifyCode = {}, verifyCode byte = {}", verifyCode, verifyCode.getBytes().length);

			/** Signature 생성 */
			String privateKey = keyPairService.getPrivateKey();
			String signature = rsaKeyGenerator.encryptPrvRSA(verifyCode, privateKey);
			log.debug("signature = {}, signature byte = {}", signature, signature.getBytes().length);

			/** Json Web Token 결합 */
			String jwt = TokenUtil.combineToken(header, String.join("", payload), signature);
			log.debug("jwt = {}, jwt byte = {}", jwt, jwt.getBytes().length);

			return CreateTokenResponse.builder()
					.resultMsg("Success")
					.resultCode(String.valueOf(HttpStatus.OK.value()))
					.claims(claims.getPublicClaims())
					.jwt(jwt)
					.build();

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		}
	}


	/**
	 * Json Web Token 검증
	 *
	 * @param token 검증 대상 토큰
	 * @return VerifyTokenResponse
	 */
	public VerifyTokenResponse verifyJwt(String token) {
		TokenUtil.validateNotEmpty(token, TokenError.MISSING_CLAIM);
		try {
			/** 토큰 구조 분류 */
			Token tokenObject = TokenUtil.parseToken(token);

			/** VerifyCode 생성 */
			String newVerifyCode = TokenUtil.setVerifyCode(tokenObject.getHeader(), tokenObject.getPayload());
			log.info("verifyCode = {}, verifyCode byte = {}", newVerifyCode, newVerifyCode.getBytes().length);

			/** 서명 검증(비대칭키 복호화) */
			String publicKey = keyPairService.getPublicKey();
			String signedVerifyCode = rsaKeyGenerator.decryptPubRSA(tokenObject.getSignature(), publicKey);

			/** 위변조 검증(해시 비교) */
			if (!MessageDigest.isEqual(newVerifyCode.getBytes(), signedVerifyCode.getBytes())) {
				throw new TokenException(TokenError.INVALID_TOKEN);
			}

			return VerifyTokenResponse.builder()
					.resultMsg("Success")
					.resultCode(String.valueOf(HttpStatus.OK.value()))
					.jwt(token)
					.build();

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		}
	}

	public ExtractClaimResponse extractClaimToJwt(String token) {
		TokenUtil.validateNotEmpty(token, TokenError.MISSING_CLAIM);
		try {
			/** 토큰 구조 분류 */
			Token tokenObject = TokenUtil.parseToken(token);

			/** 서명 검증(비대칭키 복호화) */
			String publicKey = keyPairService.getPublicKey();
			rsaKeyGenerator.decryptPubRSA(tokenObject.getSignature(), publicKey);

			/** 클레임 조회 */
			Object claims = TokenUtil.readClaim(tokenObject.getPayload());

			return ExtractClaimResponse.builder()
					.resultMsg("Success")
					.resultCode(String.valueOf(HttpStatus.OK.value()))
					.claims(claims)
					.jwt(token)
					.build();

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		}
	}
}