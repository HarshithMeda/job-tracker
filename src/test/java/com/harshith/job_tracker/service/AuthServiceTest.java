package com.harshith.job_tracker.service;

import com.harshith.job_tracker.dto.AuthResponse;
import com.harshith.job_tracker.dto.LoginRequest;
import com.harshith.job_tracker.dto.RegisterRequest;
import com.harshith.job_tracker.exception.DuplicateResourceException;
import com.harshith.job_tracker.exception.InvalidCredentialsException;
import com.harshith.job_tracker.model.AppUser;
import com.harshith.job_tracker.repository.UserRepository;
import com.harshith.job_tracker.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_hashesPasswordAndLowercasesEmail() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("StrongPass123")).thenReturn("hashed-value");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser saved = authService.register(new RegisterRequest("Test@Example.com", "StrongPass123"));

        assertEquals("test@example.com", saved.getEmail());
        assertEquals("hashed-value", saved.getPasswordHash());
        assertNotEquals("StrongPass123", saved.getPasswordHash());
    }

    @Test
    void register_throwsDuplicate_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.register(new RegisterRequest("test@example.com", "StrongPass123")));

        verify(userRepository, never()).save(any(AppUser.class));
    }

    @Test
    void login_returnsToken_whenPasswordMatches() {
        AppUser user = new AppUser();
        user.setEmail("a@b.com");
        user.setPasswordHash("stored-hash");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pw", "stored-hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token123");
        when(jwtService.getExpirationSeconds()).thenReturn(3600L);

        AuthResponse response = authService.login(new LoginRequest("a@b.com", "pw"));

        assertEquals("token123", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresInSeconds());
    }

    @Test
    void login_throwsInvalidCredentials_whenPasswordIsWrong() {
        AppUser user = new AppUser();
        user.setEmail("a@b.com");
        user.setPasswordHash("stored-hash");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "stored-hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("a@b.com", "wrong")));

        verify(jwtService, never()).generateToken(any(AppUser.class));
    }

    @Test
    void login_throwsInvalidCredentials_whenEmailIsUnknown() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest("nobody@example.com", "pw")));
    }
}
