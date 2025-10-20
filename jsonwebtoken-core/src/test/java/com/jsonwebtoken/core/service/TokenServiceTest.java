package com.jsonwebtoken.core.service;

import com.jsonwebtoken.core.config.RsaKeyGenerator;
import com.jsonwebtoken.core.config.TokenProperties;
import com.jsonwebtoken.core.exception.TokenException;
import com.jsonwebtoken.core.model.dto.Token;
import com.jsonwebtoken.core.util.TokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenServiceTest {
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

	// -------- createKeyPair --------
	@Test
	public void testCreateKeyPair_NullInput() {
		KeyPairService keyPairService = new KeyPairService(rsaKeyGenerator);
		assertThrows(TokenException.class, () -> keyPairService.createKeyPair(null));
	}

	@Test
	public void testCreateKeyPair_ValidInput() throws Exception {
		KeyPairService keyPairService = new KeyPairService(rsaKeyGenerator);
		Map<String, Object> keyMap = new HashMap<>();
		PublicKey pubKey = mock(PublicKey.class);
		PrivateKey privKey = mock(PrivateKey.class);

		when(pubKey.getEncoded()).thenReturn(new byte[]{1, 2, 3});
		when(privKey.getEncoded()).thenReturn(new byte[]{4, 5, 6});

		keyMap.put("PublicKey", pubKey);
		keyMap.put("PrivateKey", privKey);

		Map<String, Object> result = keyPairService.createKeyPair(keyMap);
		assertNotNull(result.get("publicKey"));
		assertNotNull(result.get("privateKey"));
	}

	// -------- setClaims --------
	@Test
	public void testSetClaims_NullInput() {
		assertThrows(TokenException.class, () -> TokenUtil.setClaims(null));
	}

	@Test
	public void testSetClaims_ValidInput() {
		Map<String, String> claimsMap = new HashMap<>();
		claimsMap.put("user", "test");
		assertNotNull(TokenUtil.setClaims(claimsMap));
	}

	// -------- createHeader --------
	@Test
	public void testCreateHeader_MissingProperties() {
		when(tokenProperties.getTyp()).thenReturn(null);
		when(tokenProperties.getAlg()).thenReturn("HS256");
		assertThrows(TokenException.class, () -> TokenUtil.createHeader(tokenProperties.getTyp(), tokenProperties.getAlg()));
	}

	@Test
	public void testCreateHeader_ValidProperties() {
		when(tokenProperties.getTyp()).thenReturn("JWT");
		when(tokenProperties.getAlg()).thenReturn("HS256");
		String header = TokenUtil.createHeader(tokenProperties.getTyp(), tokenProperties.getAlg());
		assertNotNull(header);
	}

	// -------- createPayload --------
	@Test
	public void testCreatePayload_ValidClaims() {
		Map<String, String> claimsMap = new HashMap<>();
		claimsMap.put("user", "test");
		assertNotNull(TokenUtil.createPayload(TokenUtil.setClaims(claimsMap)));
	}

	// -------- setVerifyCode --------
	@Test
	public void testSetVerifyCode_NullInput() {
		assertThrows(TokenException.class, () -> TokenUtil.setVerifyCode(null, "payload"));
	}

	@Test
	public void testSetVerifyCode_ValidInput() {
		String code = TokenUtil.setVerifyCode("header", "payload");
		assertNotNull(code);
	}


	// -------- combineToken --------
	@Test
	public void testCombineToken_NullInput() {
		assertThrows(TokenException.class, () -> TokenUtil.combineToken("header", null, "sig"));
	}

	@Test
	public void testCombineToken_ValidInput() {
		String token = TokenUtil.combineToken("h", "p", "s");
		assertEquals("h.p.s", token);
	}

	// -------- readClaim --------
	@Test
	public void testReadClaim_NullInput() {
		assertThrows(TokenException.class, () -> TokenUtil.readClaim(null));
	}

	@Test
	public void testReadClaim_ValidInput() {
		Map<String, String> claimsMap = new HashMap<>();
		claimsMap.put("user", "test");
		String payload = TokenUtil.createPayload(TokenUtil.setClaims(claimsMap));
		assertNotNull(TokenUtil.readClaim(payload));
	}

	// -------- parseToken --------
	@Test
	public void testParseToken_NullInput() {
		assertThrows(TokenException.class, () -> TokenUtil.parseToken(null));
	}

	@Test
	public void testParseToken_InvalidStructure() {
		assertThrows(TokenException.class, () -> TokenUtil.parseToken("abc.def"));
	}

	@Test
	public void testParseToken_ValidToken() {
		Token token = TokenUtil.parseToken("h.p.s");
		assertEquals("h", token.getHeader());
		assertEquals("p", token.getPayload());
		assertEquals("s", token.getSignature());
	}
}