# Base local e MVVM

## Camadas

- **Model:** `Studio.js` documenta os registros e o contrato `StudioRepository` com JSDoc; `studioEntry.js` mantém validações e `studioDashboard.js` calcula status e avisos da operação local.
- **Repository:** `LocalStudioRepository.js` lê e salva no `kawz-data` já usado pelo aplicativo; mantém a normalização das sessões antigas e remove registros legados `demo-*` quando encontrados.
- **ViewModel:** `useStudioViewModel.js` controla o estado, a gravação, os indicadores e os erros de persistência.
- **View:** `App.jsx` consome o ViewModel, sem inicializar dados automáticos nem acessar o armazenamento desses registros diretamente.

A base local só contém dados criados pelo usuário ou carregados da API conforme cada fluxo.

## Testes

Execute `node --test src/features/studio/model/*.test.js src/features/auth/model/*.test.js`.

A integração de Clientes e autenticação da API está descrita em `frontend/README.md`.
