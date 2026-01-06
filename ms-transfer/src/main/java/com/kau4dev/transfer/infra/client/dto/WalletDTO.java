package com.kau4dev.transfer.infra.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletDTO(
        UUID userId,
        BigDecimal balance) {
}
