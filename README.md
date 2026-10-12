# KawzTatto

Repositório para o versionamento do projeto KawzTatto.

## Projetos

| Pasta | O que é | Stack |
|---|---|---|
| `ATA generator/` | Gerador de atas de reunião do grupo | Python |
| `apis/` | Backend do estúdio: integrações externas atrás de uma API REST própria | Java 21 + Spring Boot 4.1.1 |

## apis/ — integrações do estúdio

Centraliza três serviços externos atrás de endpoints REST próprios — o cliente
só conversa com o padrão interno (DTOs, ISO 8601, erros RFC 7807):

| Integração | O que faz |
|---|---|
| **APITemplate.io** | Gera o PDF do contrato e devolve o link de download |
| **Cloudinary** | Sobe a imagem uma vez e devolve versões editadas por transformação na URL (sem novo upload) |
| **Nylas v3** | Agenda: listar, criar, atualizar e apagar eventos (a agenda é a fonte da verdade) |

### Como rodar

```bash
cd apis
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
./mvnw.cmd test
```

Documentação completa em [`apis/README.md`](apis/README.md) e
[`apis/arquitetura-apis.html`](apis/arquitetura-apis.html) (página interativa:
endpoints, entidades, credenciais, tecnologias e plano de ação por time).

### Credenciais

As chaves **não** estão no repositório. Copie o modelo e preencha:

```bash
cp apis/src/main/resources/application-local.example.properties \
   apis/src/main/resources/application-local.properties
```

> Em produção (perfil padrão), as chaves entram por variáveis de ambiente —
> veja a tabela de variáveis em `apis/README.md`.
