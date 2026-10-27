import { useEffect, useId, useRef, useState } from "react"

export default function useAnchoredPopover({ matchTriggerWidth = false } = {}) {
  const id = useId()
  const root = useRef(null)
  const popup = useRef(null)
  const [open, setOpen] = useState(false)
  const [position, setPosition] = useState({ top: 0, left: 0 })
  const trigger = () => root.current?.querySelector("button")
  const close = () => {
    popup.current?.hidePopover()
    trigger()?.focus()
  }
  const toggle = () => {
    if (!popup.current || !trigger()) return
    if (popup.current.matches(":popover-open")) {
      close()
      return
    }
    popup.current.showPopover()
    const anchor = trigger().getBoundingClientRect()
    const bounds = popup.current.getBoundingClientRect()
    const gap = 8
    const below = anchor.bottom + gap
    const width = matchTriggerWidth
      ? Math.min(anchor.width, window.innerWidth - gap * 2)
      : bounds.width
    setPosition({
      left: Math.max(
        gap,
        Math.min(anchor.right - width, window.innerWidth - width - gap),
      ),
      top: Math.max(
        gap,
        below + bounds.height <= window.innerHeight - gap
          ? below
          : anchor.top - bounds.height - gap,
      ),
      ...(matchTriggerWidth ? { width } : {}),
    })
    requestAnimationFrame(() => popup.current?.querySelector("button")?.focus())
  }
  useEffect(() => {
    if (!open) return
    const dismiss = (event) => {
      if (event.target instanceof Node && popup.current?.contains(event.target))
        return
      popup.current?.hidePopover()
    }
    window.addEventListener("resize", dismiss)
    document.addEventListener("scroll", dismiss, true)
    return () => {
      window.removeEventListener("resize", dismiss)
      document.removeEventListener("scroll", dismiss, true)
    }
  }, [open])
  const popupProps = {
    id,
    ref: popup,
    popover: "auto",
    style: position,
    onToggle: (event) => setOpen(event.newState === "open"),
    onKeyDown: (event) => {
      event.stopPropagation()
      if (event.key === "Escape") {
        event.preventDefault()
        close()
      }
      if (event.key === "ArrowDown" || event.key === "ArrowUp") {
        event.preventDefault()
        const buttons = [
          ...(popup.current?.querySelectorAll("button:not(:disabled)") || []),
        ]
        const current = buttons.indexOf(document.activeElement)
        buttons[
          (current + (event.key === "ArrowDown" ? 1 : -1) + buttons.length) %
            buttons.length
        ]?.focus()
      }
    },
  }
  return { id, root, popup, open, toggle, close, popupProps }
}
