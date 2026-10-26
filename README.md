# Kawz Tattoo

Backend REST do projeto Kawz Tattoo para gestão de clientes, projetos e agenda de um estúdio de tatuagem.

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security com JWT
- H2 para desenvolvimento e testes
- MySQL 8+ para execução com banco persistente
- OpenAPI / Swagger
- Maven Wrapper

## Funcionalidades

A API possui autenticação de tatuadores e operações para:

- tatuadores;
- clientes, incluindo CPF e dados de contato;
- anamneses;
- preferências do cliente;
- projetos;
- características do projeto;
- contratos;
- sessões de tatuagem;
- atividades e tarefas de agenda;
- follow-ups;
- movimentações financeiras;
- imagens de referência;
- dashboard e consultas agregadas.

Projetos novos já são criados com contrato e características associadas. As respostas de projeto podem incluir dados financeiros calculados, contrato, características, sessões, follow-ups, movimentações e imagens.

## Agenda e atividades

As atividades suportam os campos necessários para a tela de agenda:

- `titulo`;
- `descricao`;
- `tipo`;
- `data`;
- `inicio`;
- `termino`;
- `local`;
- `observacoes`.

O término deve ser posterior ao início. A agenda pode ser filtrada por tatuador e data:

```http
GET /api/v1/tatuadores/{tatuadorId}/atividades?data=2026-10-25
```

Os tipos de atividade são recebidos como texto. O frontend deve utilizar uma convenção única, preferencialmente `TAREFA`, `EVENTO` e `SESSAO`.

## Principais endpoints

Todas as rotas abaixo exigem JWT, exceto cadastro, login e documentação OpenAPI.

### Autenticação e tatuadores

```http
POST /api/v1/tatuadores
POST /api/v1/tatuadores/autenticacao
GET  /api/v1/tatuadores
GET  /api/v1/tatuadores/{id}
GET  /api/v1/tatuadores/me
PUT  /api/v1/tatuadores/{id}
DELETE /api/v1/tatuadores/{id}
```

### Clientes

```http
GET    /api/v1/clientes
POST   /api/v1/clientes
GET    /api/v1/clientes/{id}
PUT    /api/v1/clientes/{id}
DELETE /api/v1/clientes/{id}
GET    /api/v1/clientes/{clienteId}/anamneses
GET    /api/v1/anamneses/{id}
POST   /api/v1/anamneses
PUT    /api/v1/anamneses/{id}
DELETE /api/v1/anamneses/{id}
GET    /api/v1/clientes/{clienteId}/preferencias
PUT    /api/v1/clientes/{clienteId}/preferencias
GET    /api/v1/clientes/{clienteId}/projetos
GET    /api/v1/clientes/{clienteId}/sessoes
GET    /api/v1/clientes/{clienteId}/follow-ups
GET    /api/v1/clientes/{clienteId}/conversa
```

### Projetos e contratos

```http
GET    /api/v1/projetos
POST   /api/v1/projetos
GET    /api/v1/projetos/{id}
PUT    /api/v1/projetos/{id}
PATCH  /api/v1/projetos/{id}/status
DELETE /api/v1/projetos/{id}
GET    /api/v1/projetos/{projetoId}/detalhado
GET    /api/v1/projetos/{projetoId}/caracteristicas
PUT    /api/v1/projetos/{projetoId}/caracteristicas
GET    /api/v1/projetos/{projetoId}/contrato
GET    /api/v1/projetos/{projetoId}/sessoes
GET    /api/v1/projetos/{projetoId}/follow-ups
GET    /api/v1/projetos/{projetoId}/movimentacoes/{id}
GET    /api/v1/contratos
GET    /api/v1/contratos/{id}
POST   /api/v1/contratos
PUT    /api/v1/contratos/{id}
POST   /api/v1/contratos/{id}/pdf
PATCH  /api/v1/contratos/{id}/assinatura
```

### Agenda, sessões e follow-ups

```http
GET    /api/v1/atividades/{id}
POST   /api/v1/atividades
PUT    /api/v1/atividades/{id}
DELETE /api/v1/atividades/{id}
GET    /api/v1/tatuadores/{tatuadorId}/atividades
GET    /api/v1/sessoes
POST   /api/v1/sessoes
GET    /api/v1/sessoes/{id}
PUT    /api/v1/sessoes/{id}
PATCH  /api/v1/sessoes/{id}/remarcar
DELETE /api/v1/sessoes/{id}
GET    /api/v1/sessoes/calendario
GET    /api/v1/tatuadores/{tatuadorId}/follow-ups
GET    /api/v1/follow-ups
POST   /api/v1/follow-ups
GET    /api/v1/follow-ups/{id}
PUT    /api/v1/follow-ups/{id}
DELETE /api/v1/follow-ups/{id}
```

### Financeiro, imagens e dashboard

```http
GET    /api/v1/movimentacoes
POST   /api/v1/movimentacoes
GET    /api/v1/movimentacoes/{id}
PUT    /api/v1/movimentacoes/{id}
DELETE /api/v1/movimentacoes/{id}
GET    /api/v1/movimentacoes/resumo?mes=2026-10
GET    /api/v1/projetos/{projetoId}/movimentacoes
GET    /api/v1/clientes/{clienteId}/movimentacoes
GET    /api/v1/projetos/{projetoId}/imagens
POST   /api/v1/projetos/{projetoId}/imagens
GET    /api/v1/imagens/{id}
POST   /api/v1/imagens
PUT    /api/v1/imagens/{id}
DELETE /api/v1/imagens/{id}
GET    /api/v1/dashboard
```

