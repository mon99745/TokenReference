package io.jwt4j.lite.core.service;

import io.jwt4j.lite.core.config.RsaKeyGenerator;
import io.jwt4j.lite.core.exception.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class KeyPairServiceTest {

    @Mock
    RsaKeyGenerator rsaKeyGenerator;

    KeyPairService keyPairService;

    KeyPair realKeyPair;

    @BeforeEach
    void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        keyPairService = new KeyPairService(rsaKeyGenerator);

        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        realKeyPair = gen.generateKeyPair();
    }

    @Test
    void getPrivateKey_returnsNonEmptyBase58String() {
        when(rsaKeyGenerator.getPrivateKey()).thenReturn(realKeyPair.getPrivate());

        String privateKey = keyPairService.getPrivateKey();

        assertNotNull(privateKey);
        assertFalse(privateKey.isEmpty());
    }

    @Test
    void getPublicKey_returnsNonEmptyBase58String() {
        when(rsaKeyGenerator.getPublicKey()).thenReturn(realKeyPair.getPublic());

        String publicKey = keyPairService.getPublicKey();

        assertNotNull(publicKey);
        assertFalse(publicKey.isEmpty());
    }

    @Test
    void createKeyPair_nullInput_throwsMissingKey() {
        assertThrows(TokenException.class, () -> keyPairService.createKeyPair(null));
    }

    @Test
    void createKeyPair_publicKeyNotPublicKeyType_throwsInvalidKeyInput() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("PublicKey", "not-a-PublicKey-object");
        keyMap.put("PrivateKey", realKeyPair.getPrivate());

        assertThrows(TokenException.class, () -> keyPairService.createKeyPair(keyMap));
    }

    @Test
    void createKeyPair_privateKeyNotPrivateKeyType_throwsInvalidKeyInput() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("PublicKey", realKeyPair.getPublic());
        keyMap.put("PrivateKey", "not-a-PrivateKey-object");

        assertThrows(TokenException.class, () -> keyPairService.createKeyPair(keyMap));
    }

    @Test
    void createKeyPair_validInput_returnsBase58EncodedKeys() {
        Map<String, Object> keyMap = new HashMap<>();
        keyMap.put("PublicKey", realKeyPair.getPublic());
        keyMap.put("PrivateKey", realKeyPair.getPrivate());

        Map<String, Object> result = keyPairService.createKeyPair(keyMap);

        assertNotNull(result.get("publicKey"));
        assertNotNull(result.get("privateKey"));
        assertFalse(((String) result.get("publicKey")).isEmpty());
        assertFalse(((String) result.get("privateKey")).isEmpty());
    }

    @Test
    void createKeyPair_emptyMap_throwsInvalidKeyInput() {
        Map<String, Object> emptyMap = new HashMap<>();

        assertThrows(TokenException.class, () -> keyPairService.createKeyPair(emptyMap));
    }
}
