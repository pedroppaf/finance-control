package com.pedro.finance_control.dto;

import com.pedro.finance_control.enums.Category;
import com.pedro.finance_control.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionRequest(
        @NotBlank(message = "Title is required")
        String title,

        String description,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater")
        BigDecimal amount,

        @NotNull(message = "Type is required")
        TransactionType type,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Date is required")
        LocalDate date

) {
}