O upload multipart utiliza o campo `arquivo`:

```bash
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  -F "arquivo=@referencia.png" \
  http://localhost:8080/api/v1/projetos/1/imagens
```

## Integrações externas

As integrações ficam atrás da API REST interna e não expõem o formato específico de cada fornecedor ao frontend.

- **APITemplate.io:** gera o PDF do contrato em `POST /api/v1/contratos/{id}/pdf`, grava `pdfUrl` e `transactionRef`.
- **Cloudinary:** realiza upload e remoção de imagens, retornando a URL pública para o frontend.
- **Nylas v3:** cria, atualiza e remove eventos de calendário associados às sessões.

Sem credenciais:

- Nylas permanece inativo e as sessões continuam sendo persistidas localmente.
- APITemplate retorna `502` quando solicitado.
- Cloudinary retorna `502` quando solicitado.

Falhas de serviço externo são representadas como `502`, sem serem confundidas com erro interno `500`.

## Configuração

As credenciais não devem ser colocadas no repositório. Configure variáveis de ambiente.

### Obrigatórias para iniciar

```bash
export JWT_SECRET='um-segredo-local-com-no-minimo-32-caracteres'
```

### Banco

```bash
export DATABASE_URL='jdbc:h2:mem:kawztattoo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH'
export DATABASE_USERNAME='sa'
export DATABASE_PASSWORD=''
export DDL_AUTO='update'
```

Para MySQL, configure a URL, usuário e senha correspondentes, aplique `db/schema.sql` e use:

```bash
export DDL_AUTO='validate'
```

O schema MySQL precisa ser migrado antes de executar a versão local em um banco que já possua dados. A versão atual inclui CPF, campos de contrato, vínculo de evento de calendário e dados adicionais da view de contratos.

### Integrações

```bash
export APITEMPLATE_BASE_URL='https://rest.apitemplate.io'
export APITEMPLATE_API_KEY=''
export APITEMPLATE_TEMPLATE_ID=''

export CLOUDINARY_CLOUD_NAME=''
export CLOUDINARY_API_KEY=''
export CLOUDINARY_API_SECRET=''
export CLOUDINARY_PASTA='kawz-tattoo'

export NYLAS_BASE_URL='https://api.us.nylas.com'
export NYLAS_API_KEY=''
export NYLAS_GRANT_ID=''
export NYLAS_CALENDAR_ID='primary'
export NYLAS_FUSO='America/Sao_Paulo'
export NYLAS_DURACAO_HORAS='3'
```

Também é possível configurar `SERVER_PORT`, `JWT_EXPIRA_MINUTOS`, `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` por ambiente.

## Como executar

Linux/macOS:

```bash
export JWT_SECRET='um-segredo-local-com-no-minimo-32-caracteres'
./mvnw spring-boot:run
```

Windows:

```powershell
$env:JWT_SECRET='um-segredo-local-com-no-minimo-32-caracteres'
.\mvnw.cmd spring-boot:run
```

A API inicia por padrão em:

```text
http://localhost:8080
```

Documentação OpenAPI:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

## Banco e DER

O `db/schema.sql` segue o DER. Acréscimos fora do DER: `sessao.evento_calendario_id` (usada pelo Nylas) e as colunas `titulo`, `data_atividade`, `hora_inicio`, `hora_termino`, `local` e `observacoes` em `atividade` (agenda do frontend).

## Segurança e arquivos locais

`JWT_SECRET` é obrigatória (mínimo de 32 caracteres). Chaves das integrações vêm só de variáveis de ambiente; use o `.env.example` como modelo. O `.gitignore` deve ficar na raiz do repositório e ignora `.env`, `.env.*` (exceto `.env.example`) e `application-local.*`.

## Testes

Execute a suíte completa com compilação limpa:

```bash
./mvnw clean test
```

Estado validado da versão atual:

```text
33 testes executados (rode para confirmar)
0 falhas
0 erros
BUILD SUCCESS
```

A cobertura inclui contexto Spring, endpoints HTTP, autenticação, agenda, regras de validação, respostas de erro e clientes simulados para APITemplate, Cloudinary e Nylas.

## Contrato de erros

Os erros seguem `ProblemDetail` e devem ser tratados pelo frontend pelos códigos HTTP:

- `400`: payload ou formato inválido;
- `401`: token ausente, inválido ou expirado;
- `404`: recurso não encontrado;
- `422`: regra de negócio violada;
- `502`: integração externa indisponível ou sem configuração.

Uma resposta vazia com HTTP `200` representa uma consulta válida sem registros. O frontend não deve tratar lista vazia como erro de autenticação ou falha de rede.

## Autenticação e escopo

Fora do escopo atual: não há isolamento de dados por conta. Qualquer tatuador autenticado acessa todos os recursos; o JWT só é usado para proteger as rotas e em `/api/v1/tatuadores/me`.

## Pendências antes da produção

- Executar migração controlada no MySQL real.
- Testar APITemplate, Cloudinary e Nylas com credenciais reais de ambiente.
- Confirmar com o frontend os nomes e tipos dos DTOs de projeto, financeiro, preferências, agenda e imagens.
- Executar os testes do frontend e validar o fluxo ponta a ponta de upload e agenda.
