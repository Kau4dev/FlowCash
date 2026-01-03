package com.kau4dev.wallet.service;

import com.kau4dev.wallet.infra.exception.WalletNotFoundException;
import com.kau4dev.wallet.model.dto.CreateWalletDTO;
import com.kau4dev.wallet.model.dto.DepositDTO;
import com.kau4dev.wallet.model.dto.WalletDTO;
import com.kau4dev.wallet.model.entity.Wallet;
import com.kau4dev.wallet.model.mapper.WalletMapper;
import com.kau4dev.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@AllArgsConstructor
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    
    @Transactional
    public CreateWalletDTO createWallet (CreateWalletDTO createWalletDTO){
        Wallet wallet = Wallet.builder()
                .idUser(createWalletDTO.userId())
                .balance(createWalletDTO.balance())
                .build();
        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toCreateWalletDTO(savedWallet);
    }
    
    public BigDecimal getBalance (UUID userId){
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + userId));
        return wallet.getBalance();
    }
    
    @Transactional
    public WalletDTO deposit(DepositDTO depositDTO) {

        Wallet wallet = walletRepository.findByUserId(depositDTO.userId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + depositDTO.userId()));

        wallet.setBalance(wallet.getBalance().add(depositDTO.value()));
        Wallet updatedWallet = walletRepository.save(wallet);
        return walletMapper.toDTO(updatedWallet);
    }
}
