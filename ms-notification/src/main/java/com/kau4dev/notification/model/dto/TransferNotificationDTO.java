package com.kau4dev.notification.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferNotificationDTO(
        UUID payerId,
        UUID payeeId,
        BigDecimal amount
) {}