import { test } from "node:test"
import { strict as assert } from "node:assert"
import ApiProjectRepository, { fromApiProject, toApiProject } from "./ApiProjectRepository.js"

test("projects use paginated API contract and include JWT", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return new Response(JSON.stringify({
      content: [{
        id: 9,
        clienteId: 5,
        nome: "Projeto Black",
        descricao: "Fechamento",
        statusProjeto: "EM_ANDAMENTO",
        valorTotal: 3500,
        valorSinal: 500,
        quantidadeSessoes: 4,
      }],
      totalPages: 2,
    }), { status: 200 })
  }
  try {
    const repo = new ApiProjectRepository("token-test", { id: 3 })
    const page = await repo.list(1)
    assert.equal(calls[0].url, "/api/v1/projetos?pagina=1&tamanho=20")
    assert.equal(calls[0].options.headers.Authorization, "Bearer token-test")
    assert.equal(page.totalPages, 2)
    assert.deepEqual(fromApiProject(page.content[0]), {
      id: "9",
      name: "Projeto Black",
      clientId: "5",
      status: "EM_ANDAMENTO",
      description: "Fechamento",
      folder: "",
      total: "3500",
      deposit: "500",
      estimate: "4",
      expires: "",
      pre: "",
      post: "",
      style: "",
      body: "",
      size: "",
      colors: "",
      complexity: "",
      idea: "",
      source: "api",
    })
  } finally {
    globalThis.fetch = original
  }
})

test("project writes send ProjetoInput contract", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return options.method === "DELETE"
      ? new Response(null, { status: 204 })
      : new Response(JSON.stringify({ id: 10, clienteId: 5, nome: "Projeto" }), { status: 200 })
  }
  try {
    const repo = new ApiProjectRepository("token-test", { id: 3 })
    await repo.save({
      id: "10",
      name: " Projeto ",
      description: " Arte fechada ",
      clientId: "5",
      status: "EM_ANDAMENTO",
      total: "3000",
      deposit: "500",
      estimate: "3",
      expires: "2030-01-10",
      folder: " https://drive/pasta ",
      pre: "Sim",
      post: "Sim",
      style: "Blackwork",
      body: "Braco",
      size: "20cm",
      colors: "Preto",
      complexity: "Alta",
      idea: "Flor",
    })
    assert.equal(calls[0].url, "/api/v1/projetos/10")
    assert.equal(calls[0].options.method, "PUT")
    assert.deepEqual(JSON.parse(calls[0].options.body), {
      tatuadorId: 3,
      clienteId: 5,
      nome: "Projeto",
      descricao: "Arte fechada",
      linkPasta: "https://drive/pasta",
      statusProjeto: "EM_ANDAMENTO",
      valorTotal: 3000,
      valorSinal: 500,
      quantidadeSessoes: 3,
      dataValidade: "2030-01-10",
      lembretePre: "Sim",
      lembretePost: "Sim",
      ideiaExpectativa: "Flor",
      estiloArte: "Blackwork",
      localizacaoCorpo: "Braco",
      tamanho: "20cm",
      dificuldade: null,
      cores: "Preto",
      complexidade: "Alta",
    })
    assert.equal(await repo.remove("10"), null)
    assert.equal(calls[1].options.method, "DELETE")
  } finally {
    globalThis.fetch = original
  }
})

test("ProjetoInput validation blocks invalid form", () => {
  assert.throws(() => toApiProject({ clientId: "1" }, { id: 3 }), /status/)
  assert.throws(() => toApiProject({ clientId: "1", status: "RASCUNHO", total: "a", deposit: "1", estimate: "1", description: "x" }, { id: 3 }), /valor total/)
  assert.throws(() => toApiProject({ clientId: "1", status: "RASCUNHO", total: "1", deposit: "1", estimate: "0", description: "x" }, { id: 3 }), /quantidade de sessões/)
})
