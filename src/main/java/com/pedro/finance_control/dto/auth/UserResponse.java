package com.pedro.finance_control.dto.auth;

import java.time.LocalDateTime;

public record UserResponse (Long id,
                            String name,
                            String email,
                            String role,
                            LocalDateTime createdAt){
}
