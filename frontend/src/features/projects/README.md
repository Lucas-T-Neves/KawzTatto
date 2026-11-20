# Imagens de projetos — frontend MVVM

- **Model:** `model/ApiProjectRepository.js` integra cadastro/listagem de projetos com a API (`/api/v1/projetos`).
- **Model:** `model/ImageRepository.js` documenta `ProjectImage` e o contrato assíncrono `ImageRepository` com JSDoc.
- **ViewModel:** `viewmodel/useProjectImageViewModel.js` controla seleção, carregamento, remoção, erros e descarte de respostas antigas.
- **View:** `view/ProjectImageField.jsx` usa componentes React locais para selecionar um arquivo e exibir sua prévia.
- **Composição:** `imageRepository` aponta para `ApiImageRepository` e retorna erro explícito enquanto o endpoint de upload não existir.

O formulário entrega os campos `reference`, `referenceId` e `referenceName` ao armazenamento local já existente do aplicativo.
Nenhum backend, credencial ou endpoint de imagens foi adicionado.

`ProjectSelectField` usa a API nativa Popover (camada superior) para manter as opções fora do recorte e da rolagem do modal. O posicionamento é calculado a partir do controle e do espaço visível.
