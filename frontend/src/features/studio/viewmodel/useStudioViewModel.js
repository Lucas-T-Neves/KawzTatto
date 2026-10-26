import { useEffect, useMemo, useState } from "react"
import LocalStudioRepository from "../model/LocalStudioRepository.js"
import { studioDashboard } from "../model/studioDashboard.js"
const localRepository = new LocalStudioRepository({
  getItem: (key) => window.localStorage.getItem(key),
  setItem: (key, value) => window.localStorage.setItem(key, value),
})
export default function useStudioViewModel(repository = localRepository) {
  const [data, setData] = useState(() => repository.load())
  const dashboard = useMemo(() => studioDashboard(data), [data])
  const [persistenceError, setPersistenceError] = useState("")
  useEffect(() => {
    try {
      repository.save(data)
      setPersistenceError("")
    } catch {
      setPersistenceError(
        "Não foi possível salvar neste navegador. Verifique o espaço disponível.",
      )
    }
  }, [data, repository])
  const saveEntry = (editor, entry) => setData((previous) => ({
    ...previous,
    [editor]: entry.id && (previous[editor] || []).some((item) => item.id === entry.id)
      ? (previous[editor] || []).map((item) => item.id === entry.id ? entry : item)
      : [...(previous[editor] || []), entry],
  }))
  return { data, saveEntry, persistenceError, ...dashboard }
}
