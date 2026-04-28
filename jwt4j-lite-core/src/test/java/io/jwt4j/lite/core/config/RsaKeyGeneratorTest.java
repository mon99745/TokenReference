package io.jwt4j.lite.core.config;

import io.jwt4j.lite.core.exception.TokenException;
import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RsaKeyGeneratorTest {

    @TempDir
    Path tempDir;

    @Mock
    TokenProperties tokenProperties;

    RsaKeyGenerator rsaKeyGenerator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        when(tokenProperties.getPath()).thenReturn(tempDir.toAbsolutePath() + "/");
        when(tokenProperties.getAlg()).thenReturn("RSA");
        when(tokenProperties.getKeySize()).thenReturn(2048);
        rsaKeyGenerator = new RsaKeyGenerator(tokenProperties);
    }

    @Test
    void createKey_returnsPublicAndPrivateKey() {
        Map<String, Object> keyMap = rsaKeyGenerator.createKey();
        assertNotNull(keyMap.get("PublicKey"));
        assertNotNull(keyMap.get("PrivateKey"));
    }

    @Test
    void afterPropertiesSet_createsKeyFiles() {
        rsaKeyGenerator.afterPropertiesSet();
        assertTrue(tempDir.resolve("public.pem").toFile().exists());
        assertTrue(tempDir.resolve("private.pem").toFile().exists());
    }

    @Test
    void afterPropertiesSet_calledTwice_doesNotOverwriteExistingKeys() {
        rsaKeyGenerator.afterPropertiesSet();
        long pubLastModified = tempDir.resolve("public.pem").toFile().lastModified();
        rsaKeyGenerator.afterPropertiesSet();
        assertEquals(pubLastModified, tempDir.resolve("public.pem").toFile().lastModified());
    }

    @Test
    void getPrivateKey_fileSystem_returnsValidKey() {
        rsaKeyGenerator.afterPropertiesSet();
        assertNotNull(rsaKeyGenerator.getPrivateKey());
    }

    @Test
    void getPublicKey_fileSystem_returnsValidKey() {
        rsaKeyGenerator.afterPropertiesSet();
        assertNotNull(rsaKeyGenerator.getPublicKey());
    }

    @Test
    void encryptPrvRSA_and_decryptPubRSA_roundTrip() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair keyPair = gen.generateKeyPair();

        String privateKeyBase58 = Base58.encode(keyPair.getPrivate().getEncoded());
        String publicKeyBase58 = Base58.encode(keyPair.getPublic().getEncoded());

        String plainText = "test-verify-code-12345";
        String encrypted = rsaKeyGenerator.encryptPrvRSA(plainText, privateKeyBase58);
        String decrypted = rsaKeyGenerator.decryptPubRSA(encrypted, publicKeyBase58);

        assertEquals(plainText, decrypted);
    }

    @Test
    void encryptPrvRSA_invalidKey_throwsTokenException() {
        assertThrows(TokenException.class, () ->
                rsaKeyGenerator.encryptPrvRSA("some-text", "not-a-valid-key"));
    }

    @Test
    void decryptPubRSA_invalidBase64_throwsTokenException() {
        assertThrows(TokenException.class, () ->
                rsaKeyGenerator.decryptPubRSA("not!!!valid===base64", "some-key"));
    }

    @Test
    void getPrivateKey_invalidStringArg_throwsTokenException() {
        assertThrows(TokenException.class, () ->
                rsaKeyGenerator.getPrivateKey("not-a-valid-base58-key"));
    }

    @Test
    void getPublicKey_invalidStringArg_throwsTokenException() {
        assertThrows(TokenException.class, () ->
                rsaKeyGenerator.getPublicKey("not-a-valid-base58-key"));
    }
}
