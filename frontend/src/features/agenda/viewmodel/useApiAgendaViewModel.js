import { useCallback, useEffect, useMemo, useState } from "react"
import ApiAgendaRepository from "../model/ApiAgendaRepository.js"

export default function useApiAgendaViewModel(profile, enabled) {
  const repository = useMemo(() => {
    if (!enabled || !profile?.token || !profile?.id) return null
    return new ApiAgendaRepository(profile.token, profile.id)
  }, [enabled, profile?.token, profile?.id])

  const [activities, setActivities] = useState([])
  const [listState, setListState] = useState("idle")
  const [loadError, setLoadError] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")
  const [version, refresh] = useState(0)
  const reload = useCallback(() => refresh((value) => value + 1), [])

  useEffect(() => {
    if (!repository) {
      setActivities([])
      setListState("idle")
      setLoadError("")
      return
    }
    const controller = new AbortController()
    setLoadError("")
    setListState("loading")
    repository
      .list(controller.signal)
      .then((items) => {
        setActivities(items)
        setListState("success")
      })
      .catch((failure) => {
        if (failure.name === "AbortError") return
        setListState("error")
        setLoadError(failure.message)
      })
    return () => controller.abort()
  }, [repository, version])

  const save = async (form) => {
    if (!repository) throw new Error("Agenda indisponível.")
    setSaving(true)
    setError("")
    try {
      const saved = await repository.save(form)
      setActivities((previous) => {
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
    if (!repository) throw new Error("Agenda indisponível.")
    setSaving(true)
    setError("")
    try {
      await repository.remove(id)
      setActivities((previous) => previous.filter((item) => item.id !== id))
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally {
      setSaving(false)
    }
  }

  return {
    activities,
    listState,
    loadError,
    saving,
    error,
    reload,
    save,
    remove,
  }
}
