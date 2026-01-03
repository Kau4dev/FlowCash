package com.kau4dev.wallet.service;

import com.kau4dev.wallet.infra.exception.InvalidDepositValueException;
import com.kau4dev.wallet.infra.exception.WalletNotFoundException;
import com.kau4dev.wallet.model.dto.CreateWalletDTO;
import com.kau4dev.wallet.model.dto.DepositDTO;
import com.kau4dev.wallet.model.dto.WalletDTO;
import com.kau4dev.wallet.model.entity.Wallet;
import com.kau4dev.wallet.model.mapper.WalletMapper;
import com.kau4dev.wallet.repository.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    
    @Transactional
    public CreateWalletDTO createWallet (CreateWalletDTO createWalletDTO){
        Wallet wallet = Wallet.builder()
                .idUser(createWalletDTO.userId())
                .balance(createWalletDTO.balance() != null ? createWalletDTO.balance() : BigDecimal.ZERO)
                .build();

        Wallet savedWallet = walletRepository.save(wallet);
        return walletMapper.toCreateWalletDTO(savedWallet);
    }
    
    public BigDecimal getBalance (UUID userId){
        Wallet wallet = findWalletByUserId(userId);
        return wallet.getBalance();
    }
    
    @Transactional
    public WalletDTO deposit(DepositDTO depositDTO) {
        validateDepositValue(depositDTO.value());



        Wallet wallet = findWalletByUserId(depositDTO.userId());
        wallet.deposit(depositDTO.value());
        Wallet updatedWallet = walletRepository.save(wallet);
        return walletMapper.toDTO(updatedWallet);
    }

    private Wallet findWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found for user ID: " + userId));
    }

    private void validateDepositValue(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidDepositValueException("Deposit value must be greater than zero");
        }
    }
}
