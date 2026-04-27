package io.jwt4j.lite.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jwt4j.lite.core.exception.TokenException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class TokenUtilIsExpiredClaimTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ObjectNode buildClaimsNode(String expiration) {
        ObjectNode root = MAPPER.createObjectNode();
        ObjectNode registered = MAPPER.createObjectNode();
        registered.put("expiration", expiration);
        root.set("registeredClaims", registered);
        return root;
    }

    @Test
    void isExpiredClaim_futureExpiration_returnsFalse() {
        String future = LocalDateTime.now().plusHours(1)
                .truncatedTo(ChronoUnit.SECONDS).format(FORMATTER);
        assertFalse(TokenUtil.isExpiredClaim(buildClaimsNode(future)));
    }

    @Test
    void isExpiredClaim_pastExpiration_returnsTrue() {
        String past = LocalDateTime.now().minusHours(1)
                .truncatedTo(ChronoUnit.SECONDS).format(FORMATTER);
        assertTrue(TokenUtil.isExpiredClaim(buildClaimsNode(past)));
    }

    @Test
    void isExpiredClaim_epochMillis_futureExpiration_returnsFalse() {
        long future = System.currentTimeMillis() + 3_600_000L;
        assertFalse(TokenUtil.isExpiredClaim(buildClaimsNode(String.valueOf(future))));
    }

    @Test
    void isExpiredClaim_epochMillis_pastExpiration_returnsTrue() {
        long past = System.currentTimeMillis() - 3_600_000L;
        assertTrue(TokenUtil.isExpiredClaim(buildClaimsNode(String.valueOf(past))));
    }

    @Test
    void isExpiredClaim_nonObjectNode_throwsException() {
        assertThrows(TokenException.class, () -> TokenUtil.isExpiredClaim("plain-string"));
    }

    @Test
    void isExpiredClaim_missingRegisteredClaims_throwsException() {
        ObjectNode emptyRoot = MAPPER.createObjectNode();
        assertThrows(TokenException.class, () -> TokenUtil.isExpiredClaim(emptyRoot));
    }

    @Test
    void isExpiredClaim_missingExpirationField_throwsException() {
        ObjectNode root = MAPPER.createObjectNode();
        root.set("registeredClaims", MAPPER.createObjectNode());
        assertThrows(TokenException.class, () -> TokenUtil.isExpiredClaim(root));
    }

    @Test
    void isExpiredClaim_exactCurrentSecond_notExpiredYet() {
        // expiration 1 second in the future should not be expired
        String nearFuture = LocalDateTime.now().plusSeconds(1)
                .truncatedTo(ChronoUnit.SECONDS).format(FORMATTER);
        assertFalse(TokenUtil.isExpiredClaim(buildClaimsNode(nearFuture)));
    }
}
