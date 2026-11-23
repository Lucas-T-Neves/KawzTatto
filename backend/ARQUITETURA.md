# Organização do backend

O pacote `com.kawz.tattoo` combina camadas com agrupamento por funcionalidade:

- `api/<funcionalidade>`: controladores, DTOs e mapeadores da API. Os controladores são adaptadores HTTP finos: leem o tatuador autenticado, chamam um caso de uso/serviço e convertem o resultado em resposta. `api/exception`, `api/web` e `api/shared` reúnem componentes compartilhados.
- `application/<funcionalidade>`: casos de uso que orquestram mais de um agregado e portas (interfaces) de integração. Hoje contém `application/projeto` (leitura composta da tela de projeto detalhado), `application/security` (identidade do tatuador autenticado) e `application/gateway` (agenda e armazenamento de imagens).
- `domain/<funcionalidade>`: entidades, serviços de regra de negócio e repositórios. Exceções, segurança e a entidade base compartilhadas têm pacotes próprios.
- `infrastructure/{config,security,integration}`: configuração do Spring, implementações de segurança e adaptadores dos serviços externos (APITemplate, Cloudinary, Nylas).

Os repositórios continuam junto das funcionalidades, pois são interfaces Spring Data JPA; não há uma implementação de persistência separada que justifique criar um pacote vazio. A projeção da consulta de contratos fica com seu repositório.

## Direção das dependências

```
api  ->  application  ->  domain
 |                            ^
 +-------> infrastructure ----+
```

O piloto da camada `application` é o fluxo de projeto detalhado:

- `api/projeto/ProjetoEscopoController` recebe `GET /api/v1/projetos/{id}/detalhado` e delega;
- `application/projeto/BuscarProjetoDetalhadoUseCase` busca o projeto com contrato e características em uma única consulta (`ProjetoRepository.buscarComDetalhesDoTatuador`), soma o recebido, verifica o sinal e carrega sessões, movimentações, follow-ups e imagens;
- `application/projeto/ProjetoDetalhadoResult` devolve objetos de domínio, não DTOs;
- `api/projeto/ProjetoDetalhadoMapper` converte o resultado no `ProjetoDetalhadoResponse` que o frontend já consome.

O controller deixou de injetar sete serviços e passou a ter uma única dependência de caso de uso. As demais funcionalidades ainda seguem o padrão anterior (controller → serviço de domínio); a migração é incremental e não altera contratos HTTP.

## Portas e adaptadores de integração

Serviços de domínio não conhecem mais os clientes HTTP:

| Porta (`application/gateway`) | Adaptador (`infrastructure/integration`) | Cliente externo |
| --- | --- | --- |
| `AgendaGateway` | `NylasAgendaGateway` | `NylasCalendarClient` |
| `ArmazenamentoImagensGateway` | `CloudinaryArmazenamentoImagensGateway` | `CloudinaryClient` |

Com isso, `SessaoService`, `ImagemService` e `ProjetoService` dependem de interfaces. Os adaptadores concentram a tradução de erros do fornecedor para `IntegracaoException` (HTTP 502), mantendo a política de falha fora das regras de negócio.

## Propriedade dos dados (autorização por tatuador)

A regra é: **todo recurso pertence a um tatuador e só é visível para ele**.

- `Cliente` tem `tatuador_id` obrigatório; é a raiz da posse de clientes, anamneses, preferências e follow-ups.
- `Projeto` tem `tatuador_id` e é a raiz da posse de contrato, características, sessões, movimentações, orçamentos e imagens.
- `Atividade` tem `tatuador_id` direto.

O fluxo é sempre o mesmo:

1. `JwtAuthenticationFilter` valida o token e publica um `TatuadorPrincipal(id, email)` na `SecurityContext`.
2. O controlador obtém o id pela porta `TatuadorAutenticado` (adaptador `ContextoSegurancaTatuadorAutenticado`).
3. O serviço recebe `tatuadorId` explicitamente e consulta repositórios já filtrados pelo dono.
4. Se o recurso não existe **ou** pertence a outro tatuador, o serviço lança `ResourceNotFoundException` → **404** (nunca 403), sem revelar a existência do dado alheio.

Campos `tatuadorId` enviados pelo frontend em projetos e atividades continuam aceitos, mas são ignorados: o dono vem sempre do token. Clientes não enviam esse campo; o dono é derivado da autenticação.

## Segurança

- `JwtProperties` (`kawz.jwt.*`) valida o segredo na inicialização: ausente ou com menos de 32 bytes, a aplicação não sobe.
- O token carrega `sub` (e-mail) e `id`; o filtro rejeita token inválido/expirado com 401 e `ProblemDetail`.
- CORS restrito a `kawz.cors.origens` (variável `CORS_ORIGENS`), com métodos e cabeçalhos explícitos.
- Rotas públicas: `POST /api/v1/tatuadores`, `POST /api/v1/tatuadores/autenticacao` e a documentação OpenAPI. Todo o resto exige JWT.

## Perfis de configuração

| Perfil | Banco | DDL | Mocks | Segredo |
| --- | --- | --- | --- | --- |
| `local` (padrão) | H2 em memória | `update` | `dados-mockados.sql` | valor de desenvolvimento no `application-local.yml` |
| `prod` | MySQL (`DATABASE_URL`) | `validate` | não | `JWT_SECRET` obrigatório, sem padrão |
| `test` | H2 isolado por contexto | `create-drop` | apenas `schema.sql`, sem fixtures | segredo de teste |

`application.yml` contém só o comum (`spring.profiles.default: local`, CORS, JWT, integrações). No perfil `prod`, `ValidacaoAmbienteProducao` falha antes de abrir conexão e lista as variáveis ausentes (`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`).

## Contrato de erros

`ApiExceptionHandler` responde `ProblemDetail` (RFC 9457) e mantém o formato já consumido pelo frontend:

- `400` validação de payload;
- `401` token ausente/inválido (`NaoAutenticadoException`);
- `404` recurso inexistente ou de outro tatuador;
- `422` regra de negócio;
- `502` integração externa indisponível ou sem configuração.
