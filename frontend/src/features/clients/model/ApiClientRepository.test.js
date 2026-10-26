import { test } from "node:test"
import { strict as assert } from "node:assert"
import ApiClientRepository, { fromApiClient, toApiClient } from "./ApiClientRepository.js"
import { apiAuthRepository } from "../../auth/model/ApiAuthRepository.js"

test("clients use the exact paginated API contract and JWT header", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return new Response(JSON.stringify({ content: [{ id: 42, nome: "Ana", status: "CLIENTE", telefone: null }], totalPages: 3 }), { status: 200 })
  }
  try {
    const repo = new ApiClientRepository("token-test")
    const page = await repo.list(1)
    assert.equal(calls[0].url, "/api/v1/clientes?pagina=1&tamanho=20")
    assert.equal(calls[0].options.headers.Authorization, "Bearer token-test")
    assert.equal(page.totalPages, 3)
    assert.deepEqual(fromApiClient(page.content[0]), {
      id: "42", name: "Ana", phone: "", instagram: "", channel: "", chat: "", folder: "",
      location: "", body: "", size: "", idea: "", music: "", food: "", style: "", budget: "",
      habits: "", availability: "", notes: "", status: "CLIENTE", created: undefined, source: "api",
    })
  } finally { globalThis.fetch = original }
})

test("client writes send only ClienteInput fields and accept empty DELETE response", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return options.method === "DELETE" ? new Response(null, { status: 204 }) : new Response(JSON.stringify({ id: 42, nome: "Ana", status: "CLIENTE" }), { status: 200 })
  }
  try {
    const repo = new ApiClientRepository("token-test")
    await repo.save({ id: "42", name: " Ana ", phone: "", status: "CLIENTE", notes: "local", music: "local" })
    assert.equal(calls[0].url, "/api/v1/clientes/42")
    assert.equal(calls[0].options.method, "PUT")
    assert.deepEqual(JSON.parse(calls[0].options.body), {
      nome: "Ana", telefone: null, instagram: null, canalAquisicao: null, linkConversa: null, linkPasta: null,
      localizacao: null, localizacaoCorpo: null, tamanho: null, ideia: null, musicaFavorita: "local",
      comidaFavorita: null, estiloTatuagemFavorita: null, faixaOrcamento: null, habitos: null,
      disponibilidadeHorarios: null, observacoesGerais: "local", status: "CLIENTE",
    })
    assert.equal(await repo.remove("42"), null)
    assert.equal(calls[1].options.method, "DELETE")
    assert.throws(() => toApiClient({ name: "Ana" }), /status/)
  } finally { globalThis.fetch = original }
})

test("problem details and connection failures surface actionable messages", async () => {
  const original = globalThis.fetch
  try {
    globalThis.fetch = async () => new Response(JSON.stringify({ detail: "Cadastro inválido" }), { status: 400 })
    await assert.rejects(new ApiClientRepository("token").get(3), /Cadastro inválido/)
    globalThis.fetch = async () => { throw new TypeError("offline") }
    await assert.rejects(new ApiClientRepository("token").list(), /Não foi possível conectar/)
  } finally { globalThis.fetch = original }
})

test("an HTML fallback is reported as an invalid API response", async () => {
  const original = globalThis.fetch
  globalThis.fetch = async () => new Response("<html>Vite fallback</html>", {
    status: 200, headers: { "Content-Type": "text/html" },
  })
  try {
    await assert.rejects(new ApiClientRepository("token").list(), /resposta inválida/)
  } finally { globalThis.fetch = original }
})

test("authentication uses the backend login DTO and rejects unauthenticated response", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return new Response(JSON.stringify({ autenticado: false, token: null }), { status: 200 })
  }
  try {
    await assert.rejects(apiAuthRepository.enter({ mode: "login", email: "a@b.com", password: "wrong" }), /incorretos/)
    assert.equal(calls[0].url, "/api/v1/tatuadores/autenticacao")
    assert.deepEqual(JSON.parse(calls[0].options.body), { email: "a@b.com", senha: "wrong" })
  } finally { globalThis.fetch = original }
})
