# KawzTatto — frontend

## Execução

No diretório `frontend/`, execute `npm install` e `npm run dev`. `npm run build` gera a versão de produção. O Vite entrega `index.html` como fallback para as rotas em desenvolvimento e preview; em produção, configure o servidor estático para devolver `index.html` nas URLs de aplicação (por exemplo, `/clientes/42`).

No desenvolvimento, o Vite encaminha `/api/v1` para `http://localhost:8080` por padrão. O navegador acessa a API pela mesma origem do frontend e o acesso usa o login real, sem precisar de `VITE_API_BASE_URL`. Inicie o backend e reinicie o Vite após mudar a configuração do proxy. No build de produção sem configuração adicional, o acesso e os registros ainda permanecem em modo demonstração no `localStorage` do navegador; o perfil de teste está documentado em `src/features/studio/README.md`.

Para usar outra origem no desenvolvimento, configure `API_PROXY_TARGET` sem `/api/v1` antes de iniciar o Vite (por exemplo, `API_PROXY_TARGET=http://localhost:8080`). `VITE_API_BASE_URL` não altera o destino do proxy de desenvolvimento. Para ativar a API no **build de produção**, defina `VITE_API_BASE_URL` antes de compilar: use `/` se houver um proxy de mesma origem para `/api/v1`, ou a origem do Spring Boot sem `/api/v1` se o backend permitir CORS. O proxy do Vite não é incluído no build. Nenhuma configuração do backend é alterada aqui.

## Rotas e fontes de dados

`/` redireciona a `/inicio`. O menu leva a `/inicio`, `/agenda`, `/clientes`, `/projetos` e `/financeiro`. Fichas acessíveis por URL: `/clientes/:id` e `/projetos/:id`. URLs desconhecidas oferecem retorno ao início. Após atualizar uma URL interna, é preciso entrar novamente se estiver em modo API: o JWT é mantido apenas em memória e a URL solicitada é preservada.

No desenvolvimento com o proxy, ou na produção com `VITE_API_BASE_URL` configurada, **Clientes** mostra somente clientes vindos da API, com 20 registros por página. Fichas de clientes da base demonstrativa, quando acessadas a partir de projetos/avisos fictícios, usam IDs `demo-*` e são explicitamente demonstrativas. Não há sincronização nem migração automática entre as bases. Início, Agenda, Projetos, Financeiro, orçamentos, acompanhamentos, contratos e links de conversa continuam demonstrativos, com aviso visível. Os vínculos fictícios são consultados apenas na base local; os detalhes de clientes reais exibem somente campos persistidos pela API.

## Contratos integrados

- `POST /api/v1/tatuadores` cadastra o tatuador; `POST /api/v1/tatuadores/autenticacao` devolve `autenticado`, `nome`, `tatuadorId` e `token`. As demais requisições enviam `Authorization: Bearer <token>`. O cadastro local é usado apenas no build de produção sem a variável da API.
- `GET /api/v1/clientes?pagina=N&tamanho=20` (Spring `Page`, com `content` e `totalPages`), `GET /api/v1/clientes/{id}`, `POST /api/v1/clientes`, `PUT /api/v1/clientes/{id}`, `DELETE /api/v1/clientes/{id}`. O formulário real utiliza somente `nome`, `telefone`, `instagram`, `canalAquisicao`, `linkConversa`, `linkPasta` e `status` de `ClienteInput`. Nome e status são obrigatórios. Excluir exige confirmação; uma falha da API preserva a ficha aberta e mostra o erro.

As operações de `/api/v1/sessoes`, `/api/v1/projetos` e `/api/v1/movimentacoes` existem, mas os formulários atuais não atendem seus DTOs sem adaptar os fluxos: uma sessão não possui duração/fim no DTO, um projeto exige `tatuadorId` e campos próprios, e movimentações usam natureza/categoria/status próprios. Essas telas permanecem demonstrativas, sem enviar operações locais à API. Os campos extras da ficha local de cliente (ideia, localização, observações, preferências etc.) não integram `ClienteInput`; a API disponibiliza preferências separadamente em `/api/v1/clientes/{clienteId}/preferencias`, ainda não incorporadas à ficha integrada. A conversa externa também não está conectada. No desenvolvimento, o proxy evita o bloqueio CORS nas chamadas do navegador; um HTTP 401 sem token continua sendo a resposta esperada para endpoints protegidos. Em produção, é preciso configurar uma origem acessível ou um proxy equivalente. Os testes automatizados usam respostas HTTP simuladas, sem servidor Spring Boot.

## Organização

- **Views:** `src/App.jsx` preserva o layout e as telas; `src/ui/` mantém os controles e `src/features/*/view/` os componentes especializados.
- **ViewModels:** `src/features/auth/viewmodel/useAccessViewModel.js`, `src/features/clients/viewmodel/useApiClientsViewModel.js` e os ViewModels existentes de estúdio, projetos e financeiro coordenam estado, carregamento, erros e ações.
- **Models:** `src/features/clients/model/ApiClientRepository.js` contém as transformações de `ClienteInput`/`ClienteResponse`; `src/features/studio/model/studioEntry.js` guarda regras locais e `demoData.js` os exemplos.
- **Repositories / transporte:** `ApiClientRepository.js` e `ApiAuthRepository.js` usam `src/shared/model/http.js`; `LocalStudioRepository.js` usa armazenamento local, sem misturar dados reais e fictícios.

Testes: `node --test src/features/clients/model/ApiClientRepository.test.js src/features/studio/model/*.test.js src/features/auth/model/*.test.js src/features/clients/model/conversationGateway.test.js src/features/finance/model/*.test.js src/styles/*.test.js`; renderização: `npm run test:ui`.
