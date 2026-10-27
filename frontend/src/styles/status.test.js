import { strict as assert } from "node:assert"
import { test } from "node:test"
import { statusTone } from "./status.js"

test("positive outcomes use the positive semantic token", () => {
  for (const status of [
    "Cliente",
    "Pago",
    "Confirmada",
    "Finalizado",
    "Aprovado",
    "Realizada",
  ]) {
    assert.equal(statusTone(status), "status-positive", status)
  }
})

test("negative outcomes and pending decisions remain distinct", () => {
  for (const status of ["Cancelado", "Cancelada", "Recusado"]) {
    assert.equal(statusTone(status), "status-negative", status)
  }
  for (const status of [
    "Interessado",
    "Pendente",
    "A iniciar",
    "Aguardando assinatura",
  ]) {
    assert.equal(statusTone(status), "status-pending", status)
  }
})

test("unknown statuses are neutral and matching is case insensitive", () => {
  assert.equal(statusTone(" PAGO "), "status-positive")
  assert.equal(statusTone("Rascunho"), "status-neutral")
  assert.equal(statusTone(), "status-neutral")
})
