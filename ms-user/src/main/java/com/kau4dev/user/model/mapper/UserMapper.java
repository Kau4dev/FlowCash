package com.kau4dev.user.model.mapper;

import com.kau4dev.user.model.dto.UserDTO;
import com.kau4dev.user.model.entity.User;
import com.kau4dev.user.model.entity.enums.UserType;
import org.mapstruct.*;
import org.springframework.util.StringUtils;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "cpfCnpj", expression = "java(selectCpfOrCnpj(dto))")
    @Mapping(target = "type", expression = "java(parseUserType(dto.type()))")
    User toEntity(UserDTO dto);

    @Mapping(target = "cpf", expression = "java(isCpf(user.getCpfCnpj()) ? user.getCpfCnpj() : null)")
    @Mapping(target = "cnpj", expression = "java(isCnpj(user.getCpfCnpj()) ? user.getCpfCnpj() : null)")
    UserDTO toDTO(User user);

    default String selectCpfOrCnpj(UserDTO dto) {
        if (StringUtils.hasText(dto.cpf())) return dto.cpf().replaceAll("\\D", "");
        if (StringUtils.hasText(dto.cnpj())) return dto.cnpj().replaceAll("\\D", "");
        return null;
    }

    default UserType parseUserType(String type) {
        if (!StringUtils.hasText(type)) return null;
        try {
            return UserType.valueOf(type.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    default boolean isCpf(String cpfCnpj) {
        return cpfCnpj != null && cpfCnpj.length() == 11;
    }

    default boolean isCnpj(String cpfCnpj) {
        return cpfCnpj != null && cpfCnpj.length() == 14;
    }
}
