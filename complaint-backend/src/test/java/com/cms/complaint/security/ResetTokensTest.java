package com.cms.complaint.security;

import com.cms.complaint.entity.PasswordResetToken;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResetTokensTest {

    @Test
    void tokensAreRandomAndLongEnough() {
        String a = ResetTokens.generate();
        String b = ResetTokens.generate();
        assertNotEquals(a, b);
        assertEquals(43, a.length());
    }

    @Test
    void hashIsStableAndHidesTheToken() {
        String h = ResetTokens.hash("abc");
        assertEquals(64, h.length());
        assertEquals(h, ResetTokens.hash("abc"));
        assertNotEquals(h, ResetTokens.hash("abd"));
        assertFalse(h.contains("abc"));
    }

    @Test
    void tokenIsUsableOnlyOnceAndBeforeExpiry() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 22, 12, 0);
        PasswordResetToken t = new PasswordResetToken();
        t.setExpiresAt(now.plusMinutes(5));
        assertTrue(t.isUsable(now));
        t.setUsed(true);
        assertFalse(t.isUsable(now));
        t.setUsed(false);
        t.setExpiresAt(now.minusMinutes(1));
        assertFalse(t.isUsable(now));
    }
}
