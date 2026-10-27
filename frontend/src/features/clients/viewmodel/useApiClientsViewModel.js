import { useCallback, useEffect, useMemo, useState } from "react"
import ApiClientRepository, { fromApiClient } from "../model/ApiClientRepository.js"

export default function useApiClientsViewModel(token, enabled, selected) {
  const repository = useMemo(() => token ? new ApiClientRepository(token) : null, [token])
  const [pageIndex, setPageIndex] = useState(0)
  const [clients, setClients] = useState([])
  const [client, setClient] = useState(null)
  const [totalPages, setTotalPages] = useState(0)
  const [listState, setListState] = useState("idle")
  const [detailState, setDetailState] = useState("idle")
  const [loadError, setLoadError] = useState("")
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState("")
  const [version, refresh] = useState(0)
  const reload = useCallback(() => refresh((value) => value + 1), [])

  useEffect(() => {
    setPageIndex(0)
    setClients([])
    setClient(null)
    setTotalPages(0)
    setListState("idle")
    setDetailState("idle")
    setLoadError("")
    setError("")
  }, [token])

  useEffect(() => {
    if (!enabled || !repository) {
      setClients([])
      setClient(null)
      setTotalPages(0)
      setListState("idle")
      setDetailState("idle")
      setLoadError("")
      return
    }
    const controller = new AbortController()
    setLoadError("")
    if (selected) {
      setClient(null)
      setDetailState("loading")
      repository.get(selected, controller.signal)
        .then((detail) => { setClient(fromApiClient(detail)); setDetailState("success") })
        .catch((failure) => {
          if (failure.name === "AbortError") return
          if (failure.status === 404) {
            setClient(null)
            setDetailState("success")
            return
          }
          setDetailState("error")
          setLoadError(failure.message)
        })
    } else {
      setClients([])
      setListState("loading")
      repository.list(pageIndex, controller.signal)
        .then((result) => {
          setClients(result.content.map(fromApiClient))
          setTotalPages(result.totalPages)
          setListState("success")
        })
        .catch((failure) => {
          if (failure.name === "AbortError") return
          setListState("error")
          setLoadError(failure.message)
        })
    }
    return () => controller.abort()
  }, [enabled, repository, pageIndex, selected, version])

  const save = async (form) => {
    setSaving(true)
    setError("")
    try {
      const saved = fromApiClient(await repository.save(form))
      setClient(saved)
      reload()
      return saved
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setSaving(false) }
  }
  const remove = async (id) => {
    setSaving(true)
    setError("")
    try {
      await repository.remove(id)
      setClient(null)
      setPageIndex(0)
      reload()
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally { setSaving(false) }
  }
  return { clients, client, listState, detailState, loadError, saving, error, pageIndex, setPageIndex, totalPages, reload, save, remove }
}
