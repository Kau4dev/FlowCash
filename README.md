# 💰 FlowCash - Sistema de Transferências Bancárias

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-3-orange.svg)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED.svg)](https://www.docker.com/)

Sistema de transferências bancárias desenvolvido com arquitetura de microserviços, implementando comunicação síncrona via Feign Client e assíncrona via RabbitMQ.

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Arquitetura](#-arquitetura)
- [Tecnologias Utilizadas](#-tecnologias-utilizadas)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Configuração](#-instalação-e-configuração)
- [Microserviços](#-microserviços)
- [Fluxo de Transferência](#-fluxo-de-transferência)
- [Endpoints da API](#-endpoints-da-api)
- [Boas Práticas Implementadas](#-boas-práticas-implementadas)
- [Documentação da API](#-documentação-da-api)
- [Autor](#-autor)

## 🎯 Sobre o Projeto

FlowCash é um sistema completo de gerenciamento de transferências bancárias que simula operações financeiras entre usuários comuns e lojistas. O sistema garante a integridade das transações através de validações em múltiplas camadas e autorização externa.

### Principais Funcionalidades

- ✅ Cadastro e gerenciamento de usuários (Pessoas Físicas e Jurídicas)
- 💼 Gestão de carteiras digitais com controle de saldo
- 💸 Transferências entre usuários com validações de negócio
- 🔒 Autorização externa para cada transferência
- 🔔 Sistema de notificações assíncronas via RabbitMQ
- 📊 Controle de versão otimista para evitar condições de corrida
- 🎯 Validação de dados com Bean Validation
- 📝 Documentação automática com Swagger/OpenAPI

## 🏗️ Arquitetura

O projeto utiliza arquitetura de microserviços, com separação clara de responsabilidades:

```
flowCash/
├── ms-user/          # Microserviço de Usuários
├── ms-wallet/        # Microserviço de Carteiras
├── ms-transfer/      # Microserviço de Transferências
├── ms-notification/  # Microserviço de Notificações
├── postgres-data/    # Volume persistente do PostgreSQL
└── docker-compose.yml
```

### Diagrama de Comunicação

```
┌─────────────┐
│   Cliente   │
└──────┬──────┘
       │ HTTP
       ▼
┌─────────────────┐     Feign      ┌──────────────┐
│  ms-transfer    │◄───────────────►│   ms-user    │
│  (Orquestrador) │                 └──────────────┘
└────────┬────────┘
         │ Feign
         ▼
┌─────────────────┐
│   ms-wallet     │
│ (Saldo/Versão)  │
└────────┬────────┘
         │ RabbitMQ
         ▼
┌─────────────────┐
│ ms-notification │
│  (Email/SMS)    │
└─────────────────┘
```

## 🛠️ Tecnologias Utilizadas

### Backend
- **Spring Boot 3.4.1** - Framework principal
- **Java 21** - Linguagem de programação
- **Spring Data JPA** - Persistência de dados
- **Spring Cloud OpenFeign** - Comunicação entre microserviços
- **Spring AMQP** - Integração com RabbitMQ
- **MapStruct 1.5.5** - Mapeamento de objetos
- **Lombok** - Redução de código boilerplate
- **Bean Validation** - Validação de dados

### Banco de Dados
- **PostgreSQL 17** - Banco de dados relacional
- Cada microserviço possui seu próprio banco de dados

### Mensageria
- **RabbitMQ 3** - Message broker para comunicação assíncrona

### Documentação
- **SpringDoc OpenAPI 3** - Geração automática de documentação

### DevOps
- **Docker & Docker Compose** - Containerização e orquestração

## 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- [Java JDK 21+](https://www.oracle.com/java/technologies/downloads/)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- [Git](https://git-scm.com/)
- IDE de sua preferência (IntelliJ IDEA, Eclipse, VS Code)

## 🚀 Instalação e Configuração

### 1. Clone o Repositório

```bash
git clone https://github.com/seu-usuario/flowCash.git
cd flowCash
```

### 2. Inicie os Serviços de Infraestrutura

```bash
docker-compose up -d
```

Isso iniciará:
- **PostgreSQL** na porta `5435`
- **RabbitMQ** nas portas `5672` (AMQP) e `15672` (Management UI)

### 3. Verifique os Serviços

- **RabbitMQ Management**: http://localhost:15672
  - Usuário: `admin`
  - Senha: `123`

- **Bancos de Dados PostgreSQL**:
  - Host: `localhost:5435`
  - Usuário: `postgres`
  - Senha: `postgres`
  - Databases: `ms_user`, `ms_wallet`, `ms_transfer`

### 4. Compile os Microserviços

```bash
# Em cada diretório de microserviço
cd ms-user && mvn clean install
cd ../ms-wallet && mvn clean install
cd ../ms-transfer && mvn clean install
cd ../ms-notification && mvn clean install
```

### 5. Inicie os Microserviços

**Ordem recomendada:**

```bash
# Terminal 1 - ms-user
cd ms-user
mvn spring-boot:run

# Terminal 2 - ms-wallet
cd ms-wallet
mvn spring-boot:run

# Terminal 3 - ms-notification
cd ms-notification
mvn spring-boot:run

# Terminal 4 - ms-transfer
cd ms-transfer
mvn spring-boot:run
```

### 6. Verifique a Saúde dos Serviços

- ms-user: http://localhost:8081
- ms-wallet: http://localhost:8082
- ms-transfer: http://localhost:8083
- ms-notification: http://localhost:8084

## 🔧 Microserviços

### 1️⃣ MS-User (Porta 8081)

**Responsabilidade**: Gerenciamento de usuários

**Entidades**:
- `User`: Representa usuários do sistema (CPF ou CNPJ)

**Regras de Negócio**:
- ✅ CPF ou CNPJ obrigatório (mutuamente exclusivos)
- ✅ Email único no sistema
- ✅ CPF/CNPJ único no sistema
- ✅ Validação de formato de dados

**Estrutura do Projeto**:
```
com.kau4dev.user
├── controller/       # Endpoints REST
├── service/          # Lógica de negócio
├── repository/       # Acesso aos dados
├── model/
│   ├── entity/      # Entidades JPA
│   ├── dto/         # Data Transfer Objects
│   ├── mapper/      # MapStruct mappers
│   └── enums/       # Enumerações
└── infra/
    └── exception/   # Exceções personalizadas
```

### 2️⃣ MS-Wallet (Porta 8082)

**Responsabilidade**: Gerenciamento de carteiras e saldos

**Entidades**:
- `Wallet`: Carteira digital vinculada a um usuário

**Recursos**:
- 💰 Controle de saldo
- 🔄 Versionamento otimista (`@Version`)
- 🔒 Transações atômicas

**Estrutura do Projeto**:
```
com.kau4dev.wallet
├── controller/       # Endpoints REST
├── service/          # Lógica de negócio
├── repository/       # Acesso aos dados
├── model/
│   ├── entity/      # Entidades JPA
│   └── dto/         # Data Transfer Objects
└── infra/
    └── exception/   # Exceções personalizadas
```

### 3️⃣ MS-Transfer (Porta 8083)

**Responsabilidade**: Orquestração de transferências

**Entidades**:
- `Transfer`: Registro de transferências
- `Status`: Enum (PENDING, SUCCESS, ERROR)

**Regras de Negócio**:
- ✅ Lojistas não podem enviar dinheiro
- ✅ Autorização externa obrigatória
- ✅ Validação de saldo antes da transferência
- ✅ Notificação assíncrona após sucesso

**Integrações**:
- **Feign Clients**:
  - `UserFeignClient`: Consulta dados de usuários
  - `WalletFeignClient`: Atualiza saldos
  - `AuthorizationFeignClient`: Valida autorização externa

- **RabbitMQ**:
  - `NotificationProducer`: Envia mensagens de notificação

**Estrutura do Projeto**:
```
com.kau4dev.transfer
├── controller/          # Endpoints REST
├── service/             # Lógica de negócio
├── repository/          # Acesso aos dados
├── model/
│   ├── domain/         # Entidades JPA
│   ├── dto/            # Data Transfer Objects
│   └── enums/          # Enumerações
├── infra/
│   ├── client/         # Feign Clients
│   ├── queue/          # RabbitMQ Producer
│   └── exception/      # Exceções personalizadas
└── config/             # Configurações
```

### 4️⃣ MS-Notification (Porta 8084)

**Responsabilidade**: Envio de notificações

**Funcionalidades**:
- 📧 Simulação de envio de emails
- 📱 Log de notificações
- 🔄 Consumo de mensagens do RabbitMQ

**Estrutura do Projeto**:
```
com.kau4dev.notification
├── service/          # Lógica de notificação
├── model/
│   └── dto/         # Data Transfer Objects
├── infra/
│   └── queue/       # RabbitMQ Consumer
└── config/          # Configurações RabbitMQ
```

## 🔄 Fluxo de Transferência

### Sequência Completa de uma Transferência

```
1. Cliente envia POST /api/transfers
   ↓
2. ms-transfer valida dados básicos
   ↓
3. Consulta dados do PAGADOR no ms-user (Feign)
   ├─ Valida se é COMMON (não pode ser MERCHANT)
   ↓
4. Consulta dados do BENEFICIÁRIO no ms-user (Feign)
   ├─ Valida se usuário existe
   ↓
5. Solicita autorização externa (Feign)
   ├─ Se negado: lança exceção
   ↓
6. Processa transferência no ms-wallet (Feign)
   ├─ Debita do pagador
   ├─ Credita no beneficiário
   ├─ Usa @Version para controle de concorrência
   ↓
7. Salva registro da transferência (status: SUCCESS)
   ↓
8. Envia mensagem para RabbitMQ
   ├─ Queue: notification-queue
   ├─ Exchange: transfer-exchange
   ├─ Routing Key: notification.routing.key
   ↓
9. ms-notification consome mensagem
   ├─ Envia email/notificação (simulado)
   ├─ Loga operação
   ↓
10. Retorna transferência confirmada ao cliente
```

### Tratamento de Erros

- **Erro de Validação**: Retorna 400 Bad Request
- **Usuário não encontrado**: Retorna 404 Not Found
- **Saldo insuficiente**: Retorna 400 Bad Request
- **Autorização negada**: Retorna 403 Forbidden
- **Erro interno**: Retorna 500 Internal Server Error

Todos os erros seguem o padrão:

```json
{
  "status": 400,
  "message": "Descrição do erro",
  "errors": ["detalhes adicionais"],
  "timestamp": "2026-01-06T12:00:00"
}
```

## 📡 Endpoints da API

### MS-User (http://localhost:8081)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/users` | Criar novo usuário |
| GET | `/api/users` | Listar todos os usuários |
| GET | `/api/users/{id}` | Buscar usuário por ID |

**Exemplo - Criar Usuário (Pessoa Física)**:
```json
POST /api/users
{
  "firstName": "João",
  "lastName": "Silva",
  "cpf": "123.456.789-00",
  "email": "joao@email.com",
  "type": "COMMON"
}
```

**Exemplo - Criar Usuário (Pessoa Jurídica)**:
```json
POST /api/users
{
  "firstName": "Tech Store",
  "lastName": "LTDA",
  "cnpj": "12.345.678/0001-00",
  "email": "contato@techstore.com",
  "type": "MERCHANT"
}
```

### MS-Wallet (http://localhost:8082)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/wallets` | Criar nova carteira |
| GET | `/api/wallets/{id}` | Buscar carteira por ID |
| GET | `/api/wallets/user/{idUser}` | Buscar carteira por ID do usuário |
| GET | `/api/wallets/{idUser}/balance` | Consultar saldo |

**Exemplo - Criar Carteira**:
```json
POST /api/wallets
{
  "idUser": "e99a0b4b-ccae-4355-b36f-68b308691ae8",
  "balance": 1000.00
}
```

### MS-Transfer (http://localhost:8083)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/transfers` | Executar transferência |

**Exemplo - Executar Transferência**:
```json
POST /api/transfers
{
  "payerId": "e99a0b4b-ccae-4355-b36f-68b308691ae8",
  "payeeId": "24fe6ba6-8349-4910-a84b-91f3254046d6",
  "amount": 100.00
}
```

**Resposta de Sucesso**:
```json
{
  "id": "632a1d65-1593-45fa-a2bd-516822a69070",
  "payerId": "e99a0b4b-ccae-4355-b36f-68b308691ae8",
  "payeeId": "24fe6ba6-8349-4910-a84b-91f3254046d6",
  "amount": 100.00,
  "status": "SUCCESS",
  "timestamp": "2026-01-06T10:30:00"
}
```

## ✨ Boas Práticas Implementadas

### 🏛️ Arquitetura

- ✅ **Separation of Concerns**: Cada microserviço tem responsabilidade única
- ✅ **Clean Architecture**: Divisão em camadas (Controller, Service, Repository)
- ✅ **Database per Service**: Cada microserviço possui seu próprio banco de dados
- ✅ **API Gateway Pattern**: Centralização de requisições (via ms-transfer)

### 💻 Código

- ✅ **DTOs**: Separação entre entidades e objetos de transferência
- ✅ **MapStruct**: Mapeamento automático entre DTOs e Entidades
- ✅ **Lombok**: Redução de código boilerplate
- ✅ **Builder Pattern**: Construção de objetos complexos
- ✅ **Optional**: Tratamento seguro de valores nulos
- ✅ **Records**: Para DTOs imutáveis (Java 21)

### 🔒 Segurança e Validação

- ✅ **Bean Validation**: Validação de dados de entrada
- ✅ **Exceções Customizadas**: Tratamento específico de erros
- ✅ **Global Exception Handler**: Centralização do tratamento de exceções
- ✅ **Optimistic Locking**: Controle de concorrência com `@Version`

### 🗄️ Banco de Dados

- ✅ **Transações ACID**: Garantia de integridade
- ✅ **@Transactional**: Controle transacional
- ✅ **UUID**: Identificadores universalmente únicos
- ✅ **Índices**: Otimização de consultas
- ✅ **Constraints**: Validação em nível de banco

### 🔄 Comunicação

- ✅ **Feign Client**: Comunicação síncrona REST entre microserviços
- ✅ **RabbitMQ**: Comunicação assíncrona para notificações
- ✅ **Message Converter**: Serialização JSON automática
- ✅ **Retry Pattern**: (Pode ser implementado com Resilience4j)

### 📝 Documentação

- ✅ **OpenAPI/Swagger**: Documentação interativa da API
- ✅ **JavaDoc**: Documentação de código
- ✅ **README Completo**: Documentação do projeto

### 🧪 Testes (A implementar)

- ⏳ Unit Tests com JUnit 5
- ⏳ Integration Tests
- ⏳ TestContainers para testes com banco de dados

## 📚 Documentação da API

Acesse a documentação interativa (Swagger UI) de cada microserviço:

- **ms-user**: http://localhost:8081/swagger-ui.html
- **ms-wallet**: http://localhost:8082/swagger-ui.html
- **ms-transfer**: http://localhost:8083/swagger-ui.html
- **ms-notification**: http://localhost:8084/swagger-ui.html

## 🔍 Nomenclatura e Convenções

### Padrões Seguidos

- **Variáveis**: `camelCase` (userId, idUser)
- **Classes**: `PascalCase` (UserService, WalletController)
- **Constantes**: `UPPER_SNAKE_CASE` (QUEUE_NAME, ROUTING_KEY)
- **Pacotes**: `lowercase` (com.kau4dev.user.service)
- **DTOs**: Sufixo `DTO` (UserDTO, TransferDTO)
- **Exceções**: Sufixo `Exception` (UserNotFoundException)
- **Repositories**: Sufixo `Repository` (UserRepository)
- **Services**: Sufixo `Service` (TransferService)
- **Controllers**: Sufixo `Controller` (WalletController)

### Decisões de Design

#### userId vs idUser
Ambos são válidos, mas o projeto usa `idUser` para:
- Consistência com o padrão de banco de dados (`id_user`)
- Facilidade de mapeamento JPA
- Nomenclatura mais próxima do SQL

#### amount vs value
O projeto usa `amount` para valores monetários porque:
- É mais específico para contexto financeiro
- Padrão em APIs de pagamento (Stripe, PayPal)
- Diferencia de `value` (mais genérico)

## 🐳 Docker e Containerização

### Serviços Docker

```yaml
# PostgreSQL
- Container: flowcash-db
- Porta: 5435:5432
- Databases: ms_user, ms_wallet, ms_transfer

# RabbitMQ
- Container: flowcash-rabbitmq
- Portas: 5672 (AMQP), 15672 (Management)
- Credenciais: admin/123
```

### Comandos Úteis

```bash
# Iniciar todos os serviços
docker-compose up -d

# Parar todos os serviços
docker-compose down

# Ver logs
docker-compose logs -f

# Ver logs de um serviço específico
docker-compose logs -f database
docker-compose logs -f rabbitmq

# Reiniciar um serviço
docker-compose restart database

# Limpar volumes (⚠️ apaga dados)
docker-compose down -v
```

## 🔧 Configurações

### application.properties (ms-transfer)

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5435/ms_transfer
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=123

# Feign Clients
ms-user.url=http://localhost:8081
ms-wallet.url=http://localhost:8082
authorization.service.url=https://testbanco.free.beeceptor.com/auth

# Server
server.port=8083
```

## 🚨 Troubleshooting

### Problema: Microserviço não inicia

**Solução**:
```bash
# Verifique se as portas estão disponíveis
netstat -ano | findstr :8081
netstat -ano | findstr :8082
netstat -ano | findstr :8083

# Compile novamente
mvn clean install
```

### Problema: Erro de conexão com PostgreSQL

**Solução**:
```bash
# Verifique se o container está rodando
docker ps

# Reinicie o PostgreSQL
docker-compose restart database

# Verifique os logs
docker-compose logs database
```

### Problema: RabbitMQ não conecta

**Solução**:
```bash
# Verifique o container
docker ps | grep rabbitmq

# Acesse o management
# http://localhost:15672 (admin/123)

# Verifique as credenciais no application.properties
```

### Problema: Feign Client timeout

**Solução**:
- Verifique se todos os microserviços estão rodando
- Confirme as URLs no application.properties
- Aumente o timeout do Feign (se necessário)

## 📈 Melhorias Futuras

- [ ] Implementar testes unitários e de integração
- [ ] Adicionar Spring Cloud Config Server
- [ ] Implementar Service Discovery (Eureka)
- [ ] Adicionar Circuit Breaker (Resilience4j)
- [ ] Implementar API Gateway (Spring Cloud Gateway)
- [ ] Adicionar autenticação e autorização (Spring Security + JWT)
- [ ] Implementar logging centralizado (ELK Stack)
- [ ] Adicionar métricas e monitoramento (Prometheus + Grafana)
- [ ] Implementar cache distribuído (Redis)
- [ ] Adicionar CI/CD pipeline
- [ ] Dockerizar os microserviços
- [ ] Deploy em Kubernetes

## 🤝 Contribuindo

Contribuições são bem-vindas! Para contribuir:

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.

## 👨‍💻 Autor

**Kauã victor** - [kau4dev](https://github.com/kau4dev)

---

⭐ **Se este projeto foi útil para você, considere dar uma estrela!**

📧 **Dúvidas ou sugestões?** Abra uma issue!

💼 **LinkedIn**: [Conecte-se comigo](https://linkedin.com/in/kaua-victor)

🌐 **Portfólio**: [seu-portfolio.com](https://seu-portfolio.com)

---

**Desenvolvido com ☕ e ❤️ usando Spring Boot**

