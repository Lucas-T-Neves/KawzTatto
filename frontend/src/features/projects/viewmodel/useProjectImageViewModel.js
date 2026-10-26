import { useEffect, useRef, useState } from "react"
import { imageRepository } from "../model/ImageRepository.js"
export default function useProjectImageViewModel(
  onChange,
  repository = imageRepository,
) {
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState("")
  const request = useRef(0)
  useEffect(
    () => () => {
      request.current += 1
    },
    [],
  )
  const attach = async (file) => {
    if (!file) return
    const current = ++request.current
    setBusy(true)
    setError("")
    try {
      const image = await repository.upload(file)
      if (current === request.current) onChange(image)
    } catch (reason) {
      if (current === request.current)
        setError(
          reason instanceof Error ? reason.message : "Falha ao anexar imagem.",
        )
    } finally {
      if (current === request.current) setBusy(false)
    }
  }
  const remove = () => {
    request.current += 1
    setBusy(false)
    setError("")
    onChange(null)
  }
  return { attach, remove, busy, error }
}
