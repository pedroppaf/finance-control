package com.pedro.finance_control.repository;

import com.pedro.finance_control.entity.RefreshToken;
import com.pedro.finance_control.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(User user);
}
