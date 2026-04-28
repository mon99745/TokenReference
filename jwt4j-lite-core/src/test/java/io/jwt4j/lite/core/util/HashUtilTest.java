package io.jwt4j.lite.core.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashUtilTest {

    @Test
    void sha256_knownValue() {
        String result = HashUtil.sha256("hello");
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", result);
    }

    @Test
    void sha256_emptyString() {
        String result = HashUtil.sha256("");
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", result);
    }

    @Test
    void sha256_outputIs64HexChars() {
        assertEquals(64, HashUtil.sha256("any-input").length());
    }

    @Test
    void sha256_sameInputProducesSameOutput() {
        assertEquals(HashUtil.sha256("repeat-me"), HashUtil.sha256("repeat-me"));
    }

    @Test
    void sha256_differentInputsProduceDifferentOutputs() {
        assertNotEquals(HashUtil.sha256("alpha"), HashUtil.sha256("beta"));
    }

    @Test
    void sha256_nullInput_throwsException() {
        assertThrows(NullPointerException.class, () -> HashUtil.sha256(null));
    }
}
