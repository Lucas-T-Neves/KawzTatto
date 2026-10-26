import { useCallback, useEffect, useMemo, useState } from "react"
import ApiProjectRepository, { fromApiProject } from "../model/ApiProjectRepository.js"

export default function useApiProjectsViewModel(token, profile, enabled, selected) {
  const repository = useMemo(
    () => token && profile ? new ApiProjectRepository(token, profile) : null,
    [token, profile],
  )
  const [pageIndex, setPageIndex] = useState(0)
  const [projects, setProjects] = useState([])
  const [project, setProject] = useState(null)
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
    setProjects([])
    setProject(null)
    setTotalPages(0)
    setListState("idle")
    setDetailState("idle")
    setLoadError("")
    setError("")
  }, [token, profile])

  useEffect(() => {
    if (!enabled || !repository) {
      setProjects([])
      setProject(null)
      setTotalPages(0)
      setListState("idle")
      setDetailState("idle")
      setLoadError("")
      return
    }
    const controller = new AbortController()
    setLoadError("")
    if (selected) {
      setProject(null)
      setDetailState("loading")
      repository.get(selected, controller.signal)
        .then((detail) => {
          setProject(fromApiProject(detail))
          setDetailState("success")
        })
        .catch((failure) => {
          if (failure.name === "AbortError") return
          setDetailState(failure.status === 404 ? "notFound" : "error")
          if (failure.status !== 404) setLoadError(failure.message)
        })
    } else {
      setProjects([])
      setListState("loading")
      repository.list(pageIndex, controller.signal)
        .then((result) => {
          setProjects(result.content.map(fromApiProject))
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
      const saved = fromApiProject(await repository.save(form))
      setProject(saved)
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
      setProject(null)
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
    projects,
    project,
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
