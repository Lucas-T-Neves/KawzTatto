// Development requests use Vite's same-origin /api/v1 proxy. Production can
// use an API origin (without /api/v1) or a same-origin proxy with "/".
export const apiBaseUrl = import.meta.env?.DEV
  ? ""
  : (import.meta.env?.VITE_API_BASE_URL || "").replace(/\/+$/, "")
export const apiMode = Boolean(import.meta.env?.DEV || import.meta.env?.VITE_API_BASE_URL)

export async function apiRequest(path, { method = "GET", body, token, signal } = {}) {
  let response
  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      method,
      signal,
      headers: {
        Accept: "application/json, application/problem+json",
        ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
    })
  } catch (error) {
    if (error.name === "AbortError") throw error
    throw new Error("Não foi possível conectar à API. Verifique a URL e a conexão.")
  }
  const text = await response.text()
  let result = null
  try {
    result = text ? JSON.parse(text) : null
  } catch {
    if (response.ok) throw new Error("A API retornou uma resposta inválida (esperado JSON).")
  }
  if (!response.ok) {
    const message = response.status === 401
      ? "Acesso à API expirado ou não autorizado. Entre novamente."
      : result?.detail || result?.message || result?.title || `Erro HTTP ${response.status} ao acessar a API.`
    const error = new Error(message)
    error.status = response.status
    throw error
  }
  if (!text && response.status !== 204 && method !== "DELETE") {
    throw new Error("A API retornou uma resposta vazia inesperada.")
  }
  return result
}
