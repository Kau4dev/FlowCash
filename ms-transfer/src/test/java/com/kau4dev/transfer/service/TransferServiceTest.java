package com.kau4dev.transfer.service;

import com.kau4dev.transfer.infra.client.AuthorizationFeignClient;
import com.kau4dev.transfer.infra.client.UserFeignClient;
import com.kau4dev.transfer.infra.client.dto.AuthorizationResponseDTO;
import com.kau4dev.transfer.infra.client.dto.UserDTO;
import com.kau4dev.transfer.infra.client.dto.WalletDTO;
import com.kau4dev.transfer.infra.exception.InsufficientFundsException;
import com.kau4dev.transfer.infra.exception.MerchantCannotTransferException;
import com.kau4dev.transfer.infra.exception.TransferNotAuthorizedException;
import com.kau4dev.transfer.infra.exception.UserNotFoundException;
import com.kau4dev.transfer.infra.queue.NotificationProducer;
import com.kau4dev.transfer.model.dto.TransferDTO;
import com.kau4dev.transfer.model.entity.Transfer;
import com.kau4dev.transfer.model.entity.enums.Status;
import com.kau4dev.transfer.model.entity.enums.UserType;
import com.kau4dev.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    AuthorizationFeignClient authClient;

    @Mock
    UserFeignClient userClient;

    @Mock
    TransferRepository transferRepository;

    @Mock
    NotificationProducer notificationProducer;

    @Mock
    WalletService walletService;

    private TransferService transferService;

    @BeforeEach
    void setUp() {
        transferService = new TransferService(
                authClient,
                userClient,
                transferRepository,
                notificationProducer,
                walletService
        );
    }

    @Nested
    class ExecuteTransfer {

        @Test
        @DisplayName("Deve executar transferência com sucesso")
        void DeveExecutarTransferenciaComSucesso() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            var payerDTO = new UserDTO(
                    payerId,
                    "João Silva",
                    "joao@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("500.00"),
                    0
            );

            var payeeDTO = new UserDTO(
                    payeeId,
                    "Maria Santos",
                    "maria@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("200.00"),
                    0
            );

            var authResponse = new AuthorizationResponseDTO(
                    "APPROVED",
                    new AuthorizationResponseDTO.DataDTO(true)
            );

            var transferEntity = Transfer.builder()
                    .id(UUID.randomUUID())
                    .payerId(payerId)
                    .payeeId(payeeId)
                    .amount(amount)
                    .status(Status.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            doReturn(payerDTO).when(userClient).getUserById(payerId);
            doReturn(payeeDTO).when(userClient).getUserById(payeeId);
            doReturn(authResponse).when(authClient).authorize();
            doNothing().when(walletService).processTransfer(payerId, payeeId, amount);
            doReturn(transferEntity).when(transferRepository).save(any(Transfer.class));
            doNothing().when(notificationProducer).sendNotification(any(TransferDTO.class));

            // Act
            var output = transferService.executeTransfer(transferDTO);

            // Assert
            assertNotNull(output);
            assertEquals(payerId, output.getPayerId());
            assertEquals(payeeId, output.getPayeeId());
            assertEquals(amount, output.getAmount());
            assertEquals(Status.APPROVED, output.getStatus());
            verify(walletService, times(1)).processTransfer(payerId, payeeId, amount);
            verify(notificationProducer, times(1)).sendNotification(transferDTO);
        }

        @Test
        @DisplayName("Deve lançar exceção quando pagador é MERCHANT")
        void DeveLancarExcecaoQuandoPagadorEMerchant() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            var payerDTO = new UserDTO(
                    payerId,
                    "Loja XYZ",
                    "loja@gmail.com",
                    UserType.MERCHANT,
                    new BigDecimal("5000.00"),
                    0
            );

            doReturn(payerDTO).when(userClient).getUserById(payerId);

            // Act & Assert
            MerchantCannotTransferException exception = assertThrows(
                    MerchantCannotTransferException.class,
                    () -> transferService.executeTransfer(transferDTO)
            );
            assertEquals("Merchants cannot send money", exception.getMessage());
            verify(authClient, never()).authorize();
            verify(walletService, never()).processTransfer(any(), any(), any());
            verify(transferRepository, never()).save(any());
            verify(notificationProducer, never()).sendNotification(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando pagador não existe")
        void DeveLancarExcecaoQuandoPagadorNaoExiste() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            doThrow(new UserNotFoundException("User not found"))
                    .when(userClient).getUserById(payerId);

            // Act & Assert
            assertThrows(
                    UserNotFoundException.class,
                    () -> transferService.executeTransfer(transferDTO)
            );
            verify(authClient, never()).authorize();
            verify(walletService, never()).processTransfer(any(), any(), any());
            verify(transferRepository, never()).save(any());
            verify(notificationProducer, never()).sendNotification(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando beneficiário não existe")
        void DeveLancarExcecaoQuandoBeneficiarioNaoExiste() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            var payerDTO = new UserDTO(
                    payerId,
                    "João Silva",
                    "joao@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("500.00"),
                    0
            );

            doReturn(payerDTO).when(userClient).getUserById(payerId);
            doThrow(new UserNotFoundException("User not found"))
                    .when(userClient).getUserById(payeeId);

            // Act & Assert
            assertThrows(
                    UserNotFoundException.class,
                    () -> transferService.executeTransfer(transferDTO)
            );
            verify(authClient, never()).authorize();
            verify(walletService, never()).processTransfer(any(), any(), any());
            verify(transferRepository, never()).save(any());
            verify(notificationProducer, never()).sendNotification(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando transferência não é autorizada")
        void DeveLancarExcecaoQuandoTransferenciaNaoEAutorizada() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("100.00");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            var payerDTO = new UserDTO(
                    payerId,
                    "João Silva",
                    "joao@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("500.00"),
                    0
            );

            var payeeDTO = new UserDTO(
                    payeeId,
                    "Maria Santos",
                    "maria@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("200.00"),
                    0
            );

            var authResponse = new AuthorizationResponseDTO(
                    "DENIED",
                    new AuthorizationResponseDTO.DataDTO(false)
            );

            doReturn(payerDTO).when(userClient).getUserById(payerId);
            doReturn(payeeDTO).when(userClient).getUserById(payeeId);
            doReturn(authResponse).when(authClient).authorize();

            // Act & Assert
            TransferNotAuthorizedException exception = assertThrows(
                    TransferNotAuthorizedException.class,
                    () -> transferService.executeTransfer(transferDTO)
            );
            assertEquals("Transfer not authorized by external service", exception.getMessage());
            verify(walletService, never()).processTransfer(any(), any(), any());
            verify(transferRepository, never()).save(any());
            verify(notificationProducer, never()).sendNotification(any());
        }

        @Test
        @DisplayName("Deve lançar exceção quando saldo é insuficiente")
        void DeveLancarExcecaoQuandoSaldoEInsuficiente() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("1000.00");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            var payerDTO = new UserDTO(
                    payerId,
                    "João Silva",
                    "joao@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("100.00"),
                    0
            );

            var payeeDTO = new UserDTO(
                    payeeId,
                    "Maria Santos",
                    "maria@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("200.00"),
                    0
            );

            var authResponse = new AuthorizationResponseDTO(
                    "APPROVED",
                    new AuthorizationResponseDTO.DataDTO(true)
            );

            doReturn(payerDTO).when(userClient).getUserById(payerId);
            doReturn(payeeDTO).when(userClient).getUserById(payeeId);
            doReturn(authResponse).when(authClient).authorize();
            doThrow(new InsufficientFundsException("Insufficient funds"))
                    .when(walletService).processTransfer(payerId, payeeId, amount);

            // Act & Assert
            InsufficientFundsException exception = assertThrows(
                    InsufficientFundsException.class,
                    () -> transferService.executeTransfer(transferDTO)
            );
            assertEquals("Insufficient funds", exception.getMessage());
            verify(transferRepository, never()).save(any());
            verify(notificationProducer, never()).sendNotification(any());
        }

        @Test
        @DisplayName("Deve executar transferência com valor mínimo")
        void DeveExecutarTransferenciaComValorMinimo() {
            // Arrange
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();
            BigDecimal amount = new BigDecimal("0.01");

            var transferDTO = new TransferDTO(payerId, payeeId, amount);

            var payerDTO = new UserDTO(
                    payerId,
                    "João Silva",
                    "joao@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("500.00"),
                    0
            );

            var payeeDTO = new UserDTO(
                    payeeId,
                    "Maria Santos",
                    "maria@gmail.com",
                    UserType.COMMON,
                    new BigDecimal("200.00"),
                    0
            );

            var authResponse = new AuthorizationResponseDTO(
                    "APPROVED",
                    new AuthorizationResponseDTO.DataDTO(true)
            );

            var transferEntity = Transfer.builder()
                    .id(UUID.randomUUID())
                    .payerId(payerId)
                    .payeeId(payeeId)
                    .amount(amount)
                    .status(Status.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            doReturn(payerDTO).when(userClient).getUserById(payerId);
            doReturn(payeeDTO).when(userClient).getUserById(payeeId);
            doReturn(authResponse).when(authClient).authorize();
            doNothing().when(walletService).processTransfer(payerId, payeeId, amount);
            doReturn(transferEntity).when(transferRepository).save(any(Transfer.class));
            doNothing().when(notificationProducer).sendNotification(any(TransferDTO.class));

            // Act
            var output = transferService.executeTransfer(transferDTO);

            // Assert
            assertNotNull(output);
            assertEquals(amount, output.getAmount());
            assertEquals(Status.APPROVED, output.getStatus());
        }
    }

    @Nested
    class GetAllTransferById {

        @Test
        @DisplayName("Deve buscar transferência por ID com sucesso")
        void DeveBuscarTransferenciaPorIdComSucesso() {
            // Arrange
            UUID transferId = UUID.randomUUID();
            UUID payerId = UUID.randomUUID();
            UUID payeeId = UUID.randomUUID();

            var transferEntity = Transfer.builder()
                    .id(transferId)
                    .payerId(payerId)
                    .payeeId(payeeId)
                    .amount(new BigDecimal("100.00"))
                    .status(Status.APPROVED)
                    .createdAt(LocalDateTime.now())
                    .build();

            doReturn(Optional.of(transferEntity)).when(transferRepository).findById(transferId);

            // Act
            var output = transferService.getAllTransferById(transferId);

            // Assert
            assertNotNull(output);
            assertEquals(transferId, output.getId());
            assertEquals(payerId, output.getPayerId());
            assertEquals(payeeId, output.getPayeeId());
            assertEquals(new BigDecimal("100.00"), output.getAmount());
            assertEquals(Status.APPROVED, output.getStatus());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar transferência inexistente")
        void DeveLancarExcecaoAoBuscarTransferenciaInexistente() {
            // Arrange
            UUID transferId = UUID.randomUUID();
            doReturn(Optional.empty()).when(transferRepository).findById(transferId);

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> transferService.getAllTransferById(transferId)
            );
            assertEquals("Transfer not found with ID: " + transferId, exception.getMessage());
        }
    }
}

