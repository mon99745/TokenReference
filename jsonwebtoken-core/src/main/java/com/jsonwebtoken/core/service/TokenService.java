package com.jsonwebtoken.core.service;

import com.jsonwebtoken.core.config.VerifyProperties;
import com.jsonwebtoken.core.config.RsaKeyGenerator;
import com.jsonwebtoken.core.exception.TokenError;
import com.jsonwebtoken.core.exception.TokenException;
import com.jsonwebtoken.core.model.dto.reponse.CreateTokenResponse;
import com.jsonwebtoken.core.model.dto.reponse.ExtractClaimResponse;
import com.jsonwebtoken.core.model.dto.reponse.VerifyTokenResponse;
import com.jsonwebtoken.core.model.dto.Claims;
import com.jsonwebtoken.core.model.dto.Token;
import com.jsonwebtoken.core.util.ByteUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsonwebtoken.core.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {
	protected final RsaKeyGenerator rsaKeyGenerator;
	protected final VerifyProperties verifyProperties;
	protected final KeyPairService keyPairService;

	/**
	 * Re-entry after Claim initialization
	 *
	 * @param requestClaim to include in JWT
	 * @return CreateTokenResponse
	 */
	public CreateTokenResponse createJwt(Map<String, String> requestClaim) {
		if (requestClaim == null || requestClaim.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}
		try {
			return this.createJwt(setClaims(requestClaim));
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
		if (claims == null) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}
		try {
			/** Header 생성 */
			String header = createHeader();
			log.info("header = {}, header byte = {}", header, header.getBytes().length);

			/** Payload 생성 */
			String payload = createPayload(claims);
			log.info("payload = {}, payload byte = {}", payload, payload.getBytes().length);

			/** VerifyCode 생성 */
			String verifyCode = setVerifyCode(header, payload);
			log.info("verifyCode = {}, verifyCode byte = {}", verifyCode, verifyCode.getBytes().length);

			/** Signature 생성 */
			String privateKey = keyPairService.getPrivateKey();
			String signature = createSignature(verifyCode, privateKey);
			log.info("signature = {}, signature byte = {}", signature, signature.getBytes().length);

			/** Json Web Token 생성 */
			String jwt = combineToken(header, String.join("", payload), signature);
			log.info("jwt = {}, jwt byte = {}", jwt, jwt.getBytes().length);

			return CreateTokenResponse.builder()
					.resultMsg("Success")
					.resultCode(String.valueOf(HttpStatus.OK.value()))
					.claims(claims.getPublicClaims())
					.jwt(jwt)
					.build();

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}


	/**
	 * Json Web Token 검증
	 *
	 * @param token 검증 대상 토큰
	 * @return VerifyTokenResponse
	 */
	public VerifyTokenResponse verifyJwt(String token) {
		if (token == null || token.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			/** 토큰 구조 분류 */
			Token tokenObject = parseToken(token);

			/** VerifyCode 생성 */
			String newVerifyCode = setVerifyCode(tokenObject.getHeader(), tokenObject.getPayload());
			log.info("verifyCode = {}, verifyCode byte = {}", newVerifyCode, newVerifyCode.getBytes().length);

			/** 서명 검증(비대칭키 복호화) */
			String publicKey = keyPairService.getPublicKey();
			String signedVerifyCode = rsaKeyGenerator.decryptPubRSA(tokenObject.getSignature(), publicKey);

			/** 위변조 검증(해시 비교) */
			if (!newVerifyCode.equals(signedVerifyCode)) {
				throw new TokenException(TokenError.INVALID_KEY_INPUT,
						new IllegalArgumentException("토큰이 위변조 되었습니다."));
			}

			return VerifyTokenResponse.builder()
					.resultMsg("Success")
					.resultCode(String.valueOf(HttpStatus.OK.value()))
					.jwt(token)
					.build();

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException |
				 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
				 InvalidKeyException e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	public ExtractClaimResponse extractClaimToJwt(String token) {
		if (token == null || token.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			/** 토큰 구조 분류 */
			Token tokenObject = parseToken(token);

			/** 서명 검증(비대칭키 복호화) */
			String publicKey = keyPairService.getPublicKey();
			rsaKeyGenerator.decryptPubRSA(tokenObject.getSignature(), publicKey);

			/** 클레임 조회 */
			Object claims = readClaim(tokenObject.getPayload());

			return ExtractClaimResponse.builder()
					.resultMsg("Success")
					.resultCode(String.valueOf(HttpStatus.OK.value()))
					.claims(claims)
					.jwt(token)
					.build();

		} catch (IllegalArgumentException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		} catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException |
				 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException |
				 InvalidKeyException e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	protected Claims setClaims(Map<String, String> requestClaim) {
		if (requestClaim == null || requestClaim.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			Claims.RegisteredClaim registeredClaim = Claims.RegisteredClaim.builder()
					.issuer("security.com") // 발급자
					.subject("Json Web Token") // 주제
					.expiration("2025-01-31T23:59:59Z") // 만료 시간 (ISO-8601 형식)
					.issuedAt("2025-01-21T10:00:00Z") // 발급 시간 (ISO-8601 형식)
					.build();

			Claims.PublicClaim publicClaim = Claims.PublicClaim.builder()
					.publicClaim(requestClaim)
					.build();

			return Claims.builder()
					.registeredClaims(registeredClaim)
					.publicClaims(publicClaim)
					.build();
		} catch (Exception e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		}
	}

	protected String createHeader() {
		String typ = verifyProperties.getTyp();
		String alg = verifyProperties.getAlg();

		if (typ == null || typ.isEmpty() || alg == null || alg.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			byte[] byteHeaderData = ByteUtil.stringToBytes(typ + alg);
			return Base58.encode(byteHeaderData);
		} catch (Exception e) {
			throw new TokenException(TokenError.BASE58_ENCODING_FAILED, e);
		}
	}

	protected String createPayload(Claims claims) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String strClaims = objectMapper.writeValueAsString(claims);
			byte[] bytePayloadData = ByteUtil.stringToBytes(strClaims);
			return Base58.encode(bytePayloadData);
		} catch (Exception e) {
			throw new TokenException(TokenError.BASE58_ENCODING_FAILED, e);
		}
	}

	protected String setVerifyCode(String header, String payload) {
		if (header == null || payload == null) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			return HashUtil.sha256(header + payload);
		} catch (Exception e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	protected String createSignature(String verifyCode, String privateKey) {
		if (verifyCode == null || privateKey == null) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			return rsaKeyGenerator.encryptPrvRSA(verifyCode, privateKey);
		} catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
				 InvalidKeySpecException | BadPaddingException | InvalidKeyException e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	protected String combineToken(String header, String payload, String signature) {
		if (header == null || payload == null || signature == null) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}
		return header + "." + payload + "." + signature;
	}

	protected Object readClaim(String payload) {
		if (payload == null || payload.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		ObjectMapper objectMapper = new ObjectMapper();
		byte[] decodedBytes;
		try {
			decodedBytes = Base58.decode(payload);
			String strClaim = ByteUtil.bytesToUtfString(decodedBytes);
			return objectMapper.readTree(strClaim);
		} catch (IOException e) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
		} catch (Exception e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	protected Token parseToken(String token) {
		if (token == null || token.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		String[] splitArray = token.split("\\.");
		if (splitArray.length != 3) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT,
					new IllegalArgumentException("토큰 구조가 올바르지 않습니다."));
		}

		return new Token(splitArray[0], splitArray[1], splitArray[2]);
	}
}