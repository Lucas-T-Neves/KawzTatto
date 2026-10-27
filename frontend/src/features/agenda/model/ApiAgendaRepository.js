import { apiRequest } from "../../../shared/model/http.js"

const kindToType = {
  Sessão: "SESSAO",
  Sessao: "SESSAO",
  Tarefa: "TAREFA",
  Evento: "EVENTO",
  Projeto: "PROJETO",
}

const typeToKind = {
  SESSAO: "Sessão",
  "SESSÃO": "Sessão",
  TAREFA: "Tarefa",
  EVENTO: "Evento",
  PROJETO: "Projeto",
}

const agendaId = (entryType, id) => `${entryType}:${id}`

const parseAgendaId = (value) => {
  if (!value) return { entryType: "", sourceId: "" }
  const text = String(value)
  const [entryType, sourceId] = text.split(":")
  if (entryType && sourceId) return { entryType, sourceId }
  return { entryType: "", sourceId: text }
}

const timeValue = (value) =>
  typeof value === "string" && value.length >= 5 ? value.slice(0, 5) : value || ""

const dateAndTime = (dateTime) => {
  if (!dateTime || typeof dateTime !== "string") return { date: "", start: "" }
  const [date = "", rawTime = ""] = dateTime.split("T")
  return { date, start: timeValue(rawTime) }
}

export const fromApiActivity = (item) => ({
  id: agendaId("atividade", item.id),
  sourceId: item.id ? String(item.id) : "",
  entryType: "atividade",
  tatuadorId: item.tatuadorId ? String(item.tatuadorId) : "",
  projectId: item.projetoId ? String(item.projetoId) : "",
  clientId: item.clienteId ? String(item.clienteId) : "",
  name: item.titulo || item.descricao || "Atividade",
  description: item.descricao || "",
  kind: typeToKind[item.tipo] || item.tipo || "Tarefa",
  type: item.tipo || "",
  date: item.data || "",
  start: timeValue(item.inicio),
  end: timeValue(item.termino),
  location: item.local || "",
  notes: item.observacoes || "",
  status: "",
  collection: "Agenda",
})

export const fromApiSession = (item) => {
  const { date, start } = dateAndTime(item.dataHorario)
  return {
    id: agendaId("sessao", item.id),
    sourceId: item.id ? String(item.id) : "",
    entryType: "sessao",
    projectId: item.projetoId ? String(item.projetoId) : "",
    clientId: "",
    name: "Sessão",
    description: item.observacoesSessao || "",
    kind: "Sessão",
    type: "SESSAO",
    date,
    start,
    end: "",
    location: item.localAtendimento || "",
    notes: item.observacoesSessao || "",
    status: item.statusSessao || "",
    collection: "Agenda",
  }
}

export function toApiActivity(form, tatuadorId) {
  if (!tatuadorId) throw new Error("Não foi possível identificar o tatuador autenticado.")
  if (!form.name?.trim()) throw new Error("Informe o título da atividade.")
  const type = kindToType[form.kind] || form.kind?.toUpperCase()
  if (!type) throw new Error("Informe o tipo da atividade.")
  const description = form.notes?.trim() || form.name.trim()
  return {
    tatuadorId: Number(tatuadorId),
    projetoId: form.projectId ? Number(form.projectId) : null,
    clienteId: form.clientId ? Number(form.clientId) : null,
    descricao: description,
    tipo: type,
    titulo: form.name.trim(),
    data: form.date || null,
    inicio: form.start || null,
    termino: form.end || null,
    local: form.location?.trim() || null,
    observacoes: form.notes?.trim() || null,
  }
}

export function toApiSession(form) {
  if (!form.projectId) throw new Error("Vincule a sessão a um projeto.")
  if (!form.date || !form.start) throw new Error("Informe a data e o início da sessão.")
  return {
    projetoId: Number(form.projectId),
    dataHorario: `${form.date}T${form.start}:00`,
    localAtendimento: form.location?.trim() || "Mauá",
    statusSessao: (form.status || "A confirmar").trim(),
    observacoesSessao: form.notes?.trim() || null,
  }
}

const resolveEntry = (value, formKind) => {
  if (typeof value === "object" && value) {
    const byId = parseAgendaId(value.id)
    return {
      entryType:
        value.entryType || byId.entryType || (value.kind === "Sessão" ? "sessao" : "atividade"),
      sourceId: value.sourceId || byId.sourceId,
    }
  }
  const byId = parseAgendaId(value)
  return {
    entryType: byId.entryType || (formKind === "Sessão" ? "sessao" : "atividade"),
    sourceId: byId.sourceId,
  }
}

export default class ApiAgendaRepository {
  constructor(token, tatuadorId) {
    this.token = token
    this.tatuadorId = tatuadorId
  }

  list(signal) {
    return Promise.all([
      apiRequest(`/api/v1/tatuadores/${encodeURIComponent(this.tatuadorId)}/atividades`, {
        token: this.token,
        signal,
      }),
      apiRequest("/api/v1/sessoes?pagina=0&tamanho=200", {
        token: this.token,
        signal,
      }),
    ]).then(([activities, sessionsPage]) => {
      const sessions = Array.isArray(sessionsPage?.content)
        ? sessionsPage.content
        : Array.isArray(sessionsPage)
          ? sessionsPage
          : []
      return [
        ...activities.map(fromApiActivity),
        ...sessions.map(fromApiSession),
      ]
    })
  }

  save(form) {
    const { entryType, sourceId } = resolveEntry(form, form.kind)
    const isSession = entryType === "sessao"
    const body = isSession
      ? toApiSession(form)
      : toApiActivity(form, this.tatuadorId)
    const hasId = Boolean(sourceId)
    const path = isSession
      ? `/api/v1/sessoes${hasId ? `/${encodeURIComponent(sourceId)}` : ""}`
      : `/api/v1/atividades${hasId ? `/${encodeURIComponent(sourceId)}` : ""}`
    return apiRequest(path, {
      token: this.token,
      method: hasId ? "PUT" : "POST",
      body,
    }).then((saved) => (isSession ? fromApiSession(saved) : fromApiActivity(saved)))
  }

  remove(entry) {
    const { entryType, sourceId } = resolveEntry(entry)
    const path =
      entryType === "sessao"
        ? `/api/v1/sessoes/${encodeURIComponent(sourceId)}`
        : `/api/v1/atividades/${encodeURIComponent(sourceId)}`
    return apiRequest(path, {
      token: this.token,
      method: "DELETE",
    })
  }
}
