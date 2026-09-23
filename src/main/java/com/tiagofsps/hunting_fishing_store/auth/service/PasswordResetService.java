package com.tiagofsps.hunting_fishing_store.auth.service;

import java.security.SecureRandom;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tiagofsps.hunting_fishing_store.auth.entity.PasswordResetToken;
import com.tiagofsps.hunting_fishing_store.auth.repository.PasswordResetTokenRepository;
import com.tiagofsps.hunting_fishing_store.auth.repository.RefreshTokenRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidPasswordResetTokenException;
import com.tiagofsps.hunting_fishing_store.common.exception.UserNotFoundException;
import com.tiagofsps.hunting_fishing_store.common.service.MailService;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;

@Service
public class PasswordResetService {

    private static final long TOKEN_TTL_SECONDS = 15 * 60L;

    private final SecureRandom secureRandom = new SecureRandom();

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final String resetBaseUrl;

    public PasswordResetService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            RefreshTokenRepository refreshTokenRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            MailService mailService,
            @Value("${app.password-reset.base-url:http://localhost:5173/reset-password}") String resetBaseUrl) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.resetBaseUrl = resetBaseUrl;
    }

    /**
     * Request a password reset. Always succeeds from the caller's point of
     * view (no account enumeration): a token is only created and emailed when
     * the account actually exists.
     */
    public void requestReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = generateToken();
            passwordResetTokenRepository.save(new PasswordResetToken(token, user.getEmail(), TOKEN_TTL_SECONDS));
            mailService.send(user.getEmail(), "Password reset",
                    "Hello " + user.getFirstName() + ",\n\n"
                            + "We received a request to reset your password. Use the link below:\n\n"
                            + resetBaseUrl + "/" + token + "\n\n"
                            + "This link expires in 15 minutes. If you did not request it, ignore this email.");
        });
    }

    /**
     * Validate a reset token and set a new password for the linked account.
     * Revokes the user's refresh token(s) on a best-effort basis so existing
     * sessions must log in again.
     */
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findById(token)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        User user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new UserNotFoundException(resetToken.getEmail()));

        // Single-use: delete first so a replayed request cannot succeed twice.
        passwordResetTokenRepository.deleteById(token);

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        revokeRefreshTokensFor(resetToken.getEmail());
    }

    private void revokeRefreshTokensFor(String email) {
        // Best-effort: invalidate the stored refresh token so existing
        // sessions must log in again. Only the first matching token is
        // revoked per request (multiple-token-per-user revocation is a
        // documented follow-up).
        refreshTokenRepository.findByUsername(email)
                .ifPresent(token -> refreshTokenRepository.deleteById(token.getId()));
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}