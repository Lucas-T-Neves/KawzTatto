import { apiRequest } from "../../../shared/model/http.js"

const parseMoney = (value, label) => {
  const number = Number(value)
  if (!Number.isFinite(number) || number < 0) throw new Error(`Informe ${label} válido.`)
  return number
}

const parsePositiveInt = (value, label) => {
  const number = Number(value)
  if (!Number.isInteger(number) || number < 1) throw new Error(`Informe ${label} válido.`)
  return number
}

export const fromApiProject = (item) => ({
  id: String(item.id),
  name: item.nome || item.descricao || "Projeto",
  clientId: item.clienteId ? String(item.clienteId) : "",
  status: item.statusProjeto || "",
  description: item.descricao || "",
  folder: item.linkPasta || "",
  total: item.valorTotal != null ? String(item.valorTotal) : "",
  deposit: item.valorSinal != null ? String(item.valorSinal) : "",
  estimate: item.quantidadeSessoes != null ? String(item.quantidadeSessoes) : "",
  expires: item.dataValidade || "",
  pre: item.lembretePre || "",
  post: item.lembretePost || "",
  style: item.estiloArte || "",
  body: item.localizacaoCorpo || "",
  size: item.tamanho || "",
  colors: item.cores || "",
  complexity: item.complexidade || "",
  idea: item.ideiaExpectativa || "",
  source: "api",
})

export function toApiProject(form, profile) {
  if (!profile?.id) throw new Error("Não foi possível identificar o tatuador autenticado.")
  if (!form.clientId) throw new Error("Selecione o cliente do projeto.")
  if (!form.status?.trim()) throw new Error("Informe o status do projeto.")
  const descricao = form.description?.trim() || form.name?.trim()
  if (!descricao) throw new Error("Informe a descrição do projeto.")
  return {
    tatuadorId: Number(profile.id),
    clienteId: Number(form.clientId),
    nome: form.name?.trim() || descricao,
    descricao,
    linkPasta: form.folder?.trim() || null,
    statusProjeto: form.status.trim(),
    valorTotal: parseMoney(form.total, "valor total"),
    valorSinal: parseMoney(form.deposit, "valor de sinal"),
    quantidadeSessoes: parsePositiveInt(form.estimate, "quantidade de sessões"),
    dataValidade: form.expires || null,
    lembretePre: form.pre?.trim() || null,
    lembretePost: form.post?.trim() || null,
    ideiaExpectativa: form.idea?.trim() || null,
    estiloArte: form.style?.trim() || null,
    localizacaoCorpo: form.body?.trim() || null,
    tamanho: form.size?.trim() || null,
    dificuldade: form.difficulty?.trim() || null,
    cores: form.colors?.trim() || null,
    complexidade: form.complexity?.trim() || null,
  }
}

export default class ApiProjectRepository {
  constructor(token, profile) {
    this.token = token
    this.profile = profile
  }

  list(pagina = 0, signal) {
    return apiRequest(`/api/v1/projetos?pagina=${pagina}&tamanho=20`, { token: this.token, signal })
  }

  get(id, signal) {
    return apiRequest(`/api/v1/projetos/${encodeURIComponent(id)}`, { token: this.token, signal })
  }

  save(form) {
    return apiRequest(`/api/v1/projetos${form.id ? `/${encodeURIComponent(form.id)}` : ""}`, {
      token: this.token,
      method: form.id ? "PUT" : "POST",
      body: toApiProject(form, this.profile),
    })
  }

  remove(id) {
    return apiRequest(`/api/v1/projetos/${encodeURIComponent(id)}`, { token: this.token, method: "DELETE" })
  }
}
