package com.kau4dev.transfer.infra.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizationResponseDTO(
        String status,
        DataDTO data
) {
    public boolean isAuthorized() {
        return "success".equalsIgnoreCase(status) &&
                data != null &&
                Boolean.TRUE.equals(data.authorization());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DataDTO(Boolean authorization) {}
}
