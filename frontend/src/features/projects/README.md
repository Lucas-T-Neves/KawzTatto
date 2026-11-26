# Imagens de projetos — frontend MVVM

- **Model:** `model/ApiImageRepository.js` valida o arquivo e envia multipart para `/api/v1/projetos/{projetoId}/imagens`; o backend armazena o asset no Cloudinary.
- **ViewModel:** `viewmodel/useProjectImageViewModel.js` controla seleção, carregamento, remoção visual, erros e descarte de respostas antigas.
- **View:** `view/ProjectImageField.jsx` usa os controles React locais para selecionar um arquivo e exibir sua prévia.

O upload só é habilitado depois que o projeto possui ID persistido. O frontend aceita JPG, PNG e WebP de até 10 MB. A remoção atual limpa a referência exibida no formulário; a exclusão definitiva do asset remoto deve ser acionada pelo endpoint de imagens quando esse fluxo for exposto na tela.
