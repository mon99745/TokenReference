package com.jsonwebtoken.core.service;

import com.jsonwebtoken.core.config.RsaKeyGenerator;
import com.jsonwebtoken.core.config.TokenProperties;
import com.jsonwebtoken.core.exception.TokenException;
import com.jsonwebtoken.core.model.dto.Token;
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
		assertThrows(TokenException.class, () -> tokenService.setClaims(null));
	}

	@Test
	public void testSetClaims_ValidInput() {
		Map<String, String> claimsMap = new HashMap<>();
		claimsMap.put("user", "test");
		assertNotNull(tokenService.setClaims(claimsMap));
	}

	// -------- createHeader --------
	@Test
	public void testCreateHeader_MissingProperties() {
		when(tokenProperties.getTyp()).thenReturn(null);
		when(tokenProperties.getAlg()).thenReturn("HS256");
		assertThrows(TokenException.class, () -> tokenService.createHeader());
	}

	@Test
	public void testCreateHeader_ValidProperties() {
		when(tokenProperties.getTyp()).thenReturn("JWT");
		when(tokenProperties.getAlg()).thenReturn("HS256");
		String header = tokenService.createHeader();
		assertNotNull(header);
	}

	// -------- createPayload --------
	@Test
	public void testCreatePayload_ValidClaims() {
		Map<String, String> claimsMap = new HashMap<>();
		claimsMap.put("user", "test");
		assertNotNull(tokenService.createPayload(tokenService.setClaims(claimsMap)));
	}

	// -------- setVerifyCode --------
	@Test
	public void testSetVerifyCode_NullInput() {
		assertThrows(TokenException.class, () -> tokenService.setVerifyCode(null, "payload"));
	}

	@Test
	public void testSetVerifyCode_ValidInput() {
		String code = tokenService.setVerifyCode("header", "payload");
		assertNotNull(code);
	}

	// -------- createSignature --------
	@Test
	public void testCreateSignature_NullInput() {
		assertThrows(TokenException.class, () -> tokenService.createSignature(null, "privateKey"));
	}

	@Test
	public void testCreateSignature_ValidInput() throws Exception {
		when(rsaKeyGenerator.encryptPrvRSA("verifyCode", "privateKey")).thenReturn("signature");
		String sig = tokenService.createSignature("verifyCode", "privateKey");
		assertEquals("signature", sig);
	}

	// -------- combineToken --------
	@Test
	public void testCombineToken_NullInput() {
		assertThrows(TokenException.class, () -> tokenService.combineToken("header", null, "sig"));
	}

	@Test
	public void testCombineToken_ValidInput() {
		String token = tokenService.combineToken("h", "p", "s");
		assertEquals("h.p.s", token);
	}

	// -------- readClaim --------
	@Test
	public void testReadClaim_NullInput() {
		assertThrows(TokenException.class, () -> tokenService.readClaim(null));
	}

	@Test
	public void testReadClaim_ValidInput() {
		Map<String, String> claimsMap = new HashMap<>();
		claimsMap.put("user", "test");
		String payload = tokenService.createPayload(tokenService.setClaims(claimsMap));
		assertNotNull(tokenService.readClaim(payload));
	}

	// -------- parseToken --------
	@Test
	public void testParseToken_NullInput() {
		assertThrows(TokenException.class, () -> tokenService.parseToken(null));
	}

	@Test
	public void testParseToken_InvalidStructure() {
		assertThrows(TokenException.class, () -> tokenService.parseToken("abc.def"));
	}

	@Test
	public void testParseToken_ValidToken() {
		Token token = tokenService.parseToken("h.p.s");
		assertEquals("h", token.getHeader());
		assertEquals("p", token.getPayload());
		assertEquals("s", token.getSignature());
	}
}