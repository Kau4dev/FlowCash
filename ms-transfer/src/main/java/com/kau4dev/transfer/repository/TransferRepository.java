package com.kau4dev.transfer.repository;

import com.kau4dev.transfer.model.entity.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Transfer findByPayerId(UUID payerId);
}
