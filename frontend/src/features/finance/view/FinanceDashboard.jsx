import { useId } from "react"
import { Button } from "../../../ui/index.jsx"
import useFinanceDashboardViewModel from "../viewmodel/useFinanceDashboardViewModel.js"
import { formatDate, formatMoney } from "../model/financeDashboard.js"
import styles from "./Finance.module.css"

function ComparisonChart({ trend }) {
  const id = useId()
  const max = Math.max(
    0,
    ...trend.flatMap((month) => [month.income, month.expense]),
  )
  const step = Math.max(100, Math.ceil(max / 4 / 100) * 100)
  const ceiling = step * 4
  const x = (index) => 70 + index * 102
  const y = (value) => 180 - (value / ceiling) * 160
  const points = (key) =>
    trend.map((month, index) => `${x(index)},${y(month[key])}`).join(" ")
  return (
    <div className={styles.chartWrap}>
      <svg
        className={styles.chart}
        viewBox="0 0 610 215"
        role="img"
        aria-labelledby={id}
      >
        <title id={id}>
          Receitas e despesas pagas nos seis meses até o mês selecionado
        </title>
        {Array.from({ length: 5 }, (_, index) => {
          const value = index * step
          return (
            <g key={value}>
              <line
                x1="60"
                x2="595"
                y1={y(value)}
                y2={y(value)}
                className={styles.gridLine}
              />
              <text
                x="53"
                y={y(value)}
                textAnchor="end"
                dominantBaseline="middle"
                className={styles.axisText}
              >
                {value >= 1000
                  ? `R$ ${new Intl.NumberFormat("pt-BR", { maximumFractionDigits: 1 }).format(value / 1000)} mil`
                  : `R$ ${value}`}
              </text>
            </g>
          )
        })}
        {["income", "expense"].map((key) => (
          <g
            key={key}
            className={
              key === "income" ? styles.incomeLine : styles.expenseLine
            }
          >
            <polyline points={points(key)} fill="none" />
            {trend.map((month, index) => (
              <circle key={month.key} cx={x(index)} cy={y(month[key])} r="3">
                <title>
                  {month.label}: {key === "income" ? "Receitas" : "Despesas"}{" "}
                  {formatMoney(month[key])}
                </title>
              </circle>
            ))}
          </g>
        ))}
        {trend.map((month, index) => (
          <text
            key={month.key}
            x={x(index)}
            y="204"
            textAnchor="middle"
            className={styles.axisText}
          >
            {month.label}
          </text>
        ))}
      </svg>
      <div className={styles.legend}>
        <span>
          <i className={styles.incomeDot} />
          Receitas
        </span>
        <span>
          <i className={styles.expenseDot} />
          Despesas
        </span>
      </div>
      {max === 0 && (
        <p className={styles.chartEmpty}>
          Sem movimentações pagas nesses meses.
        </p>
      )}
    </div>
  )
}

