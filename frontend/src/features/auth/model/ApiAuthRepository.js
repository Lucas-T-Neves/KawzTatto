import { apiRequest } from "../../../shared/model/http.js"

export const apiAuthRepository = {
  async enter({ mode, name, email, password }) {
    if (mode === "register") {
      await apiRequest("/api/v1/tatuadores", {
        method: "POST", body: { nome: name.trim(), email, senha: password },
      })
    }
    const response = await apiRequest("/api/v1/tatuadores/autenticacao", {
      method: "POST", body: { email, senha: password },
    })
    if (!response?.autenticado || !response.token) throw new Error("E-mail ou senha incorretos.")
    return { name: response.nome, email, token: response.token, id: response.tatuadorId }
  },
}
