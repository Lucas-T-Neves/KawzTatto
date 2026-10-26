# Financeiro — React, JavaScript e CSS Modules

- `model/financeDashboard.js`: projeção pura dos dados locais; totais, pendências, vencimentos, movimentações e série dos seis meses até o mês escolhido.
- `viewmodel/useFinanceDashboardViewModel.js`: entrega essa projeção à View.
- `viewmodel/useFinancePopover.js`: abertura, posicionamento, foco e fechamento dos popovers de mês e criação.
- `view/FinanceDashboard.jsx`: gráfico, pendências e tabela com consulta de detalhes dos registros existentes.
- `view/MonthPicker.jsx`: seleção compacta de mês/ano no padrão dos controles de data e horário.
- `view/FinanceActions.jsx`: um único botão de adicionar, com opções de pagamento e gasto.
- `view/Finance.module.css`: estilos locais baseados nos tokens globais do design system.

Receitas e despesas do gráfico e da tabela são registros com status `Pago`.
Pendências são receitas com status `Pendente` no mês selecionado; o campo `date` representa o vencimento enquanto o recebimento está pendente.
Datas anteriores ao dia atual são destacadas como atrasadas. Nenhum valor ilustrativo das imagens é usado nos cálculos.

Os dados continuam vindo do repositório local MVVM já existente, sem backend ou chamadas de rede.

Teste: `node --test src/features/finance/model/financeDashboard.test.js`.
