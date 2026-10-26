import { test } from "node:test"
import { strict as assert } from "node:assert"
import { prepareStudioEntry } from "./studioEntry.js"

test("demo session rules still prevent collisions and unfinanced confirmations", () => {
  const session = { name: "Sessão", kind: "Sessão", projectId: "p", date: "2026-10-25", start: "10:00", end: "11:00", status: "Confirmada" }
  const existing = [{ id: "other", date: session.date, start: "10:30", end: "11:30", status: "A confirmar" }]
  const deps = { find: () => undefined, list: () => existing, hasDeposit: () => false }
  assert.throws(() => prepareStudioEntry("Agenda", session, deps), /Conflito de horários/)
  assert.throws(() => prepareStudioEntry("Agenda", session, { ...deps, list: () => [] }), /pagamento do sinal/)
})
