import { apiRequest } from "../../../shared/model/http.js"

export const fromApiFollowUp = (item) => ({
  id: String(item.id),
  clientId: item.clienteId ? String(item.clienteId) : "",
  projectId: "",
  sessionId: "",
  type: item.tipoContato || "Orçamento",
  date: item.dataPrevista || "",
  status: item.statusContato || "Pendente",
  notes: item.descricaoObservacao || "",
  name: item.descricaoObservacao || `${item.tipoContato || "Acompanhamento"}`,
  source: "api",
})

export function toApiFollowUp(form) {
  if (!form.clientId) throw new Error("Selecione o cliente do acompanhamento.")
  if (!form.date) throw new Error("Informe a data prevista.")
  return {
    clienteId: Number(form.clientId),
    tipoContato: (form.type || "Orçamento").trim(),
    statusContato: (form.status || "Pendente").trim(),
    dataPrevista: form.date,
    dataRealizacao: form.status === "Realizado" ? form.date : null,
    descricaoObservacao: form.notes?.trim() || null,
    proximaAcao: null,
  }
}

export default class ApiFollowUpRepository {
  constructor(token) {
    this.token = token
  }

  list(signal) {
    return apiRequest("/api/v1/follow-ups", { token: this.token, signal })
  }

  save(form) {
    const hasId = Boolean(form.id)
    return apiRequest(`/api/v1/follow-ups${hasId ? `/${encodeURIComponent(form.id)}` : ""}`, {
      token: this.token,
      method: hasId ? "PUT" : "POST",
      body: toApiFollowUp(form),
    })
  }

  remove(id) {
    return apiRequest(`/api/v1/follow-ups/${encodeURIComponent(id)}`, {
      token: this.token,
      method: "DELETE",
    })
  }
}
