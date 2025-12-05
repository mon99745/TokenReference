package io.jwt4j.lite.core.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jwt4j.lite.core.config.RsaKeyGenerator;
import io.jwt4j.lite.core.exception.TokenError;
import io.jwt4j.lite.core.exception.TokenException;
import io.jwt4j.lite.core.model.dto.Token;
import io.jwt4j.lite.core.model.dto.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Base58;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class TokenUtil {
	protected final RsaKeyGenerator rsaKeyGenerator;

	public static void validateNotEmpty(Object obj, TokenError error) {
		if (obj == null || (obj instanceof Map && ((Map<?, ?>) obj).isEmpty())
				|| (obj instanceof String && ((String) obj).isEmpty())) {
			throw new TokenException(error);
		}
	}

	public static Claims setClaims(Map<String, String> requestClaim, String iss, String sub, long defaultTtl) {
		if (requestClaim == null || requestClaim.isEmpty()) {
			throw new TokenException(TokenError.MISSING_CLAIM);
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime exp;
		if (requestClaim.get("exp") == null || requestClaim.get("exp").isEmpty()) {
			exp = now.plusSeconds(defaultTtl / 1000);
		} else {
			try {
				long expSeconds = Long.parseLong(requestClaim.get("exp"));
				exp = now.plusSeconds(expSeconds);
			} catch (NumberFormatException e) {
				throw new TokenException(TokenError.INVALID_CLAIM_FORMAT, e);
			}
		}

		try {
			Claims.RegisteredClaim registeredClaim = Claims.RegisteredClaim.builder()
					.issuer(iss) // 발급자
					.subject(sub) // 주제
					.expiration(exp.truncatedTo(ChronoUnit.SECONDS).toString())
					.issuedAt(now.truncatedTo(ChronoUnit.SECONDS).toString())
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

	public static boolean isExpiredClaim(Object claims) {
		try {
			if (!(claims instanceof ObjectNode)) {
				throw new TokenException(TokenError.INVALID_CLAIM_TIME_FORMAT);
			}

			ObjectNode claimsNode = (ObjectNode) claims;
			JsonNode registeredClaims = claimsNode.get("registeredClaims");
			if (registeredClaims == null || registeredClaims.get("expiration") == null) {
				throw new TokenException(TokenError.INVALID_CLAIM_TIME_FORMAT);
			}

			String expStr = registeredClaims.get("expiration").asText();

			// 밀리초 숫자 문자열이면 Instant 사용
			try {
				long expMillis = Long.parseLong(expStr);
				Instant expirationTime = Instant.ofEpochMilli(expMillis);
				return Instant.now().isAfter(expirationTime);
			} catch (NumberFormatException ignored) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
				LocalDateTime expirationTime = LocalDateTime.parse(expStr, formatter);
				return LocalDateTime.now().isAfter(expirationTime);
			}

		} catch (Exception e) {
			if (e instanceof TokenException) throw (TokenException) e;
			throw new TokenException(TokenError.INVALID_CLAIM_TIME_FORMAT, e);
		}
	}
}
