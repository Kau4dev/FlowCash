package com.kau4dev.transfer.service;

import com.kau4dev.transfer.infra.client.WalletFeignClient;
import com.kau4dev.transfer.infra.client.dto.WalletDTO;
import com.kau4dev.transfer.infra.exception.InsufficientFundsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferWalletServiceTest {

    @Mock
    WalletFeignClient walletClient;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletClient);
    }

    @Nested
    class ProcessTransfer {

        @Test
        @DisplayName("Deve processar transferência com sucesso")
        void DeveProcessarTransferenciaComSucesso() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            UUID payeeWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    new BigDecimal("500.00"),
                    0L,
                    payerId
            );

            var payeeWallet = new WalletDTO(
                    payeeWalletId,
                    new BigDecimal("200.00"),
                    0L,
                    payeeId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);
            doReturn(payeeWallet).when(walletClient).getWalletByUserId(payeeId);
            doReturn(null).when(walletClient).updateWallet(eq(payerId), any(WalletDTO.class));
            doReturn(null).when(walletClient).updateWallet(eq(payeeId), any(WalletDTO.class));

            // Act
            walletService.processTransfer(payerId, payeeId, amount);

            // Assert
            ArgumentCaptor<WalletDTO> payerCaptor = ArgumentCaptor.forClass(WalletDTO.class);
            ArgumentCaptor<WalletDTO> payeeCaptor = ArgumentCaptor.forClass(WalletDTO.class);

            verify(walletClient, times(1)).updateWallet(eq(payerId), payerCaptor.capture());
            verify(walletClient, times(1)).updateWallet(eq(payeeId), payeeCaptor.capture());

            // Verifica se o saldo do pagador foi deduzido corretamente
            WalletDTO updatedPayerWallet = payerCaptor.getValue();
            assertEquals(new BigDecimal("400.00"), updatedPayerWallet.balance());
            assertEquals(payerWalletId, updatedPayerWallet.id());
            assertEquals(payerId, updatedPayerWallet.userId());

            // Verifica se o saldo do beneficiário foi creditado corretamente
            WalletDTO updatedPayeeWallet = payeeCaptor.getValue();
            assertEquals(new BigDecimal("300.00"), updatedPayeeWallet.balance());
            assertEquals(payeeWalletId, updatedPayeeWallet.id());
            assertEquals(payeeId, updatedPayeeWallet.userId());
        }

        @Test
        @DisplayName("Deve lançar exceção quando saldo é insuficiente")
        void DeveLancarExcecaoQuandoSaldoEInsuficiente() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("1000.00");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    new BigDecimal("100.00"),
                    0L,
                    payerId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);

            // Act & Assert
            InsufficientFundsException exception = assertThrows(
                    InsufficientFundsException.class,
                    () -> walletService.processTransfer(payerId, payeeId, amount)
            );
            assertEquals("Insufficient funds", exception.getMessage());
            verify(walletClient, never()).updateWallet(any(), any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando saldo é exatamente igual ao valor da transferência menos um centavo")
        void DeveLancarExcecaoQuandoSaldoEExatamenteIgualAoValorMenosUmCentavo() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.01");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    new BigDecimal("100.00"),
                    0L,
                    payerId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);

            // Act & Assert
            InsufficientFundsException exception = assertThrows(
                    InsufficientFundsException.class,
                    () -> walletService.processTransfer(payerId, payeeId, amount)
            );
            assertEquals("Insufficient funds", exception.getMessage());
            verify(walletClient, never()).updateWallet(any(), any());
        }

        @Test
        @DisplayName("Deve processar transferência quando saldo é exatamente igual ao valor")
        void DeveProcessarTransferenciaQuandoSaldoEExatamenteIgualAoValor() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            UUID payeeWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("500.00");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    new BigDecimal("500.00"),
                    0L,
                    payerId
            );

            var payeeWallet = new WalletDTO(
                    payeeWalletId,
                    new BigDecimal("200.00"),
                    0L,
                    payeeId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);
            doReturn(payeeWallet).when(walletClient).getWalletByUserId(payeeId);
            doReturn(null).when(walletClient).updateWallet(eq(payerId), any(WalletDTO.class));
            doReturn(null).when(walletClient).updateWallet(eq(payeeId), any(WalletDTO.class));

            // Act
            walletService.processTransfer(payerId, payeeId, amount);

            // Assert
            ArgumentCaptor<WalletDTO> payerCaptor = ArgumentCaptor.forClass(WalletDTO.class);
            verify(walletClient, times(1)).updateWallet(eq(payerId), payerCaptor.capture());

            // Verifica se o saldo do pagador ficou zerado
            WalletDTO updatedPayerWallet = payerCaptor.getValue();
            assertEquals(0, updatedPayerWallet.balance().compareTo(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("Deve processar transferência com valor mínimo")
        void DeveProcessarTransferenciaComValorMinimo() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            UUID payeeWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("0.01");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    new BigDecimal("10.00"),
                    0L,
                    payerId
            );

            var payeeWallet = new WalletDTO(
                    payeeWalletId,
                    new BigDecimal("5.00"),
                    0L,
                    payeeId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);
            doReturn(payeeWallet).when(walletClient).getWalletByUserId(payeeId);
            doReturn(null).when(walletClient).updateWallet(eq(payerId), any(WalletDTO.class));
            doReturn(null).when(walletClient).updateWallet(eq(payeeId), any(WalletDTO.class));

            // Act
            walletService.processTransfer(payerId, payeeId, amount);

            // Assert
            ArgumentCaptor<WalletDTO> payerCaptor = ArgumentCaptor.forClass(WalletDTO.class);
            ArgumentCaptor<WalletDTO> payeeCaptor = ArgumentCaptor.forClass(WalletDTO.class);

            verify(walletClient, times(1)).updateWallet(eq(payerId), payerCaptor.capture());
            verify(walletClient, times(1)).updateWallet(eq(payeeId), payeeCaptor.capture());

            WalletDTO updatedPayerWallet = payerCaptor.getValue();
            assertEquals(new BigDecimal("9.99"), updatedPayerWallet.balance());

            WalletDTO updatedPayeeWallet = payeeCaptor.getValue();
            assertEquals(new BigDecimal("5.01"), updatedPayeeWallet.balance());
        }

        @Test
        @DisplayName("Deve processar transferência com valores decimais complexos")
        void DeveProcessarTransferenciaComValoresDecimaisComplexos() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            UUID payeeWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("123.4567");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    new BigDecimal("1000.9876"),
                    0L,
                    payerId
            );

            var payeeWallet = new WalletDTO(
                    payeeWalletId,
                    new BigDecimal("500.1234"),
                    0L,
                    payeeId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);
            doReturn(payeeWallet).when(walletClient).getWalletByUserId(payeeId);
            doReturn(null).when(walletClient).updateWallet(eq(payerId), any(WalletDTO.class));
            doReturn(null).when(walletClient).updateWallet(eq(payeeId), any(WalletDTO.class));

            // Act
            walletService.processTransfer(payerId, payeeId, amount);

            // Assert
            ArgumentCaptor<WalletDTO> payerCaptor = ArgumentCaptor.forClass(WalletDTO.class);
            ArgumentCaptor<WalletDTO> payeeCaptor = ArgumentCaptor.forClass(WalletDTO.class);

            verify(walletClient, times(1)).updateWallet(eq(payerId), payerCaptor.capture());
            verify(walletClient, times(1)).updateWallet(eq(payeeId), payeeCaptor.capture());

            WalletDTO updatedPayerWallet = payerCaptor.getValue();
            assertEquals(new BigDecimal("877.5309"), updatedPayerWallet.balance());

            WalletDTO updatedPayeeWallet = payeeCaptor.getValue();
            assertEquals(new BigDecimal("623.5801"), updatedPayeeWallet.balance());
        }

        @Test
        @DisplayName("Deve lançar exceção quando saldo do pagador é zero")
        void DeveLancarExcecaoQuandoSaldoDoPagadorEZero() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            UUID payerWalletId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("50.00");

            var payerWallet = new WalletDTO(
                    payerWalletId,
                    BigDecimal.ZERO,
                    0L,
                    payerId
            );

            doReturn(payerWallet).when(walletClient).getWalletByUserId(payerId);

            // Act & Assert
            InsufficientFundsException exception = assertThrows(
                    InsufficientFundsException.class,
                    () -> walletService.processTransfer(payerId, payeeId, amount)
            );
            assertEquals("Insufficient funds", exception.getMessage());
            verify(walletClient, never()).updateWallet(any(), any());
        }
    }
}

