# Kawz Tattoo API

Backend REST em Java 21, Spring Boot 4.1.1 e Spring Data JPA para gestão do estúdio Kawz Tattoo.

## Funcionalidades

A API possui autenticação JWT, CRUD de tatuadores, clientes, projetos, atividades, anamneses, follow-ups, preferências, características de projeto, contratos, sessões, movimentações e imagens. Também oferece rotas aninhadas para consulta por cliente, projeto e tatuador, dashboard e documentação OpenAPI.

## Integrações

- **APITemplate**: geração de PDF por `POST /api/v1/contratos/{id}/pdf`.
- **Cloudinary**: upload por `POST /api/v1/projetos/{projetoId}/imagens` e remoção por `DELETE /api/v1/imagens/{id}`.
- **Nylas v3**: criação, atualização e remoção de eventos ao criar ou alterar sessões.

As credenciais são lidas exclusivamente de variáveis de ambiente. Veja `.env.example`. Sem configuração, Nylas permanece inativo; APITemplate e Cloudinary respondem HTTP 502 quando solicitados.

## Execução

É necessário definir `JWT_SECRET` com pelo menos 32 bytes:

```bash
export JWT_SECRET='um-segredo-local-com-no-minimo-32-caracteres'
./mvnw spring-boot:run
```

Para testar:

```bash
./mvnw test
```

## Banco

O padrão de desenvolvimento usa H2 em memória. Para MySQL, configure `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` e `DDL_AUTO=validate`, e aplique `db/schema.sql` previamente. A validação realizada neste pacote usa H2; a compatibilidade com uma instância MySQL real ainda exige execução nesse ambiente.
