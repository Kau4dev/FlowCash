package com.kau4dev.user.repository;

import com.kau4dev.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Boolean existsByCpfCnpj(String cpfCnpj);

    Boolean existsByEmail(String email);
}
