import { apiRequest } from "../../../shared/model/http.js"

const emptyHealth = {
  tratamentoMedico: false,
  tratamentoMedicoEspecifique: "",
  cirurgiaRecente: false,
  cirurgiaRecenteEspecifique: "",
  alergia: false,
  alergiaEspecifique: "",
  gestante: false,
  amamentando: false,
  marcapasso: false,
  disturbioCirculatorio: false,
  cancer: false,
  usoDrogas: false,
  alimentou24h: false,
  queloide: false,
  diabetes: false,
  diabetesTipo: "",
  diabetesControlada: null,
  convulsoes: false,
  convulsoesEspecifique: "",
  infectocontagiosas: false,
  infectocontagiosasEspecifique: "",
  vitiligo: false,
  hipertensao: false,
  anemia: false,
  cardiopatia: false,
  hemofilia: false,
  problemaPele: false,
  hepatite: false,
  dormiuUltimaNoite: false,
  outroProblema: "",
}

export const emptyAnamnese = (clienteId) => ({
  id: "",
  clienteId: String(clienteId || ""),
  logoUrl: "",
  nome: "",
  dataNascimento: "",
  ocupacao: "",
  telefone: "",
  whatsapp: false,
  endereco: "",
  rg: "",
  cpf: "",
  saude: { ...emptyHealth },
  dataAssinatura: "",
  assinaturaUrl: "",
  pdfUrl: "",
  transactionRef: "",
  documentoAssinadoUrl: "",
  documentoAssinadoNome: "",
  documentoAssinadoMime: "",
  documentoAssinadoBytes: null,
  documentoAssinadoEm: "",
  procedimento: { local: "", observacoes: "", profissional: "", data: "" },
})

export const fromApiAnamnese = (item, clienteId) => ({
  ...emptyAnamnese(item.clienteId || clienteId),
  ...item,
  id: item.id ? String(item.id) : "",
  clienteId: String(item.clienteId || clienteId || ""),
  logoUrl: item.logoUrl || item.logo_url || "",
  dataNascimento: item.dataNascimento || item.data_nascimento || "",
  dataAssinatura: item.dataAssinatura || item.data_assinatura || "",
  assinaturaUrl: item.assinaturaUrl || item.assinatura_url || "",
  pdfUrl: item.pdfUrl || item.pdf_url || "",
  transactionRef: item.transactionRef || item.transaction_ref || "",
  documentoAssinadoUrl: item.documentoAssinadoUrl || item.documento_assinado_url || "",
  documentoAssinadoNome: item.documentoAssinadoNome || item.documento_assinado_nome || "",
  documentoAssinadoMime: item.documentoAssinadoMime || item.documento_assinado_mime || "",
  documentoAssinadoBytes: item.documentoAssinadoBytes || item.documento_assinado_bytes || null,
  documentoAssinadoEm: item.documentoAssinadoEm || item.documento_assinado_em || "",
  saude: { ...emptyHealth, ...(item.saude || {}) },
  procedimento: { local: "", observacoes: "", profissional: "", data: "", ...(item.procedimento || {}) },
})

const dateValue = (value) => (value ? String(value).slice(0, 10) : null)

export function toApiAnamnese(form, clienteId) {
  if (!clienteId) throw new Error("Não foi possível identificar o cliente da anamnese.")
  if (!form.nome?.trim()) throw new Error("Informe o nome do cliente na anamnese.")
  return {
    clienteId: Number(clienteId),
    logoUrl: form.logoUrl?.trim() || null,
    nome: form.nome.trim(),
    dataNascimento: dateValue(form.dataNascimento),
    ocupacao: form.ocupacao?.trim() || null,
    telefone: form.telefone?.trim() || null,
    whatsapp: Boolean(form.whatsapp),
    endereco: form.endereco?.trim() || null,
    rg: form.rg?.trim() || null,
    cpf: form.cpf?.trim() || null,
    saude: form.saude || { ...emptyHealth },
    dataAssinatura: dateValue(form.dataAssinatura),
    assinaturaUrl: form.assinaturaUrl?.trim() || null,
    procedimento: {
      local: form.procedimento?.local?.trim() || null,
      observacoes: form.procedimento?.observacoes?.trim() || null,
      profissional: form.procedimento?.profissional?.trim() || null,
      data: dateValue(form.procedimento?.data),
    },
  }
}

export default class ApiAnamneseRepository {
  constructor(token) { this.token = token }

  listByClient(clienteId, signal) {
    return apiRequest(`/api/v1/clientes/${encodeURIComponent(clienteId)}/anamneses`, {
      token: this.token,
      signal,
    })
  }

  save(form, clienteId) {
    return apiRequest("/api/v1/anamneses", {
      token: this.token,
      method: "POST",
      body: toApiAnamnese(form, clienteId),
    })
  }

  remove(id) {
    return apiRequest(`/api/v1/anamneses/${encodeURIComponent(id)}`, {
      token: this.token,
      method: "DELETE",
    })
  }

  generatePdf(id) {
    return apiRequest(`/api/v1/anamneses/${encodeURIComponent(id)}/pdf`, {
      token: this.token,
      method: "POST",
    })
  }

  uploadSignedDocument(id, file) {
    const body = new FormData()
    body.append("arquivo", file)
    return apiRequest(`/api/v1/anamneses/${encodeURIComponent(id)}/documento-assinado`, {
      token: this.token,
      method: "POST",
      body,
    })
  }

  removeSignedDocument(id) {
    return apiRequest(`/api/v1/anamneses/${encodeURIComponent(id)}/documento-assinado`, {
      token: this.token,
      method: "DELETE",
    })
  }

}
