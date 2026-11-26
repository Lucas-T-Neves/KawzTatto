# Regras locais de apresentação e MVVM

A aplicação usa repositories e ViewModels da API para Clientes, Projetos, Agenda, Anamnese, Orçamentos e demais recursos integrados. O módulo `studio` mantém somente regras de apresentação ainda consumidas pelo shell, como configuração de formulários, validações e projeções de status.

A antiga persistência local de registros (`LocalStudioRepository`, `useStudioViewModel` e `demoData`) foi removida do runtime para evitar duas fontes de verdade.
