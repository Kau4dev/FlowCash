package com.kau4dev.transfer.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferDTO(
        @NotNull(message = "Payer ID is required")
        UUID payerId,

        @NotNull(message = "Payee ID is required")
        UUID payeeId,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount
) {}
