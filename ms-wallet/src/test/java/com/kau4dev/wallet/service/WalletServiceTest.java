package com.kau4dev.wallet.service;

import com.kau4dev.wallet.infra.exception.InvalidDepositValueException;
import com.kau4dev.wallet.infra.exception.WalletNotFoundException;
import com.kau4dev.wallet.model.dto.CreateWalletDTO;
import com.kau4dev.wallet.model.dto.DepositDTO;
import com.kau4dev.wallet.model.dto.WalletDTO;
import com.kau4dev.wallet.model.entity.Wallet;
import com.kau4dev.wallet.model.mapper.WalletMapper;
import com.kau4dev.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    WalletRepository walletRepository;

    @Mock
    WalletMapper walletMapper;

    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletRepository, walletMapper);
    }

    @Nested
    class CreateWallet {

        @Test
        @DisplayName("Deve criar uma carteira com saldo inicial com sucesso")
        void DeveCriarUmaCarteiraComSaldoInicialComSucesso() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var createWalletDTO = new CreateWalletDTO(userId, new BigDecimal("100.00"));

            var walletEntity = Wallet.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .balance(new BigDecimal("100.00"))
                    .version(0L)
                    .build();

            var savedWalletDTO = new CreateWalletDTO(userId, new BigDecimal("100.00"));

            doReturn(walletEntity).when(walletRepository).save(any(Wallet.class));
            doReturn(savedWalletDTO).when(walletMapper).toCreateWalletDTO(any(Wallet.class));

            // Act
            var output = walletService.createWallet(createWalletDTO);

            // Assert
            assertNotNull(output);
            assertEquals(userId, output.userId());
            assertEquals(new BigDecimal("100.00"), output.balance());
        }

        @Test
        @DisplayName("Deve criar uma carteira com saldo zero quando não fornecido")
        void DeveCriarUmaCarteiraComSaldoZeroQuandoNaoFornecido() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var createWalletDTO = new CreateWalletDTO(userId, null);

            var walletEntity = Wallet.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .balance(BigDecimal.ZERO)
                    .version(0L)
                    .build();

            var savedWalletDTO = new CreateWalletDTO(userId, BigDecimal.ZERO);

            doReturn(walletEntity).when(walletRepository).save(any(Wallet.class));
            doReturn(savedWalletDTO).when(walletMapper).toCreateWalletDTO(any(Wallet.class));

            // Act
            var output = walletService.createWallet(createWalletDTO);

            // Assert
            assertNotNull(output);
            assertEquals(userId, output.userId());
            assertEquals(BigDecimal.ZERO, output.balance());
        }
    }

    @Nested
    class GetBalance {

        @Test
        @DisplayName("Deve retornar o saldo da carteira com sucesso")
        void DeveRetornarOSaldoDaCarteiraComSucesso() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var walletEntity = Wallet.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .balance(new BigDecimal("500.00"))
                    .version(0L)
                    .build();

            doReturn(Optional.of(walletEntity)).when(walletRepository).findByUserId(userId);

            // Act
            var output = walletService.getBalance(userId);

            // Assert
            assertNotNull(output);
            assertEquals(new BigDecimal("500.00"), output);
        }

        @Test
        @DisplayName("Deve lançar exceção quando carteira não existe")
        void DeveLancarExcecaoQuandoCarteiraNaoExiste() {
            // Arrange
            UUID userId = UUID.randomUUID();
            doReturn(Optional.empty()).when(walletRepository).findByUserId(userId);

            // Act & Assert
            WalletNotFoundException exception = assertThrows(
                    WalletNotFoundException.class,
                    () -> walletService.getBalance(userId)
            );
            assertEquals("Wallet not found for user ID: " + userId, exception.getMessage());
        }
    }

    @Nested
    class GetWalletByUserId {

        @Test
        @DisplayName("Deve buscar carteira por ID do usuário com sucesso")
        void DeveBuscarCarteiraPorIdDoUsuarioComSucesso() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID walletId = UUID.randomUUID();
            var walletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(new BigDecimal("750.00"))
                    .version(0L)
                    .build();

            var walletDTO = new WalletDTO(
                    walletId,
                    new BigDecimal("750.00"),
                    0L,
                    userId
            );

            doReturn(Optional.of(walletEntity)).when(walletRepository).findByUserId(userId);
            doReturn(walletDTO).when(walletMapper).toDTO(any(Wallet.class));

            // Act
            var output = walletService.getWalletByUserId(userId);

            // Assert
            assertNotNull(output);
            assertEquals(walletId, output.id());
            assertEquals(userId, output.userId());
            assertEquals(new BigDecimal("750.00"), output.balance());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar carteira inexistente")
        void DeveLancarExcecaoAoBuscarCarteiraInexistente() {
            // Arrange
            UUID userId = UUID.randomUUID();
            doReturn(Optional.empty()).when(walletRepository).findByUserId(userId);

            // Act & Assert
            WalletNotFoundException exception = assertThrows(
                    WalletNotFoundException.class,
                    () -> walletService.getWalletByUserId(userId)
            );
            assertEquals("Wallet not found for user ID: " + userId, exception.getMessage());
        }
    }

    @Nested
    class Deposit {

        @Test
        @DisplayName("Deve realizar depósito com sucesso")
        void DeveRealizarDepositoComSucesso() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID walletId = UUID.randomUUID();
            var depositDTO = new DepositDTO(userId, new BigDecimal("100.00"));

            var walletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(new BigDecimal("500.00"))
                    .version(0L)
                    .build();

            var updatedWalletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(new BigDecimal("600.00"))
                    .version(1L)
                    .build();

            var walletDTO = new WalletDTO(
                    walletId,
                    new BigDecimal("600.00"),
                    1L,
                    userId
            );

            doReturn(Optional.of(walletEntity)).when(walletRepository).findByUserId(userId);
            doReturn(updatedWalletEntity).when(walletRepository).save(any(Wallet.class));
            doReturn(walletDTO).when(walletMapper).toDTO(any(Wallet.class));

            // Act
            var output = walletService.deposit(depositDTO);

            // Assert
            assertNotNull(output);
            assertEquals(new BigDecimal("600.00"), output.balance());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar depositar valor zero")
        void DeveLancarExcecaoAoTentarDepositarValorZero() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var depositDTO = new DepositDTO(userId, BigDecimal.ZERO);

            // Act & Assert
            InvalidDepositValueException exception = assertThrows(
                    InvalidDepositValueException.class,
                    () -> walletService.deposit(depositDTO)
            );
            assertEquals("Deposit value must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar depositar valor negativo")
        void DeveLancarExcecaoAoTentarDepositarValorNegativo() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var depositDTO = new DepositDTO(userId, new BigDecimal("-50.00"));

            // Act & Assert
            InvalidDepositValueException exception = assertThrows(
                    InvalidDepositValueException.class,
                    () -> walletService.deposit(depositDTO)
            );
            assertEquals("Deposit value must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção ao tentar depositar valor nulo")
        void DeveLancarExcecaoAoTentarDepositarValorNulo() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var depositDTO = new DepositDTO(userId, null);

            // Act & Assert
            InvalidDepositValueException exception = assertThrows(
                    InvalidDepositValueException.class,
                    () -> walletService.deposit(depositDTO)
            );
            assertEquals("Deposit value must be greater than zero", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção ao depositar em carteira inexistente")
        void DeveLancarExcecaoAoDepositarEmCarteiraInexistente() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var depositDTO = new DepositDTO(userId, new BigDecimal("100.00"));

            doReturn(Optional.empty()).when(walletRepository).findByUserId(userId);

            // Act & Assert
            WalletNotFoundException exception = assertThrows(
                    WalletNotFoundException.class,
                    () -> walletService.deposit(depositDTO)
            );
            assertEquals("Wallet not found for user ID: " + userId, exception.getMessage());
        }
    }

    @Nested
    class UpdateBalance {

        @Test
        @DisplayName("Deve atualizar o saldo da carteira com sucesso")
        void DeveAtualizarOSaldoDaCarteiraComSucesso() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID walletId = UUID.randomUUID();
            BigDecimal newBalance = new BigDecimal("1000.00");

            var walletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(new BigDecimal("500.00"))
                    .version(0L)
                    .build();

            var updatedWalletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(newBalance)
                    .version(1L)
                    .build();

            var walletDTO = new WalletDTO(
                    walletId,
                    newBalance,
                    1L,
                    userId
            );

            doReturn(Optional.of(walletEntity)).when(walletRepository).findByUserId(userId);
            doReturn(updatedWalletEntity).when(walletRepository).save(any(Wallet.class));
            doReturn(walletDTO).when(walletMapper).toDTO(any(Wallet.class));

            // Act
            var output = walletService.updateBalance(userId, newBalance);

            // Assert
            assertNotNull(output);
            assertEquals(newBalance, output.balance());
        }

        @Test
        @DisplayName("Deve atualizar saldo para zero")
        void DeveAtualizarSaldoParaZero() {
            // Arrange
            UUID userId = UUID.randomUUID();
            UUID walletId = UUID.randomUUID();
            BigDecimal newBalance = BigDecimal.ZERO;

            var walletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(new BigDecimal("500.00"))
                    .version(0L)
                    .build();

            var updatedWalletEntity = Wallet.builder()
                    .id(walletId)
                    .userId(userId)
                    .balance(newBalance)
                    .version(1L)
                    .build();

            var walletDTO = new WalletDTO(
                    walletId,
                    newBalance,
                    1L,
                    userId
            );

            doReturn(Optional.of(walletEntity)).when(walletRepository).findByUserId(userId);
            doReturn(updatedWalletEntity).when(walletRepository).save(any(Wallet.class));
            doReturn(walletDTO).when(walletMapper).toDTO(any(Wallet.class));

            // Act
            var output = walletService.updateBalance(userId, newBalance);

            // Assert
            assertNotNull(output);
            assertEquals(BigDecimal.ZERO, output.balance());
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar saldo de carteira inexistente")
        void DeveLancarExcecaoAoAtualizarSaldoDeCarteiraInexistente() {
            // Arrange
            UUID userId = UUID.randomUUID();
            BigDecimal newBalance = new BigDecimal("1000.00");

            doReturn(Optional.empty()).when(walletRepository).findByUserId(userId);

            // Act & Assert
            WalletNotFoundException exception = assertThrows(
                    WalletNotFoundException.class,
                    () -> walletService.updateBalance(userId, newBalance)
            );
            assertEquals("Wallet not found for user ID: " + userId, exception.getMessage());
        }
    }
}

