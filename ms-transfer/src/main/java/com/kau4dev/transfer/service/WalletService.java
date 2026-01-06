package com.kau4dev.transfer.service;

import com.kau4dev.transfer.infra.client.WalletFeignClient;
import com.kau4dev.transfer.infra.client.dto.WalletDTO;
import com.kau4dev.transfer.infra.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletFeignClient walletClient;

    public void processTransfer(UUID payerId, UUID payeeId, BigDecimal amount) {
        WalletDTO payerWallet = walletClient.getWalletByUserId(payerId);
        WalletDTO payeeWallet = walletClient.getWalletByUserId(payeeId);

        if (payerWallet.balance().compareTo(amount) < 0 ) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        walletClient.updateWallet(payerId, new WalletDTO(payerId, payerWallet.balance().subtract(amount)));
        walletClient.updateWallet(payeeId, new WalletDTO(payeeId, payeeWallet.balance().add(amount)));
    }
}
