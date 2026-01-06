package com.kau4dev.user.service;

import com.kau4dev.user.infra.exception.*;
import com.kau4dev.user.model.dto.UserDTO;
import com.kau4dev.user.model.entity.User;
import com.kau4dev.user.model.entity.enums.UserType;
import com.kau4dev.user.model.mapper.UserMapper;
import com.kau4dev.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userMapper);
    }

    @Nested
    class CreateUser {

        @Test
        @DisplayName("Deve criar um usuário com CPF com sucesso")
        void DeveCriarUmUsuarioComCPFComSucesso() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            var userEntity = User.builder()
                    .id(UUID.randomUUID())
                    .fullName("João Silva")
                    .cpfCnpj("12345678900")
                    .email("joao@gmail.com")
                    .password("senha123")
                    .type(UserType.COMMON)
                    .build();

            var savedUserDTO = new UserDTO(
                    userEntity.getId(),
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            doReturn(userEntity).when(userMapper).toEntity(any(UserDTO.class));
            doReturn(false).when(userRepository).existsByCpfCnpj("12345678900");
            doReturn(false).when(userRepository).existsByEmail("joao@gmail.com");
            doReturn(userEntity).when(userRepository).save(any(User.class));
            doReturn(savedUserDTO).when(userMapper).toDTO(any(User.class));

            // Act
            var output = userService.createUser(userDTO);

            // Assert
            assertNotNull(output);
            assertEquals("João Silva", output.fullName());
            assertEquals("joao@gmail.com", output.email());
        }

        @Test
        @DisplayName("Deve criar um usuário com CNPJ com sucesso")
        void DeveCriarUmUsuarioComCNPJComSucesso() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "Empresa LTDA",
                    null,
                    "12.345.678/0001-90",
                    "empresa@gmail.com",
                    "senha123",
                    "MERCHANT"
            );

            var userEntity = User.builder()
                    .id(UUID.randomUUID())
                    .fullName("Empresa LTDA")
                    .cpfCnpj("12345678000190")
                    .email("empresa@gmail.com")
                    .password("senha123")
                    .type(UserType.MERCHANT)
                    .build();

            var savedUserDTO = new UserDTO(
                    userEntity.getId(),
                    "Empresa LTDA",
                    null,
                    "12.345.678/0001-90",
                    "empresa@gmail.com",
                    "senha123",
                    "MERCHANT"
            );

            doReturn(userEntity).when(userMapper).toEntity(any(UserDTO.class));
            doReturn(false).when(userRepository).existsByCpfCnpj("12345678000190");
            doReturn(false).when(userRepository).existsByEmail("empresa@gmail.com");
            doReturn(userEntity).when(userRepository).save(any(User.class));
            doReturn(savedUserDTO).when(userMapper).toDTO(any(User.class));

            // Act
            var output = userService.createUser(userDTO);

            // Assert
            assertNotNull(output);
            assertEquals("Empresa LTDA", output.fullName());
            assertEquals("empresa@gmail.com", output.email());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF já existe")
        void DeveLancarExcecaoQuandoCPFJaExiste() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            doReturn(true).when(userRepository).existsByCpfCnpj("12345678900");

            // Act & Assert
            CpfCnpjAlreadyExistsException exception = assertThrows(
                    CpfCnpjAlreadyExistsException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("CPF/CNPJ already registered", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existe")
        void DeveLancarExcecaoQuandoEmailJaExiste() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            doReturn(false).when(userRepository).existsByCpfCnpj("12345678900");
            doReturn(true).when(userRepository).existsByEmail("joao@gmail.com");

            // Act & Assert
            EmailAlreadyExistsException exception = assertThrows(
                    EmailAlreadyExistsException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("Email already registered", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF e CNPJ não são fornecidos")
        void DeveLancarExcecaoQuandoCPFECNPJNaoSaoFornecidos() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    null,
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            // Act & Assert
            CpfCnpjRequiredException exception = assertThrows(
                    CpfCnpjRequiredException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("CPF or CNPJ is required", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando CPF e CNPJ são fornecidos juntos")
        void DeveLancarExcecaoQuandoCPFECNPJSaoFornecidosJuntos() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    "123.456.789-00",
                    "12.345.678/0001-90",
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            // Act & Assert
            CpfCnpjMutuallyExclusiveException exception = assertThrows(
                    CpfCnpjMutuallyExclusiveException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("Provide either CPF or CNPJ, not both", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email é nulo")
        void DeveLancarExcecaoQuandoEmailENulo() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    "123.456.789-00",
                    null,
                    null,
                    "senha123",
                    "COMMON"
            );

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("Email is required", exception.getMessage());
        }

        @Test
        @DisplayName("Deve lançar exceção quando email está vazio")
        void DeveLancarExcecaoQuandoEmailEstaVazio() {
            // Arrange
            var userDTO = new UserDTO(
                    null,
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "",
                    "senha123",
                    "COMMON"
            );

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("Email is required", exception.getMessage());
        }
    }

    @Nested
    class GetAllUsers {

        @Test
        @DisplayName("Deve listar usuários com sucesso")
        void DeveListarUsuariosComSucesso() {
            // Arrange
            var userEntity1 = User.builder()
                    .id(UUID.randomUUID())
                    .fullName("João Silva")
                    .cpfCnpj("12345678900")
                    .email("joao@gmail.com")
                    .password("senha123")
                    .type(UserType.COMMON)
                    .build();

            var userEntity2 = User.builder()
                    .id(UUID.randomUUID())
                    .fullName("Maria Santos")
                    .cpfCnpj("98765432100")
                    .email("maria@gmail.com")
                    .password("senha456")
                    .type(UserType.COMMON)
                    .build();

            var userDTO1 = new UserDTO(
                    userEntity1.getId(),
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            var userDTO2 = new UserDTO(
                    userEntity2.getId(),
                    "Maria Santos",
                    "987.654.321-00",
                    null,
                    "maria@gmail.com",
                    "senha456",
                    "COMMON"
            );

            doReturn(List.of(userEntity1, userEntity2)).when(userRepository).findAll();
            doReturn(userDTO1).when(userMapper).toDTO(userEntity1);
            doReturn(userDTO2).when(userMapper).toDTO(userEntity2);

            // Act
            var output = userService.getAllUsers();

            // Assert
            assertNotNull(output);
            assertEquals(2, output.size());
            assertEquals("João Silva", output.get(0).fullName());
            assertEquals("joao@gmail.com", output.get(0).email());
            assertEquals("Maria Santos", output.get(1).fullName());
            assertEquals("maria@gmail.com", output.get(1).email());
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver usuários")
        void DeveRetornarListaVaziaQuandoNaoHouverUsuarios() {
            // Arrange
            doReturn(List.of()).when(userRepository).findAll();

            // Act
            var output = userService.getAllUsers();

            // Assert
            assertNotNull(output);
            assertTrue(output.isEmpty());
        }
    }

    @Nested
    class GetUserById {

        @Test
        @DisplayName("Deve buscar usuário por ID com sucesso")
        void DeveBuscarUsuarioPorIdComSucesso() {
            // Arrange
            UUID userId = UUID.randomUUID();
            var userEntity = User.builder()
                    .id(userId)
                    .fullName("João Silva")
                    .cpfCnpj("12345678900")
                    .email("joao@gmail.com")
                    .password("senha123")
                    .type(UserType.COMMON)
                    .build();

            var userDTO = new UserDTO(
                    userId,
                    "João Silva",
                    "123.456.789-00",
                    null,
                    "joao@gmail.com",
                    "senha123",
                    "COMMON"
            );

            doReturn(Optional.of(userEntity)).when(userRepository).findById(userId);
            doReturn(userDTO).when(userMapper).toDTO(any(User.class));

            // Act
            var output = userService.getUserById(userId);

            // Assert
            assertNotNull(output);
            assertEquals("João Silva", output.fullName());
            assertEquals("joao@gmail.com", output.email());
        }

        @Test
        @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
        void DeveLancarExcecaoAoBuscarUsuarioInexistente() {
            // Arrange
            UUID userId = UUID.randomUUID();
            doReturn(Optional.empty()).when(userRepository).findById(userId);

            // Act & Assert
            UserNotFoundException exception = assertThrows(
                    UserNotFoundException.class,
                    () -> userService.getUserById(userId)
            );
            assertEquals("User not found with ID: " + userId, exception.getMessage());
        }
    }
}

