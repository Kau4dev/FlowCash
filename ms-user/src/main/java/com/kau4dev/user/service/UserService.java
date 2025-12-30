package com.kau4dev.user.service;

import com.kau4dev.user.model.dto.UserDTO;
import com.kau4dev.user.model.entity.User;
import com.kau4dev.user.model.mapper.UserMapper;
import com.kau4dev.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        validateCpfOrCnpj(userDTO.cpf(), userDTO.cnpj());

        if(userRepository.existsByCpfOrCnpj(userDTO.cnpj(), userDTO.cpf())){
            throw new IllegalArgumentException("User already exists");
        }
        if(userRepository.existsEmail(userDTO.email())){
            throw new IllegalArgumentException("Email already exists");
        }

        User createdUser = userMapper.toEntity(userDTO);
        User SavedUser = userRepository.save(createdUser);
        return userMapper.toDTO(SavedUser);
    }

    public List<UserDTO> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toDTO)
                .toList();
    }

    public UserDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return userMapper.toDTO(user);
    }


    private void validateCpfOrCnpj(String cpf, String cnpj) {
        if ((cpf == null || cpf.isBlank()) && (cnpj == null || cnpj.isBlank())) {
            throw new IllegalArgumentException("CPF or CNPJ is required");
        }
        if (cpf != null && !cpf.isBlank() && cnpj != null && !cnpj.isBlank()) {
            throw new IllegalArgumentException("Provide either CPF or CNPJ, not both");
        }
    }

}
