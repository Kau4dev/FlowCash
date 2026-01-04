package com.kau4dev.transfer.service;

import com.kau4dev.transfer.infra.client.AuthorizationFeignClient;
import com.kau4dev.transfer.infra.client.UserFeignClient;
import com.kau4dev.transfer.infra.client.dto.UserDTO;
import com.kau4dev.transfer.infra.exception.MerchantCannotTransferException;
import com.kau4dev.transfer.infra.exception.TransferNotAuthorizedException;
import com.kau4dev.transfer.infra.exception.UserNotFoundException;
import com.kau4dev.transfer.infra.queue.NotificationProducer;
import com.kau4dev.transfer.model.dto.TransferDTO;
import com.kau4dev.transfer.model.entity.Transfer;
import com.kau4dev.transfer.model.entity.enums.Status;
import com.kau4dev.transfer.model.entity.enums.UserType;
import com.kau4dev.transfer.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AuthorizationFeignClient authClient;
    private final UserFeignClient userClient;
    private final TransferRepository repository;
    private final NotificationProducer notificationProducer;

    @Transactional
    public Transfer executeTransfer(TransferDTO transferDTO) {

        UserDTO payer = userClient.getUserById(transferDTO.payerId());
        if (payer.type() == UserType.MERCHANT) {
            throw new MerchantCannotTransferException("Merchants cannot send money");
        }

        userClient.getUserById(transferDTO.payeeId());

        var authResponse = authClient.authorize();
        if (!authResponse.isAuthorized()) {
            throw new TransferNotAuthorizedException("Transfer not authorized by external service");
        }

        Transfer transfer = Transfer.builder()
                .payerId(transferDTO.payerId())
                .payeeId(transferDTO.payeeId())
                .amount(transferDTO.amount())
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        Transfer savedTransfer = repository.save(transfer);

        notificationProducer.sendNotification(transferDTO);

        return savedTransfer;
    }

    public Transfer getAllTransferById(UUID transferId) {
        return repository.findById(transferId)
                .orElseThrow(() -> new UserNotFoundException("Transfer not found with ID: " + transferId));
    }
}
