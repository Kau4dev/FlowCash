package com.kau4dev.wallet.model.mapper;

import com.kau4dev.wallet.model.dto.CreateWalletDTO;
import com.kau4dev.wallet.model.dto.WalletDTO;
import com.kau4dev.wallet.model.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletMapper {

    WalletDTO toDTO(Wallet wallet);

    @Mapping(target = "userId", source = "idUser")
    CreateWalletDTO toCreateWalletDTO(Wallet wallet);
}
