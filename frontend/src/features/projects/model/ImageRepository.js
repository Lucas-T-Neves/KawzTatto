import { apiRequest } from "../../../shared/model/http.js"

export class ApiImageRepository {
  constructor(token, projectId) { this.token = token; this.projectId = projectId }
  async upload(file) {
    if (!this.projectId) throw new Error("Salve o projeto antes de enviar uma imagem.")
    if (!["image/jpeg", "image/png", "image/webp"].includes(file.type)) {
      throw new Error("Selecione uma imagem JPG, PNG ou WebP.")
    }
    if (file.size > 10 * 1024 * 1024) {
      throw new Error("A imagem deve ter no máximo 10 MB.")
    }
    const body = new FormData()
    body.append("arquivo", file)
    const item = await apiRequest(`/api/v1/projetos/${encodeURIComponent(this.projectId)}/imagens`, {
      token: this.token,
      method: "POST",
      body,
    })
    return {
      id: String(item.id),
      name: item.nome || file.name,
      url: item.url,
      publicId: item.publicId,
    }
  }
}
