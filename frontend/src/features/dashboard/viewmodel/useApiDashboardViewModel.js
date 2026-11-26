import { useEffect, useState } from "react"
import { apiRequest } from "../../../shared/model/http.js"

export default function useApiDashboardViewModel(token, enabled = true) {
  const [data, setData] = useState({ alertas: [] })
  const [state, setState] = useState("idle")
  const [error, setError] = useState("")

  useEffect(() => {
    if (!enabled || !token) {
      setData({ alertas: [] })
      setState("idle")
      setError("")
      return
    }
    const controller = new AbortController()
    setState("loading")
    setError("")
    apiRequest("/api/v1/dashboard", { token, signal: controller.signal })
      .then((response) => {
        setData({ ...response, alertas: Array.isArray(response?.alertas) ? response.alertas : [] })
        setState("success")
      })
      .catch((failure) => {
        if (failure.name === "AbortError") return
        setState("error")
        setError(failure.message)
      })
    return () => controller.abort()
  }, [enabled, token])

  return { ...data, state, error }
}
