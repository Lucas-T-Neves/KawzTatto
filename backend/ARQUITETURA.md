# Organização do backend

O pacote `com.kawz.tattoo` combina camadas com agrupamento por funcionalidade:

- `api/<funcionalidade>`: controladores, DTOs e conversões da API; `api/exception`, `api/web` e `api/shared` reúnem componentes compartilhados.
- `domain/<funcionalidade>`: entidades, serviços e repositórios do recurso. Exceções, segurança e a entidade base compartilhadas têm pacotes próprios.
- `infrastructure/config`, `infrastructure/security` e `infrastructure/integration`: configuração do Spring, implementações de segurança e clientes de serviços externos.

Os repositórios continuam junto das funcionalidades, pois são interfaces Spring Data JPA; não há uma implementação de persistência separada que justifique criar um pacote vazio. A projeção da consulta de contratos fica com seu repositório.

Esta reorganização preserva as dependências existentes: serviços ainda usam DTOs e mapeadores da API, entidades usam JPA e serviços chamam integrações diretamente. Portanto, os pacotes facilitam a navegação, mas não estabelecem isolamento de domínio ou arquitetura limpa. Uma separação dessas dependências exigiria uma refatoração adicional de contratos e responsabilidades.
