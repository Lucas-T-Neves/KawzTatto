# APIs — Estúdio (Contratos, Imagens e Agenda)

Backend Spring Boot **4.1.1** / Java **21** com três integrações externas, todas expostas por
endpoints REST próprios: **APITemplate.io** (PDF de contrato), **Cloudinary** (imagens) e
**Nylas** (agenda).

---

## 1. Como rodar

```bash
# perfil local (le as credenciais de src/main/resources/application-local.properties)
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# ou rodando os testes
./mvnw.cmd test
```

Swagger: <http://localhost:8080/swagger-ui.html>
Console H2: <http://localhost:8080/h2-console> (JDBC `jdbc:h2:mem:apisdb`, usuário `sa`)

> Swagger e console do H2 estão **desligados por padrão** (404) — somente o perfil
> `local` os liga. Ver nota abaixo.

> O banco é o H2 **em memória** (`jdbc:h2:mem`): contratos e imagens salvos desaparecem
> quando a aplicação para. A agenda não é afetada — o Nylas é a fonte da verdade.
>
> **Endurecimento (2026-10-10):** o console do H2, o Swagger e o `/v3/api-docs` ficam
> **desligados por padrão** (devolvem 404) — são superfície de ataque em produção.
> O perfil `local` os liga para o desenvolvimento. Para ligar em outro ambiente:
> `H2_CONSOLE_ENABLED=true` e `SPRINGDOC_ENABLED=true`.
>
> Todas as respostas saem com headers de segurança (`X-Content-Type-Options`,
> `X-Frame-Options: DENY`, `CSP`, `Referrer-Policy`, `HSTS`) via `SecurityHeadersFilter`.

> **Porta:** a aplicação sobe na **8080** por padrão. Para rodá-la junto com o projeto
> irmão `sentinela/` (que também usa a 8080), suba um dos dois em outra porta:
> `./mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"`.

## 2. Configuração

O `application.properties` **versionado** só contém referências `${VARIAVEL}` — nenhum segredo.
Os valores reais ficam em:

- `src/main/resources/application-local.properties` (perfil `local`)
- `.env`

Os dois, além do `briefing-implementacao-apis.MD`, estão no `.gitignore` e **nunca** devem ser
versionados. Testes usam valores falsos (`src/test/resources/application.properties`).

Os timeouts ficam fixos no `application.properties` (não são variáveis de ambiente):
3 s de conexão e 30 s de leitura para o APITemplate (geração de PDF), 3 s/15 s para o Nylas —
aplicados aos `RestClient`s em `RestClientConfig`. O SDK do Cloudinary tem um único timeout,
em segundos, alimentado pelo `cloudinary.read-timeout-ms` em `CloudinaryConfig`.

| Variável | Uso |
|---|---|
| `APITEMPLATE_API_KEY` | chave do APITemplate.io |
| `APITEMPLATE_TEMPLATE_ID` | id do template do contrato (**configurado no perfil local**) | vazio |
| `APITEMPLATE_BASE_URL` | base da API (padrão `https://rest.apitemplate.io/v2`) |
| `CLOUDINARY_URL` | `cloudinary://<key>:<secret>@<cloud>` |
| `CLOUDINARY_CLOUD_NAME` / `CLOUDINARY_API_KEY` / `CLOUDINARY_API_SECRET` | alternativa ao `CLOUDINARY_URL` |
| `CLOUDINARY_PASTA_PADRAO` | pasta padrão de upload (padrão `studio/`) |
| `NYLAS_API_KEY` | chave do Nylas |
| `NYLAS_GRANT_ID` | grant da agenda do estúdio |
| `NYLAS_CALENDAR_ID` | calendário (padrão `primary`) |
| `NYLAS_BASE_URL` | região US/EU (padrão `https://api.us.nylas.com`) |
| `NYLAS_NOTIFY_PARTICIPANTES` | envia e-mail aos participantes (padrão **`false`**) |
| `ESTUDIO_NOME` / `ESTUDIO_DOCUMENTO` | dados do estúdio injetados no PDF |

## 3. Endpoints

