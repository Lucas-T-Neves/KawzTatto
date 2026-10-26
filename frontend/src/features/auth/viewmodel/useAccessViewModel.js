import { useState } from "react"
import { apiAuthRepository } from "../model/ApiAuthRepository.js"

export default function useAccessViewModel(onEnter) {
  const [mode, setMode] = useState("login")
  const [name, setName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState("")
  const changeMode = () => { setMode(mode === "login" ? "register" : "login"); setError(""); setPassword("") }
  const submit = async () => {
    if (submitting) return
    setError("")
    const normalizedEmail = email.trim().toLowerCase()
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalizedEmail)) return setError("Informe um e-mail válido.")
    if (mode === "register" && !name.trim()) return setError("Informe seu nome.")
    if (!password) return setError("Informe sua senha.")
    if (mode === "register" && password.length < 8) return setError("A senha deve ter pelo menos 8 caracteres.")
    setSubmitting(true)
    try {
      onEnter(await apiAuthRepository.enter({ mode, name, email: normalizedEmail, password }))
      setPassword("")
    } catch (failure) {
      setError(failure.message || "Não foi possível concluir o acesso.")
    } finally {
      setSubmitting(false)
    }
  }
  return { mode, name, setName, email, setEmail, password, setPassword, submitting, error, changeMode, submit }
}
