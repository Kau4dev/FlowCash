package com.kau4dev.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.br.CNPJ;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public record UserDTO(

        UUID id,

        @NotNull(message = "Full name is required")
        String fullName,

        @CPF
        String cpf,
        @CNPJ
        String cnpj,

        @Email
        @NotNull(message = "Email is required")
        String email,

        @NotNull(message = "Password is required")
        String password,

        @NotBlank(message = "Type is required")
        String type

) {
}
