package com.kau4dev.user.service;

import com.kau4dev.user.infra.exception.*;
import com.kau4dev.user.model.dto.UserDTO;
import com.kau4dev.user.model.entity.User;
import com.kau4dev.user.model.mapper.UserMapper;
import com.kau4dev.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {

        validateUserData(userDTO);
        String cpfCnpj = extractAndCleanCpfCnpj(userDTO);
        checkDuplicates(cpfCnpj, userDTO.email());

        User createdUser = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(createdUser);
        return userMapper.toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDTO getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
        return userMapper.toDTO(user);
    }



    private void validateUserData(UserDTO userDTO) {
        validateCpfOrCnpj(userDTO.cpf(), userDTO.cnpj());
        validateEmail(userDTO.email());
    }

    private void validateCpfOrCnpj(String cpf, String cnpj) {
        boolean hasCpf = cpf != null && !cpf.isBlank();
        boolean hasCnpj = cnpj != null && !cnpj.isBlank();

        if (!hasCpf && !hasCnpj) {
            throw new CpfCnpjRequiredException("CPF or CNPJ is required");
        }
        if (hasCpf && hasCnpj) {
            throw new CpfCnpjMutuallyExclusiveException("Provide either CPF or CNPJ, not both");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
    }

    private String extractAndCleanCpfCnpj(UserDTO userDTO) {
        String document = userDTO.cpf() != null ? userDTO.cpf() : userDTO.cnpj();
        return document.replaceAll("\\D", "");
    }

    private void checkDuplicates(String cpfCnpj, String email) {
        if (userRepository.existsByCpfCnpj(cpfCnpj)) {
            throw new CpfCnpjAlreadyExistsException("CPF/CNPJ already registered");
        }
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already registered");
        }
    }

}
