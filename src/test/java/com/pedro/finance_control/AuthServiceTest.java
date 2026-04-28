package com.pedro.finance_control;

import com.pedro.finance_control.dto.auth.AuthResponse;
import com.pedro.finance_control.dto.auth.LoginRequest;
import com.pedro.finance_control.dto.auth.RegisterRequest;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.repository.UserRepository;
import com.pedro.finance_control.security.JwtService;
import com.pedro.finance_control.service.AuthService;
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

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Pedro", "pedro@email.com", "123456");

        when(userRepository.existsByEmail("pedro@email.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");
        when(jwtService.generateToken("pedro@email.com")).thenReturn("token-123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("token-123", response.token());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("Pedro", savedUser.getName());
        assertEquals("pedro@email.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
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

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("token-456", response.token());
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
}

