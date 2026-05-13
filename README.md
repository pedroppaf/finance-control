# Finance Control 💰

Este projeto é uma API para controle financeiro pessoal, construída usando Java, Spring Boot, PostgreSQL como banco de dados, e Spring Security e JWT para controle de autenticação.


### Funcionalidades Principais
- 🔐 **Autenticação:** Registro e login com JWT (access token + refresh token)
- 🔄 **Refresh Token:** Rotação automática (revoga token antigo, emite novo)
- 🚪 **Logout:** Revogação segura de refresh tokens
- 👤 **Roles & Permissões:** Controle de acesso baseado em roles (USER, ADMIN)
- 💳 **Transações:** CRUD completo com filtros por tipo, data e paginação
- 📊 **Resumo Financeiro:** Totalizações de receita, despesa e saldo

---

## 📋 Pré-requisitos

- **Java 25+**
- **Maven 3.8+**
- **PostgreSQL 12+** (ou H2 em testes)
- **Git**

---

## Índice

- Instalação
- Uso
- Endpoints da API
- Autenticação
- Banco de Dados
- Contribuindo

## Instalação

Clone o repositório:

```bash
git clone https://github.com/pedroppaf/finance-control.git
```

Instale as dependências com Maven:

```bash
mvn clean install
```

Instale o PostgreSQL

## Uso

Inicie a aplicação com Maven

A API estará acessível em `http://localhost:8080`

Acesse a UI do Swagger em `http://localhost:8080/swagger-ui/index.html`

## Endpoints da API

A API fornece os seguintes endpoints:

**Autenticação:**
- `POST /auth/register` - Registrar um novo usuário
- `POST /auth/login` - Fazer login na aplicação
- `POST /auth/refresh` - Renovar token de acesso
- `POST /auth/logout` - Fazer logout e revogar refresh token

**Transações (todos os usuários autenticados):**
- `GET /transactions` - Listar todas as transações
- `POST /transactions` - Criar uma nova transação
- `GET /transactions/{id}` - Obter transação por ID
- `PUT /transactions/{id}` - Atualizar uma transação
- `DELETE /transactions/{id}` - Deletar uma transação
- `GET /transactions/summary` - Obter resumo financeiro

## Autenticação

A API usa Spring Security para controle de autenticação. Os seguintes papéis estão disponíveis:

- **USER** → Papel de usuário padrão para usuários conectados (podem acessar transações)
- **ADMIN** → Papel de administrador para gerenciar recursos do sistema

Para acessar endpoints protegidos, forneça um token JWT válido no header de Autorização:

```
Authorization: Bearer <seu-jwt-token>
```

## Banco de Dados

O projeto usa PostgreSQL como banco de dados com Hibernate para gerenciamento de ORM.

Entidades principais:
- **users** - Contas de usuário com email, senha e papel
- **refresh_tokens** - Tokens de refresh armazenados para rotação de tokens
- **transactions** - Transações financeiras com tipo, categoria e valor

## Contribuindo

Contribuições são bem-vindas! Se você encontrar algum problema ou tiver sugestões de melhorias, abra uma issue ou envie um pull request para o repositório.

Ao contribuir para este projeto, siga o estilo de código existente e envie suas alterações em um branch separado.

---
