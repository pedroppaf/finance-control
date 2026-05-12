package com.pedro.finance_control;

import com.pedro.finance_control.dto.auth.AuthResponse;
import com.pedro.finance_control.dto.auth.LoginRequest;
import com.pedro.finance_control.dto.auth.RegisterRequest;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.entity.RefreshToken;
import com.pedro.finance_control.repository.UserRepository;
import com.pedro.finance_control.security.JwtService;
import com.pedro.finance_control.service.AuthService;
import com.pedro.finance_control.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Pedro", "pedro@email.com", "123456");

        when(userRepository.existsByEmail("pedro@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken("pedro@email.com")).thenReturn("token-123");
        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(RefreshToken.builder().token("refresh-123").build());

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("token-123", response.token());
        assertEquals("refresh-123", response.refreshToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Pedro", savedUser.getName());
        assertEquals("pedro@email.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals("USER", savedUser.getRole());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void shouldThrowWhenEmailAlreadyExistsOnRegister() {
        RegisterRequest request = new RegisterRequest("Pedro", "pedro@email.com", "123456");

        when(userRepository.existsByEmail("pedro@email.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("pedro@email.com", "123456");

        User user = new User();
        user.setEmail("pedro@email.com");
        user.setPassword("encoded-password");
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken("pedro@email.com")).thenReturn("token-456");
        when(refreshTokenService.createRefreshToken(any(User.class)))
                .thenReturn(RefreshToken.builder().token("refresh-456").build());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token-456", response.token());
        assertEquals("refresh-456", response.refreshToken());
        verify(jwtService).generateToken("pedro@email.com");
    }

    @Test
    void shouldThrowWhenEmailDoesNotExistOnLogin() {
        LoginRequest request = new LoginRequest("pedro@email.com", "123456");

        when(userRepository.findByEmail("pedro@email.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldThrowWhenPasswordIsInvalidOnLogin() {
        LoginRequest request = new LoginRequest("pedro@email.com", "wrong-password");

        User user = new User();
        user.setEmail("pedro@email.com");
        user.setPassword("encoded-password");
        user.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(request));

        assertEquals("Invalid email or password", exception.getMessage());
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    void shouldRefreshSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setEmail("pedro@email.com");

        RefreshToken current = RefreshToken.builder()
                .id(10L)
                .token("old-refresh")
                .user(user)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(refreshTokenService.findByToken("old-refresh")).thenReturn(Optional.of(current));
        when(refreshTokenService.isExpired(current)).thenReturn(false);
        when(refreshTokenService.createRefreshToken(user))
                .thenReturn(RefreshToken.builder().token("new-refresh").user(user).build());
        when(jwtService.generateToken("pedro@email.com")).thenReturn("new-access");

        AuthResponse response = authService.refresh("old-refresh");

        assertEquals("new-access", response.token());
        assertEquals("new-refresh", response.refreshToken());
        verify(refreshTokenService).revoke(current);
    }

    @Test
    void shouldLogoutSuccessfully() {
        User user = new User();
        user.setId(1L);
        user.setEmail("pedro@email.com");

        RefreshToken token = RefreshToken.builder()
                .token("logout-refresh")
                .user(user)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(user));
        when(refreshTokenService.findByToken("logout-refresh")).thenReturn(Optional.of(token));

        authService.logout("pedro@email.com", "logout-refresh");

        verify(refreshTokenService).revoke(token);
    }

    @Test
    void shouldThrowWhenLogoutTokenDoesNotBelongToUser() {
        User authenticated = new User();
        authenticated.setId(1L);
        authenticated.setEmail("pedro@email.com");

        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setEmail("maria@email.com");

        RefreshToken token = RefreshToken.builder()
                .token("other-refresh")
                .user(anotherUser)
                .revoked(false)
                .expiryDate(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("pedro@email.com")).thenReturn(Optional.of(authenticated));
        when(refreshTokenService.findByToken("other-refresh")).thenReturn(Optional.of(token));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.logout("pedro@email.com", "other-refresh"));

        assertEquals("Refresh token does not belong to authenticated user", exception.getMessage());
        verify(refreshTokenService, never()).revoke(any());
    }
}

