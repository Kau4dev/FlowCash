package com.kau4dev.wallet.controller;

import com.kau4dev.wallet.model.dto.CreateWalletDTO;
import com.kau4dev.wallet.model.entity.Wallet;
import com.kau4dev.wallet.repository.WalletRepository;
import com.kau4dev.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<CreateWalletDTO> createWallet(@RequestBody @Valid CreateWalletDTO createWalletDTO) {
        CreateWalletDTO createdWallet  = walletService.createWallet;

    }

    @GetMapping("/{userId}")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID userId) {

    }
    @PostMapping("/deposit")
    public ResponseEntity<WalletDTO> deposit(@RequestBody DepositDTO depositDTO) {

    }
}