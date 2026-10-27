import { test } from "node:test"
import { strict as assert } from "node:assert"
import ApiAgendaRepository, {
  fromApiActivity,
  fromApiSession,
  toApiSession,
} from "./ApiAgendaRepository.js"

test("agenda combina atividades e sessões em uma única coleção", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    if (String(url).includes("/atividades")) {
      return new Response(JSON.stringify([
        {
          id: 1,
          projetoId: 10,
          clienteId: 20,
          titulo: "Planejamento",
          descricao: "Planejar arte",
          tipo: "TAREFA",
          data: "2026-10-26",
          inicio: "09:00:00",
          termino: "10:00:00",
        },
      ]), { status: 200 })
    }
    return new Response(JSON.stringify({
      content: [
        {
          id: 2,
          projetoId: 10,
          dataHorario: "2026-10-27T14:30:00",
          localAtendimento: "Mauá",
          statusSessao: "A confirmar",
          observacoesSessao: "Levar referência",
        },
      ],
      totalPages: 1,
    }), { status: 200 })
  }
  try {
    const repo = new ApiAgendaRepository("token", "7")
    const list = await repo.list()
    assert.equal(calls.length, 2)
    assert.ok(list.some((item) => item.entryType === "atividade"))
    assert.ok(list.some((item) => item.entryType === "sessao"))
    const activity = list.find((item) => item.entryType === "atividade")
    const session = list.find((item) => item.entryType === "sessao")
    assert.equal(activity.projectId, "10")
    assert.equal(activity.clientId, "20")
    assert.equal(session.kind, "Sessão")
    assert.equal(session.projectId, "10")
    assert.equal(session.start, "14:30")
  } finally {
    globalThis.fetch = original
  }
})

test("save de sessão usa endpoint de sessões e payload correto", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return new Response(JSON.stringify({
      id: 99,
      projetoId: 321,
      dataHorario: "2026-10-28T15:00:00",
      localAtendimento: "Vila Mariana",
      statusSessao: "Confirmada",
      observacoesSessao: "Sessão teste",
    }), { status: 200 })
  }
  try {
    const repo = new ApiAgendaRepository("token", "7")
    const saved = await repo.save({
      kind: "Sessão",
      projectId: "321",
      date: "2026-10-28",
      start: "15:00",
      location: "Vila Mariana",
      status: "Confirmada",
      notes: "Sessão teste",
    })
    assert.equal(calls[0].url, "/api/v1/sessoes")
    assert.equal(calls[0].options.method, "POST")
    assert.deepEqual(JSON.parse(calls[0].options.body), {
      projetoId: 321,
      dataHorario: "2026-10-28T15:00:00",
      localAtendimento: "Vila Mariana",
      statusSessao: "Confirmada",
      observacoesSessao: "Sessão teste",
    })
    assert.equal(saved.entryType, "sessao")
    assert.equal(saved.kind, "Sessão")
  } finally {
    globalThis.fetch = original
  }
})

test("save de atividade mantém endpoint de atividades", async () => {
  const original = globalThis.fetch
  const calls = []
  globalThis.fetch = async (url, options) => {
    calls.push({ url, options })
    return new Response(JSON.stringify({ id: 7, tipo: "TAREFA", titulo: "Contato" }), { status: 200 })
  }
  try {
    const repo = new ApiAgendaRepository("token", "8")
    await repo.save({
      kind: "Tarefa",
      name: "Contato",
      notes: "Ligar",
      date: "2026-10-30",
      start: "10:00",
      end: "10:30",
      projectId: "44",
      clientId: "55",
    })
    assert.equal(calls[0].url, "/api/v1/atividades")
    assert.deepEqual(JSON.parse(calls[0].options.body), {
      tatuadorId: 8,
      projetoId: 44,
      clienteId: 55,
      descricao: "Ligar",
      tipo: "TAREFA",
      titulo: "Contato",
      data: "2026-10-30",
      inicio: "10:00",
      termino: "10:30",
      local: null,
      observacoes: "Ligar",
    })
  } finally {
    globalThis.fetch = original
  }
})

test("helpers de agenda mapeiam sessão e atividade corretamente", () => {
  assert.equal(toApiSession({ projectId: "1", date: "2026-10-26", start: "09:00" }).projetoId, 1)
  assert.equal(fromApiSession({ id: 2, projetoId: 3, dataHorario: "2026-10-26T09:00:00" }).kind, "Sessão")
  assert.equal(fromApiActivity({ id: 3, tipo: "SESSÃO" }).kind, "Sessão")
})
