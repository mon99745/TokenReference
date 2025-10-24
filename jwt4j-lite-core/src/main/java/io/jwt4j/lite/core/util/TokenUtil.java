package io.jwt4j.lite.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jwt4j.lite.core.config.RsaKeyGenerator;
import io.jwt4j.lite.core.exception.TokenError;
import io.jwt4j.lite.core.exception.TokenException;
import io.jwt4j.lite.core.model.dto.Token;
import io.jwt4j.lite.core.model.dto.Claims;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.core.Base58;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
public class TokenUtil {
	protected final RsaKeyGenerator rsaKeyGenerator;
	public static void validateNotEmpty(Object obj, TokenError error) {
		if (obj == null || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())
				|| (obj instanceof String && ((String) obj).isEmpty())) {
			throw new TokenException(error);
		}
	}
	public static Claims setClaims(Map<String, String> requestClaim) {
		if (requestClaim == null || requestClaim.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			Claims.RegisteredClaim registeredClaim = Claims.RegisteredClaim.builder()
					.issuer("io-jwt4j-lite") // 발급자
					.subject("jsonwebtoken") // 주제
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

	public static String createHeader(String typ, String alg) {
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

	public static String createPayload(Claims claims) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			String strClaims = objectMapper.writeValueAsString(claims);
			byte[] bytePayloadData = ByteUtil.stringToBytes(strClaims);
			return Base58.encode(bytePayloadData);
		} catch (Exception e) {
			throw new TokenException(TokenError.BASE58_ENCODING_FAILED, e);
		}
	}

	public static String setVerifyCode(String header, String payload) {
		if (header == null || payload == null) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		try {
			return HashUtil.sha256(header + payload);
		} catch (Exception e) {
			throw new TokenException(TokenError.JWT_CREATION_FAILED, e);
		}
	}

	public static String combineToken(String header, String payload, String signature) {
		if (header == null || payload == null || signature == null) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}
		return header + "." + payload + "." + signature;
	}

	public static Object readClaim(String payload) {
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

	public static Token parseToken(String token) {
		if (token == null || token.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}

		String[] splitArray = token.split("\\.");
		if (splitArray.length != 3) {
			throw new TokenException(TokenError.INVALID_CLAIM_FORMAT);
		}

		return new Token(splitArray[0], splitArray[1], splitArray[2]);
	}
}