export default function FinanceDashboard({
  transactions,
  clients,
  projects,
  period,
  onInspect,
  unavailable = false,
}) {
  const data = useFinanceDashboardViewModel(
    transactions || [],
    clients || [],
    projects || [],
    period,
  )
  const titleId = useId()
  if (unavailable) return (
    <div className={styles.dashboard}>
      <div className={styles.summary}>
        {["Receitas", "Despesas", "Saldo", "Pendências do mês"].map((label) => (
          <section className={styles.metric} key={label}><span>{label}</span><strong>—</strong></section>
        ))}
      </div>
      <div className={styles.panels}>
        {[
          "Receitas × Despesas",
          "Pendências",
          "Movimentações recentes",
        ].map((label) => (
          <section className={styles.card} key={label}>
            <div className={styles.sectionHead}><div role="heading" aria-level={2}>{label}</div></div>
            <p role="status">Financeiro ainda não integrado à API. Dados indisponíveis.</p>
          </section>
        ))}
      </div>
    </div>
  )
  return (
    <div className={styles.dashboard}>
      <div className={styles.summary}>
        {[
          ["Receitas", data.income],
          ["Despesas", data.expense],
          ["Saldo", data.balance],
          ["Pendências do mês", data.pendingTotal],
        ].map(([label, value]) => (
          <section className={styles.metric} key={label}>
            <span>{label}</span>
            <strong>{formatMoney(value)}</strong>
          </section>
        ))}
      </div>
      <div className={styles.panels}>
        <section
          className={`${styles.card} ${styles.comparison}`}
          aria-labelledby={`${titleId}-chart`}
        >
          <div className={styles.sectionHead}>
            <div role="heading" aria-level={2} id={`${titleId}-chart`}>
              Receitas × Despesas
            </div>
          </div>
          <ComparisonChart trend={data.trend} />
        </section>
        <section
          className={`${styles.card} ${styles.pending}`}
          aria-labelledby={`${titleId}-pending`}
        >
          <div className={styles.sectionHead}>
            <div role="heading" aria-level={2} id={`${titleId}-pending`}>
              Pendências
            </div>
            <span>
              {data.pending.length}{" "}
              {data.pending.length === 1 ? "registro" : "registros"}
            </span>
          </div>
          <div className={styles.pendingList}>
            {data.pending.length ? (
              data.pending.map(({ entry, client, project, value, overdue }) => (
                <Button
                  key={entry.id}
                  variant="subtle"
                  className={styles.pendingButton}
                  aria-label={`Ver pagamento de ${client}, ${formatMoney(value)}`}
                  onClick={() => onInspect(entry)}
                >
                  <span className={styles.pendingContent}>
                    <strong>{client}</strong>
                    <strong className={styles.amount}>
                      {formatMoney(value)}
                    </strong>
                    <span className={styles.pendingDescription}>
                      {project} · {entry.type || "Pagamento"}
                    </span>
                    <span
                      className={
                        overdue ? styles.overdue : styles.pendingStatus
                      }
                    >
                      {overdue ? "Atrasado" : "Pendente"}
                    </span>
                    <span className={styles.due}>
                      Vencimento: {formatDate(entry.date)}
                    </span>
                  </span>
                </Button>
              ))
            ) : (
              <div className={styles.empty}>
                Nenhum pagamento pendente neste mês.
              </div>
            )}
          </div>
        </section>
        <section
          className={`${styles.card} ${styles.recent}`}
          aria-labelledby={`${titleId}-recent`}
        >
          <div className={styles.sectionHead}>
            <div role="heading" aria-level={2} id={`${titleId}-recent`}>
              Movimentações recentes
            </div>
            <span className={styles.monthLabel}>{data.monthLabel}</span>
          </div>
          <div className={styles.tableScroll}>
            <table className={styles.table}>
              <thead>
                <tr>
                  <th scope="col">Data</th>
                  <th scope="col">Descrição</th>
                  <th scope="col">Tipo</th>
                  <th scope="col">Valor</th>
                </tr>
              </thead>
              <tbody>
                {data.recent.map(({ entry, client, project, value }) => (
                  <tr key={entry.id}>
                    <td>
                      {formatDate(entry.date, {
                        day: "2-digit",
                        month: "2-digit",
                      })}
                    </td>
                    <td>
                      <Button
                        variant="subtle"
                        className={styles.descriptionButton}
                        onClick={() => onInspect(entry)}
                      >
                        <span className={styles.descriptionContent}>
                          <strong>{entry.name}</strong>
                          <small>
                            {entry.kind === "Despesa" ? project : client}
                          </small>
                        </span>
                      </Button>
                    </td>
                    <td>{entry.kind === "Despesa" ? "Despesa" : "Receita"}</td>
                    <td
                      className={
                        entry.kind === "Despesa"
                          ? styles.expenseValue
                          : styles.incomeValue
                      }
                    >
                      {entry.kind === "Despesa" ? "−" : "+"}{" "}
                      {formatMoney(value)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {!data.recent.length && (
              <div className={styles.empty}>
                Nenhuma movimentação paga neste mês.
              </div>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}
