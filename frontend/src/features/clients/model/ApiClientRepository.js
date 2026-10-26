import { apiRequest } from "../../../shared/model/http.js"

// ClienteResponse and ClienteInput from ApiModels.java; local-only fields are never sent.
export const fromApiClient = (item) => ({
  id: String(item.id), name: item.nome, phone: item.telefone || "",
  instagram: item.instagram || "", channel: item.canalAquisicao || "",
  chat: item.linkConversa || "", folder: item.linkPasta || "",
  location: item.localizacao || "", body: item.localizacaoCorpo || "",
  size: item.tamanho || "", idea: item.ideia || "",
  music: item.musicaFavorita || "", food: item.comidaFavorita || "",
  style: item.estiloTatuagemFavorita || "", budget: item.faixaOrcamento || "",
  habits: item.habitos || "", availability: item.disponibilidadeHorarios || "",
  notes: item.observacoesGerais || "",
  status: item.status, created: item.dataCriacao, source: "api",
})

export function toApiClient(form) {
  if (!form.name?.trim() || !form.status?.trim()) throw new Error("Informe nome e status do cliente.")
  return {
    nome: form.name.trim(), telefone: form.phone?.trim() || null,
    instagram: form.instagram?.trim() || null, canalAquisicao: form.channel?.trim() || null,
    linkConversa: form.chat?.trim() || null, linkPasta: form.folder?.trim() || null,
    localizacao: form.location?.trim() || null,
    localizacaoCorpo: form.body?.trim() || null,
    tamanho: form.size?.trim() || null,
    ideia: form.idea?.trim() || null,
    musicaFavorita: form.music?.trim() || null,
    comidaFavorita: form.food?.trim() || null,
    estiloTatuagemFavorita: form.style?.trim() || null,
    faixaOrcamento: form.budget?.trim() || null,
    habitos: form.habits?.trim() || null,
    disponibilidadeHorarios: form.availability?.trim() || null,
    observacoesGerais: form.notes?.trim() || null,
    status: form.status.trim(),
  }
}

export default class ApiClientRepository {
  constructor(token) { this.token = token }
  list(pagina = 0, signal) {
    return apiRequest(`/api/v1/clientes?pagina=${pagina}&tamanho=20`, { token: this.token, signal })
  }
  get(id, signal) {
    return apiRequest(`/api/v1/clientes/${encodeURIComponent(id)}`, { token: this.token, signal })
  }
  save(form) {
    return apiRequest(`/api/v1/clientes${form.id ? `/${encodeURIComponent(form.id)}` : ""}`, {
      token: this.token, method: form.id ? "PUT" : "POST", body: toApiClient(form),
    })
  }
  remove(id) {
    return apiRequest(`/api/v1/clientes/${encodeURIComponent(id)}`, { token: this.token, method: "DELETE" })
  }
}
