import { apiRequest } from "../../../shared/model/http.js"

const title = (value = "") => {
  if (!value) return ""
  return value.charAt(0).toUpperCase() + value.slice(1).toLowerCase()
}

const money = (value) => (value == null ? "" : String(value))

export const fromApiFinance = (item) => ({
  id: String(item.id),
  name: item.categoria || "Movimentação",
  projectId: item.projetoId ? String(item.projetoId) : "",
  sessionId: "",
  kind: item.naturezaTransacao === "SAIDA" ? "Despesa" : "Receita",
  type: item.categoria || "Outro",
  value: money(item.valorMonetario),
  date: item.dataOcorrencia || "",
  method: item.formaPagamento || "",
  status: title(item.statusPagamento || ""),
  notes: "",
  source: "api",
})

const amount = (value) => {
  const parsed = Number(value)
  if (!Number.isFinite(parsed) || parsed < 0) throw new Error("Informe um valor válido.")
  return parsed
}

export function toApiFinance(form) {
  if (!form.projectId) throw new Error("Selecione o projeto da movimentação.")
  if (!form.date) throw new Error("Informe a data da movimentação.")
  if (!form.status?.trim()) throw new Error("Informe o status da movimentação.")
  const kind = form.kind === "Despesa" ? "SAIDA" : "ENTRADA"
  return {
    projetoId: Number(form.projectId),
    naturezaTransacao: kind,
    categoria: (form.type || form.name || "Outro").trim(),
    dataOcorrencia: form.date,
    valorMonetario: amount(form.value),
    formaPagamento: form.method?.trim() || null,
    statusPagamento: form.status.trim().toLowerCase(),
  }
}

export default class ApiFinanceRepository {
  constructor(token) {
    this.token = token
  }

  list(signal) {
    return apiRequest("/api/v1/movimentacoes?pagina=0&tamanho=200", { token: this.token, signal })
  }

  save(form) {
    const hasId = Boolean(form.id)
    return apiRequest(`/api/v1/movimentacoes${hasId ? `/${encodeURIComponent(form.id)}` : ""}`, {
      token: this.token,
      method: hasId ? "PUT" : "POST",
      body: toApiFinance(form),
    })
  }

  remove(id) {
    return apiRequest(`/api/v1/movimentacoes/${encodeURIComponent(id)}`, {
      token: this.token,
      method: "DELETE",
    })
  }
}
