package com.kau4dev.transfer.infra.client;

import com.kau4dev.transfer.infra.client.dto.AuthorizationResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "authorization-service", url = "${authorization.service.url:https://util.devi.tools/api/v2/authorize}")
public interface AuthorizationFeignClient {

    @GetMapping
    AuthorizationResponseDTO authorize();
}