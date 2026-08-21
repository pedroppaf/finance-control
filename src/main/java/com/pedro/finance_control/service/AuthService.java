package com.pedro.finance_control.service;

import com.pedro.finance_control.dto.auth.AuthResponse;
import com.pedro.finance_control.dto.auth.LoginRequest;
import com.pedro.finance_control.dto.auth.RegisterRequest;
import com.pedro.finance_control.dto.auth.UserResponse;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.exception.BusinessRuleException;
import com.pedro.finance_control.repository.UserRepository;
import com.pedro.finance_control.security.JwtService;
import com.pedro.finance_control.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new BusinessRuleException("Email already registered");
        }
        User user = User.builder()
                .role("USER")
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return new AuthResponse(token, refreshToken.getToken());
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessRuleException("Invalid email or password"));

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new BusinessRuleException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return new AuthResponse(token, refreshToken.getToken());
    }

    public AuthResponse refresh(String refreshToken){
        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        if (tokenEntity.isRevoked() || refreshTokenService.isExpired(tokenEntity)){
            refreshTokenService.revoke(tokenEntity);
            throw new BusinessRuleException("Refresh token expired or revoked");
        }

        // rotate: revoke old and create a new one
        refreshTokenService.revoke(tokenEntity);
        RefreshToken newRefresh = refreshTokenService.createRefreshToken(tokenEntity.getUser());

        String newAccessToken = jwtService.generateToken(tokenEntity.getUser().getEmail());

        return new AuthResponse(newAccessToken, newRefresh.getToken());
    }

    public void logout(String email, String refreshToken) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("Authenticated user not found"));

        RefreshToken tokenEntity = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));

        if (!tokenEntity.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleException("Refresh token does not belong to authenticated user");
        }

        refreshTokenService.revoke(tokenEntity);
    }

    public UserResponse getAuthenticatedUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessRuleException("User not found"));
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
