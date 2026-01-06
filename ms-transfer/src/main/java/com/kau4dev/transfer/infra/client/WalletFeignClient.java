package com.kau4dev.transfer.infra.client;

import com.kau4dev.transfer.infra.client.dto.WalletDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "ms-wallet", url = "${ms-wallet.url}")
public interface WalletFeignClient {

    @GetMapping("api/wallets/{userId}")
    WalletDTO getWalletByUserId(@PathVariable("userId") UUID userId);

    @PutMapping("api/wallets/{userId}")
    WalletDTO updateWallet(@PathVariable("userId") UUID userId, @RequestBody WalletDTO walletDTO);
}
