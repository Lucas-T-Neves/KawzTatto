import { apiRequest } from "../../../shared/model/http.js"

const text = (value) => (value == null ? "" : String(value))

export const fromApiBudget = (item) => ({
  id: String(item.id),
  name: item.nome || "Orçamento",
  projectId: item.projetoId ? String(item.projetoId) : "",
  clientId: item.clienteId ? String(item.clienteId) : "",
  date: item.data || "",
  status: item.status || "",
  total: text(item.total),
  deposit: text(item.deposito),
  estimate: text(item.sessoesEstimadas),
  perSession: text(item.valorSessaoEstimado),
  method: item.formaPagamento || "",
  expires: item.validade || "",
  accepted: item.propostaAceita ? "Sim" : "Não",
  source: "api",
})

const number = (value, label) => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed < 0) throw new Error(`Informe ${label} válido.`)
  return parsed
}

const positiveInt = (value, label) => {
  const parsed = Number(value)
  if (!Number.isInteger(parsed) || parsed < 1) throw new Error(`Informe ${label} válido.`)
  return parsed
}

export function toApiBudget(form) {
  if (!form.projectId) throw new Error("Selecione o projeto do orçamento.")
  if (!form.clientId) throw new Error("Selecione o cliente do orçamento.")
  if (!form.status?.trim()) throw new Error("Informe o status do orçamento.")
  return {
    projetoId: Number(form.projectId),
    clienteId: Number(form.clientId),
    nome: form.name?.trim() || "Orçamento",
    data: form.date || null,
    status: form.status.trim(),
    total: number(form.total, "valor total"),
    deposito: number(form.deposit, "valor de sinal"),
    sessoesEstimadas: positiveInt(form.estimate, "sessões estimadas"),
    formaPagamento: form.method?.trim() || null,
    validade: form.expires || null,
    propostaAceita: form.accepted === "Sim",
  }
}

export default class ApiBudgetRepository {
  constructor(token) {
    this.token = token
  }

  list(pagina = 0, signal) {
    return apiRequest(`/api/v1/orcamentos?pagina=${pagina}&tamanho=20`, { token: this.token, signal })
  }

  get(id, signal) {
    return apiRequest(`/api/v1/orcamentos/${encodeURIComponent(id)}`, { token: this.token, signal })
  }

  save(form) {
    const hasId = Boolean(form.id)
    return apiRequest(`/api/v1/orcamentos${hasId ? `/${encodeURIComponent(form.id)}` : ""}`, {
      token: this.token,
      method: hasId ? "PUT" : "POST",
      body: toApiBudget(form),
    })
  }

  remove(id) {
    return apiRequest(`/api/v1/orcamentos/${encodeURIComponent(id)}`, { token: this.token, method: "DELETE" })
  }
}
