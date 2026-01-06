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

        WalletDTO updatedPayerWallet = new WalletDTO(
                payerWallet.id(),
                payerWallet.balance().subtract(amount),
                payerWallet.version(),
                payerWallet.userId()
        );

        WalletDTO updatedPayeeWallet = new WalletDTO(
                payeeWallet.id(),
                payeeWallet.balance().add(amount),
                payeeWallet.version(),
                payeeWallet.userId()
        );

        walletClient.updateWallet(payerId, updatedPayerWallet);
        walletClient.updateWallet(payeeId, updatedPayeeWallet);
    }
}
