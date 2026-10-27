# Base fictícia e MVVM

## Camadas

- **Model:** `Studio.js` documenta os registros e o contrato `StudioRepository` com JSDoc; `studioEntry.js` mantém validações e `studioDashboard.js` calcula status e avisos demonstrativos.
- **Dados mockados:** `demoData.js` produz clientes, projetos, sessões, atividades, orçamentos, movimentações, acompanhamentos e contratos relacionados.
- **Repository:** `LocalStudioRepository.js` lê e salva no `kawz-data` já usado pelo aplicativo; mantém a normalização das sessões antigas.
- **ViewModel:** `useStudioViewModel.js` controla o estado, a gravação, os indicadores e os erros de persistência.
- **View:** `App.jsx` consome o ViewModel, sem inicializar a base nem acessar o armazenamento desses registros diretamente.

A base é adicionada automaticamente na primeira abertura da área interna após esta atualização.
Registros existentes são preservados. Os IDs `demo-*` são determinísticos; o marcador `__demo` é salvo junto aos dados para impedir duplicação em recargas e evitar recriar exemplos excluídos. Alterações nos exemplos continuam salvas localmente.

## Roteiro de teste

- **Ana Martins / Lua botânica:** sessões passadas e futuras, mais de um orçamento, receita paga, pagamento pendente, despesa e contrato.
- **Bruno Costa / Montanhas geométricas:** interessado, rascunho e orçamento próximo do vencimento.
- **Camila Rocha / Mariposa:** proposta aprovada, sinal pago e sessão a iniciar.
- **Diego Alves / Constelação:** projeto finalizado, pagamento integral e acompanhamento realizado.
- **Elisa Lima / Rosa:** projeto e sessão cancelados.
- **Felipe Nunes:** cliente sem projeto, para conferir estados vazios.
- **Agenda:** tarefa hoje, sessões em dias variados e evento que atravessa a meia-noite.

As datas são relativas ao primeiro carregamento da base, para popular as telas atuais e próximas. Depois disso, permanecem fixas no navegador.
Telefones usam apenas zeros; emails dos clientes terminam em `example.invalid`. Não há pessoas, pagamentos, assinaturas ou integrações reais.

## Acesso demonstrativo

- Email: `demo@kawztattoo.test`
- Senha: `KawzDemo2026!`

O perfil público de teste fica em `features/auth/model/localAuth.js`, separado dos dados do estúdio.
Ele funciona apenas no login local do navegador e não é uma conta de produção. Cadastros existentes não são substituídos.

## Testes

Execute `node --test src/features/studio/model/demoData.test.js src/features/auth/model/localAuth.test.js`.

Os dados mockados não fazem chamadas de rede. A integração independente de Clientes está descrita em `frontend/README.md`; os vínculos demonstrativos permanecem na base local.
