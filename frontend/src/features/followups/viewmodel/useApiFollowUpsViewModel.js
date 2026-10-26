import { useCallback, useEffect, useMemo, useState } from "react"
import ApiFollowUpRepository, { fromApiFollowUp } from "../model/ApiFollowUpRepository.js"

export default function useApiFollowUpsViewModel(token, enabled) {
  const repository = useMemo(() => (token ? new ApiFollowUpRepository(token) : null), [token])
  const [entries, setEntries] = useState([])
  const [listState, setListState] = useState("idle")
  const [loadError, setLoadError] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")
  const [version, refresh] = useState(0)
  const reload = useCallback(() => refresh((value) => value + 1), [])

  useEffect(() => {
    if (!enabled || !repository) {
      setEntries([])
      setListState("idle")
      setLoadError("")
      return
    }
    const controller = new AbortController()
    setLoadError("")
    setListState("loading")
    repository.list(controller.signal)
      .then((items) => {
        setEntries(items.map(fromApiFollowUp))
        setListState("success")
      })
      .catch((failure) => {
        if (failure.name === "AbortError") return
        setListState("error")
        setLoadError(failure.message)
      })
    return () => controller.abort()
  }, [enabled, repository, version])

  const save = async (form) => {
    if (!repository) throw new Error("Acompanhamentos indisponíveis.")
    setSaving(true)
    setError("")
    try {
      const saved = fromApiFollowUp(await repository.save(form))
      setEntries((previous) => {
        const exists = previous.some((item) => item.id === saved.id)
        return exists
          ? previous.map((item) => (item.id === saved.id ? saved : item))
          : [...previous, saved]
      })
      return saved
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally {
      setSaving(false)
    }
  }

  const remove = async (id) => {
    if (!repository) throw new Error("Acompanhamentos indisponíveis.")
    setSaving(true)
    setError("")
    try {
      await repository.remove(id)
      setEntries((previous) => previous.filter((item) => item.id !== id))
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
    reload,
    save,
    remove,
  }
}
