import { apiRequest } from "../../../shared/model/http.js"

const kindToType = {
  Sessão: "SESSAO",
  "Sessão": "SESSAO",
  Tarefa: "TAREFA",
  Evento: "EVENTO",
  Projeto: "PROJETO",
}

const typeToKind = {
  SESSAO: "Sessão",
  TAREFA: "Tarefa",
  EVENTO: "Evento",
  PROJETO: "Projeto",
}

const timeValue = (value) =>
  typeof value === "string" && value.length >= 5 ? value.slice(0, 5) : value || ""

export const fromApiActivity = (item) => ({
  id: String(item.id),
  tatuadorId: item.tatuadorId ? String(item.tatuadorId) : "",
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

export function toApiActivity(form, tatuadorId) {
  if (!tatuadorId) throw new Error("Não foi possível identificar o tatuador autenticado.")
  if (!form.name?.trim()) throw new Error("Informe o título da atividade.")
  const type = kindToType[form.kind] || form.kind?.toUpperCase()
  if (!type) throw new Error("Informe o tipo da atividade.")
  const description = form.notes?.trim() || form.name.trim()
  return {
    tatuadorId: Number(tatuadorId),
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

export default class ApiAgendaRepository {
  constructor(token, tatuadorId) {
    this.token = token
    this.tatuadorId = tatuadorId
  }

  list(signal) {
    return apiRequest(`/api/v1/tatuadores/${encodeURIComponent(this.tatuadorId)}/atividades`, {
      token: this.token,
      signal,
    })
  }

  save(form) {
    const body = toApiActivity(form, this.tatuadorId)
    const hasId = Boolean(form.id)
    return apiRequest(`/api/v1/atividades${hasId ? `/${encodeURIComponent(form.id)}` : ""}`, {
      token: this.token,
      method: hasId ? "PUT" : "POST",
      body,
    })
  }

  remove(id) {
    return apiRequest(`/api/v1/atividades/${encodeURIComponent(id)}`, {
      token: this.token,
      method: "DELETE",
    })
  }
}
