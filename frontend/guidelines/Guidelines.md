# Interface local do KawzTattoo

Os componentes visuais são locais, em JavaScript/React e CSS Modules.
Não reinstale um kit de componentes para modificar a interface.

- Primitivos: `src/ui/index.jsx`.
- Estilos dos primitivos: `src/ui/Controls.module.css`.
- Cores, fontes, espaçamentos e medidas: variáveis em `src/index.css`.
- Estilos das telas: `src/App.module.css` e módulos CSS das funcionalidades.
- Ícones: `lucide-react`.

Preserve a arquitetura MVVM, os dados locais, o fluxo de login e os componentes acessíveis.
Seletores usam popovers nativos; os modais usam `dialog` nativo com gerenciamento de foco do navegador.
