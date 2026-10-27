export const formatMoney = (value) =>
  new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" }).format(
    value,
  )

export const formatDate = (value, options) =>
  value
    ? new Date(`${value}T12:00:00`).toLocaleDateString("pt-BR", options)
    : "—"

const amount = (value) => (Number.isFinite(Number(value)) ? Number(value) : 0)
const sum = (items) =>
  items.reduce((total, item) => total + amount(item.value), 0)

export function buildFinanceDashboard(
  transactions,
  clients,
  projects,
  period,
  today,
) {
  const month = transactions.filter((item) =>
    item.date?.startsWith(`${period}-`),
  )
  const paid = month.filter((item) => item.status === "Pago")
  const income = sum(paid.filter((item) => item.kind !== "Despesa"))
  const expense = sum(paid.filter((item) => item.kind === "Despesa"))
  const enrich = (entry) => {
    const project = projects.find((project) => project.id === entry.projectId)
    const client = clients.find(
      (client) => client.id === (entry.clientId || project?.clientId),
    )
    return {
      entry,
      client: client?.name || "Cliente não informado",
      project: project?.name || "Sem projeto",
      value: amount(entry.value),
      overdue: Boolean(
        entry.date && entry.date < today && entry.status === "Pendente",
      ),
    }
  }
  const pending = month
    .filter((item) => item.kind !== "Despesa" && item.status === "Pendente")
    .sort((a, b) => (a.date || "").localeCompare(b.date || ""))
    .map(enrich)
  const recent = [...paid]
    .sort(
      (a, b) =>
        (b.date || "").localeCompare(a.date || "") ||
        (b.created || "").localeCompare(a.created || ""),
    )
    .map(enrich)
  const [year, monthNumber] = period.split("-").map(Number)
  const trend = Array.from({ length: 6 }, (_, index) => {
    const date = new Date(year, monthNumber - 6 + index, 1, 12)
    const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`
    const records = transactions.filter(
      (item) => item.status === "Pago" && item.date?.startsWith(`${key}-`),
    )
    return {
      key,
      label: date
        .toLocaleDateString("pt-BR", { month: "short" })
        .replace(".", ""),
      income: sum(records.filter((item) => item.kind !== "Despesa")),
      expense: sum(records.filter((item) => item.kind === "Despesa")),
    }
  })
  return {
    income,
    expense,
    balance: income - expense,
    pendingTotal: pending.reduce((total, item) => total + item.value, 0),
    pending,
    recent,
    trend,
    monthLabel: new Date(year, monthNumber - 1, 1, 12).toLocaleDateString(
      "pt-BR",
      { month: "long", year: "numeric" },
    ),
  }
}
