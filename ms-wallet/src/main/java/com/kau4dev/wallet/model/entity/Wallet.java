package com.kau4dev.wallet.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "wallets")
@Table(name = "tb_wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
 
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "id_user", nullable = false)
    private UUID idUser;

    public void deposit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }


}