### Contratos — `/contratos`
| Método | URL | Descrição |
|---|---|---|
| POST | `/contratos` | salva, gera o PDF e devolve `pdfUrl` (201 + `Location`) |
| GET | `/contratos/{id}` | busca um contrato (404 se não existir) |
| GET | `/contratos` | lista os contratos |

### Imagens — `/imagens`
| Método | URL | Descrição |
|---|---|---|
| POST | `/imagens` | upload `multipart/form-data`, campo `arquivo`, `pasta` opcional (201) |
| GET | `/imagens` | lista as imagens |
| GET | `/imagens/{id}` | detalhe com a URL original |
| GET | `/imagens/{id}/url` | URL transformada **sem novo upload** |
| DELETE | `/imagens/{id}` | remove no Cloudinary e no banco (204) |

Parâmetros de `/imagens/{id}/url` (todos opcionais e restritos a listas fechadas):

| Parâmetro | Aceita |
|---|---|
| `largura`, `altura` | `auto` ou 1–10000 |
| `corte` | `fill`, `fit`, `crop`, `scale` |
| `qualidade` | `auto` ou 1–100 |
| `formato` | `auto`, `webp`, `jpg`, `png` |
| `efeito` | `grayscale`, `sepia`, `blur` |

### Agenda — `/agenda/eventos`
| Método | URL | Descrição |
|---|---|---|
| GET | `/agenda/eventos?inicio=&fim=` | lista no intervalo (ISO 8601, ambos opcionais) |
| POST | `/agenda/eventos` | cria evento (201) |
| PUT | `/agenda/eventos/{id}` | atualiza evento (200) |
| DELETE | `/agenda/eventos/{id}` | apaga evento (204) |

> **Participantes:** enviar `participantes: []` **remove** todos os participantes do evento
> (a lista vazia é transmitida ao Nylas; `null`/omissão seria lido como "não alterar").
> Verificado em teste ao vivo em 2026-10-09 — CRUD completo exercitado contra o calendário real.
>
> **Consistência do provedor:** um `PUT` imediatamente após um `DELETE` (204) ainda pode
> responder 200 com os dados antigos — é replicação eventual do Nylas/Google Calendar.
> A exclusão prevalece; não trate esse 200 como "evento revivido".

### Exemplo — criar contrato

```json
{
  "nomeCliente": "Lucas Neves",
  "cpfCliente": "529.982.247-25",
  "telefoneCliente": "+55(11)99999-9999",
  "nomeArtista": "Kawã",
  "descricaoTatuagem": "Cruzeiro do sul",
  "localCorpo": "Antebraço",
  "tamanhoEstimado": "GG",
  "dataInicio": "2026-09-19",
  "dataFim": "2026-10-19",
  "valorOrcado": 500.00,
  "sinalPago": true,
  "formaPagamento": "Pix"
}
```

`cpfCliente` é validado pelo algoritmo do CPF e aceita com ou sem máscara. O valor é enviado ao
template **no formato recebido**.

## 4. Tratamento de erros

Todas as respostas de erro seguem **RFC 7807** (`ProblemDetail`):

| Situação | HTTP |
|---|---|
| campo inválido | 400 com `errors: [{campo, mensagem}]` |
| período inválido (`dataFim` < `dataInicio`; `fim` ≤ `inicio` no corpo do evento ou na listagem) | 400 |
| arquivo vazio, maior que 10 MB ou que não é JPEG/PNG/WebP (validado pelo **conteúdo**) | 400 |
| parâmetro de transformação fora da lista | 400 |
| recurso inexistente no banco (contrato, imagem) | 404 |
| evento inexistente no Nylas (`PUT`/`DELETE`) | 404 |
| falha de integração | **502** |
| timeout do provedor | **504** |
| limite de requisições do provedor | **503** + `Retry-After` |

> O 404 da agenda existe porque a agenda não tem tabela própria: quando o Nylas responde 404 em
> uma chamada sobre um evento específico (`PUT`/`DELETE`), o evento realmente não existe.
> 404 na listagem ou na criação indica problema de configuração (`grant_id`/calendário)
> e continua sendo tratado como 502.

