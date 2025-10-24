package io.jwt4j.lite.core.service;

import io.jwt4j.lite.core.config.RsaKeyGenerator;
import io.jwt4j.lite.core.config.TokenProperties;
import io.jwt4j.lite.core.exception.TokenException;
import io.jwt4j.lite.core.model.dto.Token;
import io.jwt4j.lite.core.util.TokenUtil;
import io.jwt4j.lite.core.model.dto.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class TokenServiceIntegrationTest {

	private TokenService tokenService;

	@Mock
	private KeyPairService keyPairService;

	@Mock
	private RsaKeyGenerator rsaKeyGenerator;

	@Mock
	private TokenProperties tokenProperties;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		tokenService = new TokenService(rsaKeyGenerator, tokenProperties, keyPairService);
	}

	@Test
	public void testJwtFullFlow_Success() throws Exception {
		// -------- 준비: Claims --------
		Map<String, String> claimMap = new HashMap<>();
		claimMap.put("userId", "test-user");
		claimMap.put("role", "admin");

		// -------- Header 설정 --------
		when(tokenProperties.getTyp()).thenReturn("JWT");
		when(tokenProperties.getAlg()).thenReturn("HS256");

		// -------- KeyPairService & RSA --------
		when(keyPairService.getPrivateKey()).thenReturn("private-key");
		when(keyPairService.getPublicKey()).thenReturn("public-key");
		when(rsaKeyGenerator.encryptPrvRSA(anyString(), anyString())).thenAnswer(i -> "mock-signature");
		when(rsaKeyGenerator.decryptPubRSA(anyString(), anyString())).thenAnswer(i -> i.getArgument(0));

		// -------- JWT 생성 --------
		Claims claims = TokenUtil.setClaims(claimMap, "test-issuer", "test-subject", 600000L);
		String header = TokenUtil.createHeader(tokenProperties.getTyp(), tokenProperties.getAlg());
		String payload = TokenUtil.createPayload(claims);
		String verifyCode = TokenUtil.setVerifyCode(header, payload);
		String signature = rsaKeyGenerator.encryptPrvRSA(verifyCode, keyPairService.getPrivateKey());
		String jwt = TokenUtil.combineToken(header, payload, signature);

		assertNotNull(jwt);
		assertTrue(jwt.contains("."), () -> "JWT는 점(.)으로 구분된 3부분이어야 함");

		// -------- JWT 파싱 --------
		Token tokenObj = TokenUtil.parseToken(jwt);
		assertEquals(header, tokenObj.getHeader());
		assertEquals(payload, tokenObj.getPayload());
		assertEquals(signature, tokenObj.getSignature());

		// -------- Claim 읽기 --------
		Object extractedClaims = TokenUtil.readClaim(tokenObj.getPayload());
		assertNotNull(extractedClaims);
		assertTrue(extractedClaims.toString().contains("test-user"));
		assertTrue(extractedClaims.toString().contains("admin"));
	}

	@Test
	public void testJwtFullFlow_InvalidToken() {
		// 잘못된 토큰 구조
		String invalidToken = "abc.def";

		TokenException ex = assertThrows(TokenException.class, () -> TokenUtil.parseToken(invalidToken));
		Assertions.assertTrue(ex.getMessage().contains("INVALID_CLAIM_FORMAT"));
	}
}