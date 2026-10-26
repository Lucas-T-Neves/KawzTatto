import { useCallback, useEffect, useMemo, useState } from "react"
import ApiBudgetRepository, { fromApiBudget } from "../model/ApiBudgetRepository.js"

export default function useApiBudgetsViewModel(token, enabled, selected) {
  const repository = useMemo(() => (token ? new ApiBudgetRepository(token) : null), [token])
  const [pageIndex, setPageIndex] = useState(0)
  const [budgets, setBudgets] = useState([])
  const [budget, setBudget] = useState(null)
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
    setBudgets([])
    setBudget(null)
    setTotalPages(0)
    setListState("idle")
    setDetailState("idle")
    setLoadError("")
    setError("")
  }, [token])

  useEffect(() => {
    if (!enabled || !repository) {
      setBudgets([])
      setBudget(null)
      setTotalPages(0)
      setListState("idle")
      setDetailState("idle")
      setLoadError("")
      return
    }
    const controller = new AbortController()
    setLoadError("")
    if (selected) {
      setBudget(null)
      setDetailState("loading")
      repository.get(selected, controller.signal)
        .then((detail) => {
          setBudget(fromApiBudget(detail))
          setDetailState("success")
        })
        .catch((failure) => {
          if (failure.name === "AbortError") return
          setDetailState(failure.status === 404 ? "notFound" : "error")
          if (failure.status !== 404) setLoadError(failure.message)
        })
    } else {
      setBudgets([])
      setListState("loading")
      repository.list(pageIndex, controller.signal)
        .then((result) => {
          setBudgets(result.content.map(fromApiBudget))
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
      const saved = fromApiBudget(await repository.save(form))
      setBudget(saved)
      reload()
      return saved
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally {
      setSaving(false)
    }
  }

  const remove = async (id) => {
    setSaving(true)
    setError("")
    try {
      await repository.remove(id)
      setBudget(null)
      setPageIndex(0)
      reload()
    } catch (failure) {
      setError(failure.message)
      throw failure
    } finally {
      setSaving(false)
    }
  }

  return {
    budgets,
    budget,
    listState,
    detailState,
    loadError,
    saving,
    error,
    pageIndex,
    setPageIndex,
    totalPages,
    reload,
    save,
    remove,
  }
}
