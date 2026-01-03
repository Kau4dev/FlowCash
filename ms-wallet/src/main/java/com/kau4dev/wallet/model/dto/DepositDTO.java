package com.kau4dev.wallet.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositDTO(UUID userId, BigDecimal value){

}
