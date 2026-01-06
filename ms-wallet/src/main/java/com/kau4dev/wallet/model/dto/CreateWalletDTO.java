package com.kau4dev.wallet.model.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletDTO(

        @NotNull(message = "User ID is required")
        UUID userId,

        @NotNull(message = "Balance is required")
        BigDecimal balance) {}
