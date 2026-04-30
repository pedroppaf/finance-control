package com.pedro.finance_control;

import com.pedro.finance_control.entity.User;
import com.pedro.finance_control.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmail() {
        User user = new User(null, "Pedro", "pedro@email.com", "password", LocalDateTime.now());
        userRepository.save(user);

        assertTrue(userRepository.findByEmail("pedro@email.com").isPresent());
        assertEquals("pedro@email.com", userRepository.findByEmail("pedro@email.com").orElseThrow().getEmail());
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = new User(null, "Pedro", "pedro@email.com", "password", LocalDateTime.now());
        userRepository.save(user);

        assertTrue(userRepository.existsByEmail("pedro@email.com"));
        assertFalse(userRepository.existsByEmail("other@email.com"));
    }
}


