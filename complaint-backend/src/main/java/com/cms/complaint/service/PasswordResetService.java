package com.cms.complaint.service;

import com.cms.complaint.entity.PasswordResetToken;
import com.cms.complaint.entity.User;
import com.cms.complaint.exception.ApiException;
import com.cms.complaint.repository.PasswordResetTokenRepository;
import com.cms.complaint.repository.UserRepository;
import com.cms.complaint.security.ResetTokens;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Forgot password flow. The reset link is written to the server log (no email server needed for the demo).
 * To send real emails later, replace the log line with a JavaMailSender call.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final long tokenMinutes;
    private final String frontendUrl;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens, PasswordEncoder encoder,
                                @Value("${app.reset.token-minutes:30}") long tokenMinutes,
                                @Value("${app.frontend-url:http://127.0.0.1:5500}") String frontendUrl) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.tokenMinutes = tokenMinutes;
        this.frontendUrl = frontendUrl;
    }

    /** Always behaves the same for known and unknown emails, so nobody can discover which emails exist. */
    @Transactional
    public void requestReset(String email) {
        Optional<User> found = users.findByEmail(email.trim().toLowerCase());
        if (found.isEmpty() || !found.get().isActive()) {
            return;
        }
        String token = ResetTokens.generate();
        PasswordResetToken t = new PasswordResetToken();
        t.setUser(found.get());
        t.setTokenHash(ResetTokens.hash(token));
        t.setExpiresAt(LocalDateTime.now().plusMinutes(tokenMinutes));
        tokens.save(t);
        log.info("PASSWORD RESET LINK for {} (valid {} min): {}/reset-password.html?token={}",
                found.get().getEmail(), tokenMinutes, frontendUrl, token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken t = tokens.findByTokenHash(ResetTokens.hash(token.trim()))
                .filter(x -> x.isUsable(LocalDateTime.now()))
                .orElseThrow(() -> ApiException.badRequest("Invalid or expired reset link"));
        User user = t.getUser();
        user.setPassword(encoder.encode(newPassword));
        users.save(user);
        // Every unused link of this user stops working, including the one just used.
        tokens.findByUserIdAndUsedFalse(user.getId()).forEach(x -> x.setUsed(true));
    }
}
