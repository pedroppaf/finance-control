package com.pedro.finance_control.service;

import com.pedro.finance_control.dto.auth.AuthResponse;
import com.pedro.finance_control.dto.auth.LoginRequest;
import com.pedro.finance_control.dto.auth.RegisterRequest;
import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        return new AuthResponse("User sucessfully registered");
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new RuntimeException("Invalid email or password");
        }
        return new AuthResponse("Login successful");
    }
}
