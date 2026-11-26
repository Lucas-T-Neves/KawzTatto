import { useCallback, useEffect, useMemo, useState } from "react"
import ApiAnamneseRepository, { emptyAnamnese, fromApiAnamnese } from "../model/ApiAnamneseRepository.js"

export default function useApiAnamnesisViewModel(token, clienteId, enabled = true) {
  const repository = useMemo(() => token ? new ApiAnamneseRepository(token) : null, [token])
  const [anamnese, setAnamnese] = useState(null)
  const [listState, setListState] = useState("idle")
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState("")
  const [version, refresh] = useState(0)
  const reload = useCallback(() => refresh((value) => value + 1), [])

  useEffect(() => {
    if (!enabled || !repository || !clienteId) {
      setAnamnese(null)
      setListState("idle")
      setError("")
      return
    }
    const controller = new AbortController()
    setListState("loading")
    setError("")
    repository.listByClient(clienteId, controller.signal)
      .then((items) => {
        const list = Array.isArray(items) ? items : []
        setAnamnese(list.length ? fromApiAnamnese(list[0], clienteId) : emptyAnamnese(clienteId))
        setListState("success")
      })
      .catch((failure) => {
        if (failure.name === "AbortError") return
        setListState("error")
        setError(failure.message)
      })
    return () => controller.abort()
  }, [clienteId, enabled, repository, version])

  const create = async (form) => {
    setSaving(true)
    setError("")
    try {
      const saved = fromApiAnamnese(await repository.save(form, clienteId), clienteId)
      setAnamnese(saved)
      reload()
      return saved
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setSaving(false) }
  }

  const remove = async () => {
    if (!anamnese?.id) return
    setSaving(true)
    setError("")
    try {
      await repository.remove(anamnese.id)
      setAnamnese(emptyAnamnese(clienteId))
      reload()
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setSaving(false) }
  }

  const generatePdf = async () => {
    if (!anamnese?.id) return null
    setSaving(true)
    setError("")
    try {
      const generated = fromApiAnamnese(await repository.generatePdf(anamnese.id), clienteId)
      setAnamnese(generated)
      return generated
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setSaving(false) }
  }

  const uploadSignedDocument = async (file) => {
    if (!anamnese?.id || !file) return null
    setUploading(true)
    setError("")
    try {
      const uploaded = fromApiAnamnese(await repository.uploadSignedDocument(anamnese.id, file), clienteId)
      setAnamnese(uploaded)
      return uploaded
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setUploading(false) }
  }

  const removeSignedDocument = async () => {
    if (!anamnese?.id) return
    setUploading(true)
    setError("")
    try {
      await repository.removeSignedDocument(anamnese.id)
      setAnamnese((current) => ({ ...current, documentoAssinadoUrl: "", documentoAssinadoNome: "", documentoAssinadoMime: "", documentoAssinadoBytes: null, documentoAssinadoEm: "" }))
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setUploading(false) }
  }

  return { anamnese, listState, saving, uploading, error, reload, create, remove, generatePdf, uploadSignedDocument, removeSignedDocument }
}
