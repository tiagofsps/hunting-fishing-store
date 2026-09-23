package com.tiagofsps.hunting_fishing_store.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tiagofsps.hunting_fishing_store.auth.entity.PasswordResetToken;
import com.tiagofsps.hunting_fishing_store.auth.entity.RefreshToken;
import com.tiagofsps.hunting_fishing_store.auth.repository.PasswordResetTokenRepository;
import com.tiagofsps.hunting_fishing_store.auth.repository.RefreshTokenRepository;
import com.tiagofsps.hunting_fishing_store.common.exception.InvalidPasswordResetTokenException;
import com.tiagofsps.hunting_fishing_store.common.service.MailService;
import com.tiagofsps.hunting_fishing_store.user.entity.User;
import com.tiagofsps.hunting_fishing_store.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailService mailService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private PasswordResetService passwordResetService;

    private User user;

    @BeforeEach
    void setup() {
        passwordResetService = new PasswordResetService(
                passwordResetTokenRepository, refreshTokenRepository, userRepository,
                passwordEncoder, mailService, "http://localhost:5173/reset-password");

        user = new User();
        user.setEmail("hunter@example.com");
        user.setFirstName("Hunter");
        user.setPassword("hashed-old");
    }

    @Test
    @DisplayName("Should create a token and send a mail when the user exists")
    void shouldCreateTokenAndSendMailForExistingUser() {
        when(userRepository.findByEmail("hunter@example.com")).thenReturn(Optional.of(user));

        passwordResetService.requestReset("hunter@example.com");

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertEquals("hunter@example.com", captor.getValue().getEmail());
        assertEquals(15 * 60L, captor.getValue().getTtlInSeconds());
        verify(mailService).send(eq("hunter@example.com"), eq("Password reset"), contains("http://localhost:5173/reset-password"));
    }

    @Test
    @DisplayName("Should stay silent (no exception, no email) for an unknown email")
    void shouldStaySilentForUnknownEmail() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> passwordResetService.requestReset("ghost@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
        verify(mailService, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("Should reset the password and revoke the refresh token")
    void shouldResetPasswordAndRevokeRefreshToken() {
        PasswordResetToken resetToken = new PasswordResetToken("abc123", "hunter@example.com", 900L);
        when(passwordResetTokenRepository.findById("abc123")).thenReturn(Optional.of(resetToken));
        when(userRepository.findByEmail("hunter@example.com")).thenReturn(Optional.of(user));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId("refresh-id");
        when(refreshTokenRepository.findByUsername("hunter@example.com")).thenReturn(Optional.of(refreshToken));

        passwordResetService.resetPassword("abc123", "new-secret-password");

        assertTrue(passwordEncoder.matches("new-secret-password", user.getPassword()));
        verify(passwordResetTokenRepository).deleteById("abc123");
        verify(userRepository).save(user);
        verify(refreshTokenRepository).deleteById("refresh-id");
    }

    @Test
    @DisplayName("Should reject an unknown token")
    void shouldRejectUnknownToken() {
        when(passwordResetTokenRepository.findById("bogus")).thenReturn(Optional.empty());

        assertThrows(InvalidPasswordResetTokenException.class,
                () -> passwordResetService.resetPassword("bogus", "new-secret-password"));

        verify(userRepository, never()).save(any());
    }
}