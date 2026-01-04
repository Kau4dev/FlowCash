package com.kau4dev.transfer.infra.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kau4dev.transfer.model.entity.enums.UserType;

import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserDTO(
        UUID id,
        String name,
        String email,
        UserType type,
        BigDecimal balance,
        Integer version
) {}
