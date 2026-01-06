package com.kau4dev.transfer.controller;

import com.kau4dev.transfer.model.dto.TransferDTO;
import com.kau4dev.transfer.model.entity.Transfer;
import com.kau4dev.transfer.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<Object> performTransfer(@RequestBody @Valid TransferDTO transferDTO) {
        Transfer transfer = transferService.executeTransfer(transferDTO);
        return ResponseEntity.status(201).body(transfer);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Transfer> getAllTransferById(@PathVariable UUID userId) {
        Transfer transfer = transferService.getAllTransferById(userId);
        return ResponseEntity.status(200).body(transfer);
    }
}
