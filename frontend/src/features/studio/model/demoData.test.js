import { strict as assert } from "node:assert"
import { test } from "node:test"
import { createDemoData, initializeDemoData } from "./demoData.js"
import LocalStudioRepository from "./LocalStudioRepository.js"
const now = new Date(2026, 6, 15, 12)
test("fixtures cover all application collections with unique IDs", () => {
  const data = createDemoData(now)
  assert.deepEqual(
    Object.keys(data).sort(),
    [
      "Clientes",
      "Projetos",
      "Agenda",
      "Orçamentos",
      "Financeiro",
      "Acompanhamentos",
      "Contratos",
    ].sort(),
  )
  const entries = Object.values(data).flat()
  assert.equal(new Set(entries.map((entry) => entry.id)).size, entries.length)
  assert.ok(entries.every((entry) => entry.demo === "true"))
})
test("all client, project and session references resolve consistently", () => {
  const data = createDemoData(now)
  for (const entry of Object.values(data).flat()) {
    if (entry.clientId)
      assert.ok(
        data.Clientes.some((client) => client.id === entry.clientId),
        entry.id,
      )
    if (entry.projectId) {
      const project = data.Projetos.find(
        (project) => project.id === entry.projectId,
      )
      assert.ok(project, entry.id)
      assert.equal(project.clientId, entry.clientId, entry.id)
    }
    if (entry.sessionId) {
      const session = data.Agenda.find(
        (session) => session.id === entry.sessionId,
      )
      assert.ok(session, entry.id)
      assert.equal(session.projectId, entry.projectId, entry.id)
    }
  }
})
test("dates are relative and the overnight fixture does not overlap other active activities", () => {
  const data = createDemoData(now)
  assert.equal(
    data.Agenda.find((entry) => entry.id === "demo-tarefa")?.date,
    "2026-07-15",
  )
  const active = data.Agenda.filter((entry) => entry.status !== "Cancelada")
  const interval = (entry) => {
    const start = new Date(`${entry.date}T${entry.start}:00`)
    const end = new Date(`${entry.date}T${entry.end}:00`)
    if (entry.end < entry.start) end.setDate(end.getDate() + 1)
    return [start.getTime(), end.getTime()]
  }
  for (let a = 0; a < active.length; a++) {
    for (let b = a + 1; b < active.length; b++) {
      const [start, end] = interval(active[a])
      const [otherStart, otherEnd] = interval(active[b])
      assert.equal(
        start < otherEnd && otherStart < end,
        false,
        `${active[a].id} / ${active[b].id}`,
      )
    }
  }
})
test("initialization preserves existing records and is idempotent", () => {
  const personal = { id: "personal-client", name: "Existing client" }
  const editedDemo = { id: "demo-ana", name: "Edited fixture" }
  const existing = {
    Clientes: [personal, editedDemo],
    Custom: [{ id: "custom", name: "Keep" }],
  }
  const result = initializeDemoData(existing, now)
  assert.equal(result.Clientes[0], personal)
  assert.equal(result.Clientes[1], editedDemo)
  assert.equal(result.Custom, existing.Custom)
  assert.equal(existing.Clientes.length, 2)
  assert.equal(initializeDemoData(result, new Date(2027, 0, 1)), result)
})
test("deleted examples are not resurrected after initialization", () => {
  const data = initializeDemoData({}, now)
  data.Clientes = data.Clientes.filter((entry) => entry.id !== "demo-felipe")
  assert.ok(
    !initializeDemoData(data, now).Clientes.some(
      (entry) => entry.id === "demo-felipe",
    ),
  )
})
test("repository normalizes old sessions and persists one initialized dataset", () => {
  const memory = new Map([
    [
      "kawz-data",
      JSON.stringify({
        Agenda: [{ id: "old-session", name: "Legacy", status: "Rascunho" }],
        Clientes: [{ id: "own-client", name: "Existing" }],
      }),
    ],
  ])
  const repository = new LocalStudioRepository({
    getItem: (key) => memory.get(key) ?? null,
    setItem: (key, value) => {
      memory.set(key, value)
    },
  })
  const data = repository.load()
  assert.equal(data.Agenda[0].kind, "Sessão")
  assert.equal(data.Agenda[0].status, "A confirmar")
  assert.equal(data.Clientes[0].id, "own-client")
  repository.save(data)
  assert.deepEqual(repository.load(), data)
})
