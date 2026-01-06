package com.kau4dev.transfer.infra.client;

import com.kau4dev.transfer.infra.client.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "ms-user", url = "${ms-user.url:http://localhost:8081}")
public interface UserFeignClient {

    @GetMapping("api/users/{id}")
    UserDTO getUserById(@PathVariable("id") UUID id);
}
