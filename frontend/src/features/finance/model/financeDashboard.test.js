import { strict as assert } from "node:assert"
import { test } from "node:test"
import { buildFinanceDashboard } from "./financeDashboard.js"

const clients = [{ id: "client", name: "Cliente de teste" }]
const projects = [
  { id: "project", name: "Projeto de teste", clientId: "client" },
]
const transactions = [
  {
    id: "paid",
    name: "Receita",
    projectId: "project",
    date: "2026-01-10",
    kind: "Receita",
    value: "1000",
    status: "Pago",
  },
  {
    id: "expense",
    name: "Material",
    projectId: "project",
    date: "2026-01-11",
    kind: "Despesa",
    value: "200",
    status: "Pago",
  },
  {
    id: "late",
    name: "Parcela",
    projectId: "project",
    date: "2026-01-05",
    kind: "Receita",
    value: "300",
    status: "Pendente",
  },
  {
    id: "pending",
    name: "Sinal",
    projectId: "project",
    date: "2026-01-25",
    kind: "Receita",
    value: "100",
    status: "Pendente",
  },
  {
    id: "cancelled",
    name: "Cancelado",
    projectId: "project",
    date: "2026-01-01",
    kind: "Receita",
    value: "500",
    status: "Cancelado",
  },
  {
    id: "previous",
    name: "Receita anterior",
    projectId: "project",
    date: "2025-12-30",
    kind: "Receita",
    value: "700",
    status: "Pago",
  },
]
const dashboard = () =>
  buildFinanceDashboard(
    transactions,
    clients,
    projects,
    "2026-01",
    "2026-01-15",
  )

test("monthly totals only include paid transactions", () => {
  const data = dashboard()
  assert.equal(data.income, 1000)
  assert.equal(data.expense, 200)
  assert.equal(data.balance, 800)
  assert.equal(data.pendingTotal, 400)
})

test("pending entries expose customer, project, due date and overdue state", () => {
  const data = dashboard()
  assert.equal(data.pending.length, 2)
  assert.equal(data.pending[0].client, "Cliente de teste")
  assert.equal(data.pending[0].project, "Projeto de teste")
  assert.equal(data.pending[0].entry.date, "2026-01-05")
  assert.equal(data.pending[0].overdue, true)
  assert.equal(data.pending[1].overdue, false)
})

test("recent movements exclude pending and cancelled records and sort newest first", () => {
  assert.deepEqual(
    dashboard().recent.map(({ entry }) => entry.id),
    ["expense", "paid"],
  )
})

test("six-month trend crosses the year boundary and includes empty months", () => {
  const data = dashboard()
  assert.equal(data.trend.length, 6)
  assert.equal(data.trend[0].key, "2025-08")
  assert.equal(data.trend[5].key, "2026-01")
  assert.equal(data.trend[0].income, 0)
  assert.equal(data.trend[4].income, 700)
  assert.equal(data.trend[5].expense, 200)
})

test("empty periods and missing relationships are safe without mutating input", () => {
  const before = JSON.stringify(transactions)
  const empty = buildFinanceDashboard([], [], [], "2026-01", "2026-01-15")
  assert.equal(empty.balance, 0)
  assert.deepEqual(empty.pending, [])
  assert.deepEqual(empty.recent, [])
  const missing = buildFinanceDashboard(
    transactions,
    [],
    [],
    "2026-01",
    "2026-01-15",
  )
  assert.equal(missing.pending[0].client, "Cliente não informado")
  assert.equal(JSON.stringify(transactions), before)
})
