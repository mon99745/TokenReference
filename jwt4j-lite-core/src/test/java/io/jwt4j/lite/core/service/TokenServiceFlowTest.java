package io.jwt4j.lite.core.service;

import io.jwt4j.lite.core.config.RsaKeyGenerator;
import io.jwt4j.lite.core.config.TokenProperties;
import io.jwt4j.lite.core.exception.TokenException;
import io.jwt4j.lite.core.model.dto.reponse.CreateTokenResponse;
import io.jwt4j.lite.core.model.dto.reponse.ExtractClaimResponse;
import io.jwt4j.lite.core.model.dto.reponse.VerifyTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class TokenServiceFlowTest {

    @Mock
    RsaKeyGenerator rsaKeyGenerator;

    @Mock
    TokenProperties tokenProperties;

    @Mock
    KeyPairService keyPairService;

    TokenService tokenService;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(tokenProperties.getTyp()).thenReturn("JWT");
        when(tokenProperties.getAlg()).thenReturn("RSA");
        when(tokenProperties.getIss()).thenReturn("test-issuer");
        when(tokenProperties.getSub()).thenReturn("test-subject");
        when(tokenProperties.getExp()).thenReturn(3_600_000L);

        when(keyPairService.getPrivateKey()).thenReturn("mock-private-key");
        when(keyPairService.getPublicKey()).thenReturn("mock-public-key");

        // Signature = verifyCode (identity) so that verify can confirm it without real RSA
        when(rsaKeyGenerator.encryptPrvRSA(anyString(), anyString()))
                .thenAnswer(i -> i.getArgument(0));
        when(rsaKeyGenerator.decryptPubRSA(anyString(), anyString()))
                .thenAnswer(i -> i.getArgument(0));

        tokenService = new TokenService(rsaKeyGenerator, tokenProperties, keyPairService);
    }

    // -------- createJwt(Map) --------

    @Test
    void createJwt_nullClaim_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.createJwt((Map<String, String>) null));
    }

    @Test
    void createJwt_emptyClaim_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.createJwt(new HashMap<>()));
    }

    @Test
    void createJwt_validClaim_returnsJwtWith3Parts() {
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-001");

        CreateTokenResponse response = tokenService.createJwt(claim);

        assertNotNull(response);
        assertEquals("200", response.getResultCode());
        assertEquals(3, response.getJwt().split("\\.").length);
    }

    @Test
    void createJwt_includesPublicClaims() {
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-abc");
        claim.put("role", "admin");

        CreateTokenResponse response = tokenService.createJwt(claim);

        assertNotNull(response.getClaims());
    }

    // -------- verifyJwt --------

    @Test
    void verifyJwt_nullToken_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.verifyJwt(null));
    }

    @Test
    void verifyJwt_emptyToken_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.verifyJwt(""));
    }

    @Test
    void verifyJwt_twoPartToken_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.verifyJwt("abc.def"));
    }

    @Test
    void verifyJwt_tamperedSignature_throwsInvalidToken() {
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-001");
        CreateTokenResponse created = tokenService.createJwt(claim);

        // Simulate tampered: public-key decryption returns a different hash
        when(rsaKeyGenerator.decryptPubRSA(anyString(), anyString())).thenReturn("tampered-hash");

        assertThrows(TokenException.class, () -> tokenService.verifyJwt(created.getJwt()));
    }

    @Test
    void verifyJwt_expiredToken_throwsExpiredToken() {
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-001");
        claim.put("exp", "-1"); // 1 second in the past

        CreateTokenResponse created = tokenService.createJwt(claim);

        assertThrows(TokenException.class, () -> tokenService.verifyJwt(created.getJwt()));
    }

    @Test
    void verifyJwt_validToken_returnsSuccess() {
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-001");

        CreateTokenResponse created = tokenService.createJwt(claim);
        VerifyTokenResponse verified = tokenService.verifyJwt(created.getJwt());

        assertNotNull(verified);
        assertEquals("200", verified.getResultCode());
        assertEquals(created.getJwt(), verified.getJwt());
    }

    // -------- extractClaimToJwt --------

    @Test
    void extractClaimToJwt_nullToken_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.extractClaimToJwt(null));
    }

    @Test
    void extractClaimToJwt_emptyToken_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.extractClaimToJwt(""));
    }

    @Test
    void extractClaimToJwt_twoPartToken_throwsTokenException() {
        assertThrows(TokenException.class, () -> tokenService.extractClaimToJwt("abc.def"));
    }

    @Test
    void extractClaimToJwt_validToken_returnsClaims() {
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-extract-test");
        claim.put("role", "viewer");

        CreateTokenResponse created = tokenService.createJwt(claim);
        ExtractClaimResponse extracted = tokenService.extractClaimToJwt(created.getJwt());

        assertNotNull(extracted);
        assertEquals("200", extracted.getResultCode());
        assertNotNull(extracted.getClaims());
        assertTrue(extracted.getClaims().toString().contains("user-extract-test"));
    }

    @Test
    void extractClaimToJwt_doesNotCheckExpiration() {
        // extractClaim should succeed even for expired tokens (no expiration check)
        Map<String, String> claim = new HashMap<>();
        claim.put("userId", "user-001");
        claim.put("exp", "-1"); // already expired

        CreateTokenResponse created = tokenService.createJwt(claim);
        ExtractClaimResponse extracted = tokenService.extractClaimToJwt(created.getJwt());

        assertNotNull(extracted);
        assertEquals("200", extracted.getResultCode());
    }
}
