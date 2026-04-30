package com.pedro.finance_control.dto;

import com.pedro.finance_control.enums.Category;
import com.pedro.finance_control.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String title,
        String description,
        BigDecimal amount,
        TransactionType type,
        Category category,
        LocalDate date,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
