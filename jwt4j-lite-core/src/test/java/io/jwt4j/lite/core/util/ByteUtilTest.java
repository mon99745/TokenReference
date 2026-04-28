package io.jwt4j.lite.core.util;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ByteUtilTest {

    @Test
    void stringToBytes_and_bytesToUtfString_roundTrip() throws IOException {
        String original = "hello world";
        byte[] bytes = ByteUtil.stringToBytes(original);
        assertEquals(original, ByteUtil.bytesToUtfString(bytes));
    }

    @Test
    void stringToBytes_emptyString_returnsEmptyArray() throws IOException {
        assertEquals(0, ByteUtil.stringToBytes("").length);
    }

    @Test
    void stringToBytes_unicodeCharacters_preservedAfterRoundTrip() throws IOException {
        // "한글テスト" = Korean/Japanese multi-byte chars
        String unicode = "한글テスト";
        assertEquals(unicode, ByteUtil.bytesToUtfString(ByteUtil.stringToBytes(unicode)));
    }

    @Test
    void bytesToHexString_knownByteArray() {
        byte[] bytes = {(byte) 0xFF, (byte) 0x00, (byte) 0xAB};
        assertEquals("ff00ab", ByteUtil.bytesToHexString(bytes).toString());
    }

    @Test
    void bytesToHexString_singleBytePaddedToTwoChars() {
        byte[] bytes = {(byte) 0x0F};
        assertEquals("0f", ByteUtil.bytesToHexString(bytes).toString());
    }

    @Test
    void objectToBytes_and_bytesToObject_roundTrip() throws IOException, ClassNotFoundException {
        String original = "serializable-value";
        byte[] bytes = ByteUtil.objectToBytes(original);
        assertEquals(original, ByteUtil.bytesToObject(bytes));
    }

    @Test
    void stringToBytes_specialCharacters() throws IOException {
        String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        assertEquals(special, ByteUtil.bytesToUtfString(ByteUtil.stringToBytes(special)));
    }
}
