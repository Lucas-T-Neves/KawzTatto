# Kawz Tattoo API — Sprint 2

Primeira versão do back-end do sistema Kawz Tattoo, desenvolvida com Java 21,
Spring Boot e Spring Data JPA.

## Entregue nesta sprint

- estrutura inicial em camadas;
- mapeamento das entidades do diagrama de classes;
- banco H2 em memória;
- CRUD de tatuadores, clientes e projetos;
- validações básicas e cálculo do saldo do projeto;
- autenticação inicial por e-mail e senha.

## Ainda pendente

Follow-ups, agenda de sessões, contratos, movimentações financeiras, upload de
imagens, autenticação com token, PostgreSQL e documentação Swagger serão
desenvolvidos nas próximas sprints.

## Executar

```bash
./mvnw spring-boot:run
```

A API será iniciada em `http://localhost:8080/api/v1`.

Para executar o teste inicial:

```bash
./mvnw test
```
