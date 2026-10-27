import { useEffect } from "react"
export default function useScrollVisibility() {
  useEffect(() => {
    const timers = new Map()
    const show = (event) => {
      if (!(event.target instanceof HTMLElement)) return
      const element = event.target
      clearTimeout(timers.get(element))
      element.classList.add("is-scrolling")
      timers.set(
        element,
        setTimeout(() => {
          element.classList.remove("is-scrolling")
          timers.delete(element)
        }, 900),
      )
    }
    document.addEventListener("scroll", show, { capture: true, passive: true })
    return () => {
      document.removeEventListener("scroll", show, true)
      timers.forEach((timer, element) => {
        clearTimeout(timer)
        element.classList.remove("is-scrolling")
      })
    }
  }, [])
}
