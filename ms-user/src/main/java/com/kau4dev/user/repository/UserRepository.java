package com.kau4dev.user.repository;

import com.kau4dev.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Boolean existsByCpfOrCnpj(String cpf, String cnpj);
    Boolean existsEmail(String email);
}
