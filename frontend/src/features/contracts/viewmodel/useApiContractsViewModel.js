import { useEffect, useMemo, useState } from "react"
import ApiContractRepository from "../model/ApiContractRepository.js"

export default function useApiContractsViewModel(token, projectId, clientId, enabled) {
  const repository = useMemo(() => (token ? new ApiContractRepository(token) : null), [token])
  const [entries, setEntries] = useState([])
  const [listState, setListState] = useState("idle")
  const [loadError, setLoadError] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    if (!enabled || !repository || !projectId) {
      setEntries([])
      setListState("idle")
      setLoadError("")
      return
    }
    const controller = new AbortController()
    setLoadError("")
    setListState("loading")
    repository.listByProject(projectId, clientId, controller.signal)
      .then((items) => {
        setEntries(items)
        setListState("success")
      })
      .catch((failure) => {
        if (failure.name === "AbortError") return
        setEntries([])
        setListState("error")
        setLoadError(failure.message)
      })
    return () => controller.abort()
  }, [enabled, repository, projectId, clientId])

  const save = async (form) => {
    if (!repository) throw new Error("Contratos indisponíveis.")
    setSaving(true)
    setError("")
    try {
      const saved = await repository.save(form)
      const normalized = {
        id: String(saved.id),
        name: `Contrato · ${form.projectId || "Projeto"}`,
        projectId: form.projectId,
        clientId: form.clientId,
        status: saved.statusContrato || "Rascunho",
        changes: saved.limiteAlteracoesArte || "",
        delay: saved.toleranciaAtraso || "",
        image: saved.usoImagem || "",
        notes: saved.pdfUrl ? `PDF: ${saved.pdfUrl}` : "",
        source: "api",
      }
      setEntries([normalized])
      return normalized
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally {
      setSaving(false)
    }
  }

  return {
    entries,
    listState,
    loadError,
    saving,
    error,
    save,
  }
}
