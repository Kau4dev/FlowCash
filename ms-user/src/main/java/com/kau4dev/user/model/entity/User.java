package com.kau4dev.user.model.entity;

import com.kau4dev.user.model.entity.enums.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "users")
@Table(name = "tb_users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="full_name", nullable = false)
    private String fullName;

    @Column(name="cpf_cnpj", nullable = false, unique = true)
    private String cpfCnpj;

    @Column(nullable = false, unique = true)
    private String email;

    @Column( nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private UserType type;
}
