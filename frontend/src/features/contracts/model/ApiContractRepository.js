import { apiRequest } from "../../../shared/model/http.js"

export const fromApiContract = (item, projectId = "", clientId = "") => ({
  id: String(item.id || item.contratoId),
  name: `Contrato · ${projectId || item.nomeCliente || "Projeto"}`,
  projectId: projectId ? String(projectId) : "",
  clientId: clientId ? String(clientId) : "",
  status: item.statusContrato || "Rascunho",
  changes: item.limiteAlteracoesArte || "",
  delay: item.toleranciaAtraso || "",
  image: item.usoImagem || "",
  notes: item.pdfUrl ? `PDF: ${item.pdfUrl}` : "",
  source: "api",
})

export default class ApiContractRepository {
  constructor(token) {
    this.token = token
  }

  async listByProject(projectId, clientId, signal) {
    const item = await apiRequest(`/api/v1/projetos/${encodeURIComponent(projectId)}/contrato`, {
      token: this.token,
      signal,
    })
    return [fromApiContract(item, projectId, clientId)]
  }

  save(form) {
    if (!form.projectId) throw new Error("Selecione o projeto do contrato.")
    const body = {
      limiteAlteracoesArte: form.changes?.trim() || null,
      toleranciaAtraso: form.delay?.trim() || null,
      usoImagem: form.image?.trim() || null,
    }
    if (form.id) {
      return apiRequest(`/api/v1/contratos/${encodeURIComponent(form.id)}`, {
        token: this.token,
        method: "PUT",
        body,
      })
    }
    return apiRequest("/api/v1/contratos", {
      token: this.token,
      method: "POST",
      body: { projetoId: Number(form.projectId), ...body },
    })
  }
}
