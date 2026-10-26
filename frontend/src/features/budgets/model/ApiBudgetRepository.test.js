import { test } from "node:test"
import { strict as assert } from "node:assert"
import ApiBudgetRepository, { fromApiBudget, toApiBudget } from "./ApiBudgetRepository.js"

test("budgets use paginated API contract and map fields", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return new Response(JSON.stringify({
      content: [{
        id: 3,
        projetoId: 8,
        clienteId: 5,
        nome: "Orçamento tattoo",
        data: "2030-01-10",
        status: "Enviado",
        total: 2000,
        deposito: 400,
        sessoesEstimadas: 3,
        valorSessaoEstimado: 666.67,
        formaPagamento: "Pix",
        validade: "2030-01-20",
        propostaAceita: false,
      }],
      totalPages: 2,
    }), { status: 200 })
  }
  try {
    const repo = new ApiBudgetRepository("token")
    const page = await repo.list(1)
    assert.equal(calls[0].url, "/api/v1/orcamentos?pagina=1&tamanho=20")
    assert.equal(page.totalPages, 2)
    assert.equal(fromApiBudget(page.content[0]).estimate, "3")
  } finally {
    globalThis.fetch = original
  }
})

test("budget writes send OrcamentoInput contract", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return options.method === "DELETE"
      ? new Response(null, { status: 204 })
      : new Response(JSON.stringify({ id: 1, projetoId: 8, clienteId: 5, status: "Rascunho" }), { status: 200 })
  }
  try {
    const repo = new ApiBudgetRepository("token")
    await repo.save({
      id: "1",
      projectId: "8",
      clientId: "5",
      name: " Orc ",
      date: "2030-01-10",
      status: "Rascunho",
      total: "2000",
      deposit: "400",
      estimate: "3",
      method: "Pix",
      expires: "2030-01-20",
      accepted: "Não",
    })
    assert.equal(calls[0].options.method, "PUT")
    assert.deepEqual(JSON.parse(calls[0].options.body), {
      projetoId: 8,
      clienteId: 5,
      nome: "Orc",
      data: "2030-01-10",
      status: "Rascunho",
      total: 2000,
      deposito: 400,
      sessoesEstimadas: 3,
      formaPagamento: "Pix",
      validade: "2030-01-20",
      propostaAceita: false,
    })
    await repo.remove("1")
    assert.equal(calls[1].options.method, "DELETE")
    assert.throws(() => toApiBudget({ status: "Rascunho" }), /projeto/)
  } finally {
    globalThis.fetch = original
  }
})
