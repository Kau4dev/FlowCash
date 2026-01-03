package com.kau4dev.wallet.model.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletDTO (
        UUID id,

        @NotNull(message = "Balance is required")
        BigDecimal balance,

        @NotNull(message = "Version is required")
        Long version,

        @NotNull(message = "Id User is required")
        UUID userId
) {
}
