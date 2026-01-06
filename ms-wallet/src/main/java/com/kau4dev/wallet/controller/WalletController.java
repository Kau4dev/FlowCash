package com.kau4dev.wallet.controller;

import com.kau4dev.wallet.model.dto.CreateWalletDTO;
import com.kau4dev.wallet.model.dto.DepositDTO;
import com.kau4dev.wallet.model.dto.WalletDTO;
import com.kau4dev.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<CreateWalletDTO> createWallet(@RequestBody @Valid CreateWalletDTO createWalletDTO) {
        CreateWalletDTO createdWallet  = walletService.createWallet(createWalletDTO);
        return ResponseEntity.status(201).body(createdWallet);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletDTO> getBalance(@PathVariable UUID userId) {
        WalletDTO walletDTO = walletService.getWalletByUserId(userId);
        return ResponseEntity.status(200).body(walletDTO);
    }
    @PostMapping("/deposit")
    public ResponseEntity<WalletDTO> deposit(@RequestBody DepositDTO depositDTO) {
        WalletDTO updatedWallet = walletService.deposit(depositDTO);
        return ResponseEntity.status(200).body(updatedWallet);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<WalletDTO> updateBalance(@PathVariable UUID userId, @RequestBody WalletDTO updateWalletDTO){
        WalletDTO updatedWallet = walletService.updateBalance(userId, updateWalletDTO.balance());
        return ResponseEntity.status(200).body(updatedWallet);
    }
}