O status e o corpo **crus** do provedor nunca são repassados ao cliente: só o status traduzido,
o nome do provedor e uma mensagem genérica. O detalhe do provedor vai apenas para o log.

## 5. Arquitetura

```
Controller -> Service -> Repository (banco)      Controller -> Service -> Client (API externa)
```

- **Entities nunca aparecem em assinatura de controller**: entra e sai sempre DTO (`record`).
- Cada provedor tem um **client próprio** com os DTOs dele; o formato do provedor não vaza para o domínio.
- O mapeamento domínio → template do APITemplate fica em **um único método** (`ApiTemplateClient.mapearParaTemplate`).
- **Nenhuma transação de banco aberta durante chamada HTTP**: o serviço salva `PENDENTE`, chama o
  provedor fora de transação e depois salva `GERADO`/`FALHA`.
- Chaves só via `@ConfigurationProperties`.

## 6. Testes

```bash
./mvnw.cmd test
```

61 testes, sem nenhuma chamada às APIs reais:

| Tipo | Onde | Como |
|---|---|---|
| Clients | `ApiTemplateClientTest`, `NylasClientTest` | `MockRestServiceServer`: sucesso, 4xx, 5xx, 429, 401, timeout e 404 (Nylas, só para evento inexistente) |
| Services | `ContratoServiceTest`, `ImagemServiceTest`, `AgendaServiceTest` | Mockito mockando os clients (fluxo `PENDENTE → GERADO` e `PENDENTE → FALHA`, período inválido e resposta vazia do provedor na agenda) |
| Controllers | `ContratoControllerTest`, `ImagemControllerTest` | `@WebMvcTest`: status, validação e formato JSON |
| Config | `CloudinaryConfigTest` | bean do SDK montado a partir das propriedades (timeout em segundos) |
| Config | `SecurityHeadersFilterTest` | headers de segurança (`X-Content-Type-Options`, `X-Frame-Options: DENY`, CSP, `Referrer-Policy`, HSTS) em todas as respostas |

> O ambiente de testes tem pouca RAM: o `pom.xml` configura o surefire com JVM enxuta (`-Xmx384m`)
> e um processo isolado por classe de teste (`reuseForks=false`).

## 7. Pendências e limitações conhecidas

1. **`APITEMPLATE_TEMPLATE_ID` resolvido em 2026-10-09.** O template da conta foi
   localizado via `GET /v2/list-templates` ("New Template", formato PDF) e
   configurado no perfil `local`. O `POST /contratos` foi testado ponta a ponta e
   devolveu 201 com PDF real (42 KB). **O `application.properties` versionado
   continua lendo de `${APITEMPLATE_TEMPLATE_ID}`** — em outro ambiente, defina a
   variável antes de subir.
2. **Validade do `download_url` do APITemplate não confirmada** (a documentação da v2 não foi
   acessível). Se o link expirar, a recomendação é baixar e guardar o PDF.
3. **`estudio.nome` e `estudio.documento` não foram informados na seção 13.2** do briefing. Estão
   preenchidos com os valores do exemplo do template (`Kawz Tatto` / `<documento>`). Ajuste no
   perfil `local` quando o dono informar os valores oficiais.
4. **`tamanhoEstimado` e `formaPagamento`** são aceitos como texto livre (até 20 e 60 caracteres),
   porque a lista de valores aceitos não foi informada.
5. **Eventos de dia inteiro (`datespan`) do Nylas não são suportados**: apenas `timespan`.
   A conversão sempre usa timestamp Unix.
6. **Paginação da agenda**: o Nylas devolve até 50 eventos por requisição, ordenados pelo início.
   A listagem usa os filtros `start`/`end`, mas não pagin além disso.
7. **Chaves já expostas**: as credenciais fornecidas apareceram em conversa/log durante a
   implementação. **Recomenda-se rotacioná-las** após colocar em produção.
8. SDK do Cloudinary: usado `cloudinary-http5:2.5.0` (traz `cloudinary-core` transitivamente),
   confirmado por inspeção do JAR (`javap`). Não existe `Transformation.format()` — a saída em
   outro formato usa `fetchFormat()`.
