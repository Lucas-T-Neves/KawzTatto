# Componentes locais

Esta pasta implementa os controles da aplicação em React/JavaScript com CSS Modules.
Não há biblioteca externa de componentes visuais.

- `Button` e `IconButton`: variantes, estados, foco visível e atributos nativos. `type` é `button` por padrão; formulários de login usam `type="submit"` explicitamente.
- `InputField` e `TextareaField`: rótulos associados, descrição acessível e contrato `onChange(value)`. Campos de arquivo não recebem valor controlado.
- `SelectField`: combobox com opções em popover nativo, navegação por teclado e fechamento sem listeners globais de digitação.
- `Modal`: `dialog` nativo, responsável pelo foco modal, tecla Escape, botão de fechar e clique no fundo.
- `SidebarNavigation`: navegação semântica com área de conteúdo e rodapé.

Os tokens de cores, fontes, espaçamentos e medidas pertencem a `src/index.css`.
`Controls.module.css` define os estilos dos controles; as telas continuam usando seus próprios módulos e classes.
As camadas Model, Repository e ViewModel, os dados locais e as credenciais de demonstração não dependem destes componentes.

## Testes

`npm run test:ui` verifica a renderização HTML dos controles, do login e do início autenticado.
O carregador de teste transforma JSX e substitui CSS Modules somente no processo de testes Node.
Ele não valida layout visual, foco ou interação num navegador; o build de produção continua usando o Vite.
