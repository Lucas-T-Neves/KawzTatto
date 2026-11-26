# KawzTattoo — frontend

## Execução

No diretório `frontend/`, execute `npm install` e `npm run dev`. `npm run build` gera a versão de produção. O Vite encaminha `/api/v1` para `http://localhost:8080` por padrão; configure `API_PROXY_TARGET` para outra origem. Em produção, defina `VITE_API_BASE_URL` ou use um proxy de mesma origem.

## Estado das integrações

O login, Clientes, Projetos, Agenda, Orçamentos, Anamnese, Dashboard e upload de imagens de projetos seguem repositories/ViewModels de API. O documento assinado da anamnese é enviado como multipart ao backend e armazenado no Cloudinary. Alertas de pré e pós-tatuagem são carregados do Dashboard.

Financeiro, acompanhamentos e conversa externa ainda possuem partes locais ou contratos de API não totalmente conectados ao fluxo visual. Atividades comuns permanecem locais por decisão de produto; sessões são sincronizadas com Nylas pelo backend. Contratos ainda aparecem como módulo legado no shell e devem ser removidos se a descontinuação for confirmada definitivamente.

## Organização

- **Views:** `src/App.jsx`, `src/ui/` e componentes especializados em `src/features/*/view/`.
- **ViewModels:** coordenam carregamento, erros e ações por feature.
- **Repositories:** encapsulam DTOs e transporte HTTP em `src/shared/model/http.js`.
- **Regras do studio:** `src/features/studio/model/` mantém somente configuração, validação e projeções usadas pelo runtime.

Testes: `npm test`, `npm run test:ui` e `npm run build`.
