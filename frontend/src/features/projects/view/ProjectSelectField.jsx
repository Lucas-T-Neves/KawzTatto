import { cssClass } from "../../../styles/classes.js"
import { useEffect, useId, useRef, useState } from "react"
import { Button } from "../../../ui/index.jsx"
import { ChevronDown } from "lucide-react"
// The native top layer keeps options outside the modal's scroll/clipping context.
export default function ProjectSelectField({
  label,
  value,
  options,
  onChange,
}) {
  const id = useId()
  const popup = useRef(null)
  const root = useRef(null)
  const getTrigger = () => root.current?.querySelector("button")
  const [open, setOpen] = useState(false)
  const [geometry, setGeometry] = useState({
    left: 0,
    top: 0,
    width: 0,
    maxHeight: 0,
  })
  useEffect(() => {
    if (!open) return
    const close = (event) => {
      if (event.target instanceof Node && popup.current?.contains(event.target))
        return
      popup.current?.hidePopover()
    }
    window.addEventListener("resize", close)
    document.addEventListener("scroll", close, true)
    return () => {
      window.removeEventListener("resize", close)
      document.removeEventListener("scroll", close, true)
    }
  }, [open])
  const show = () => {
    const rect = getTrigger()?.getBoundingClientRect()
    if (!rect || !popup.current) return
    if (popup.current.matches(":popover-open")) {
      popup.current.hidePopover()
      return
    }
    const below = window.innerHeight - rect.bottom
    const above = below < Math.min(240, options.length * 44) && rect.top > below
    const height = Math.min(280, Math.max(0, (above ? rect.top : below) - 16))
    setGeometry({
      left: Math.max(
        8,
        Math.min(rect.left, window.innerWidth - rect.width - 8),
      ),
      top: above ? Math.max(8, rect.top - height - 8) : rect.bottom + 8,
      width: Math.min(rect.width, window.innerWidth - 16),
      maxHeight: height,
    })
    popup.current.showPopover()
    requestAnimationFrame(() => {
      const selected = popup.current?.querySelector('[aria-selected="true"]')
      ;(selected || popup.current?.querySelector('[role="option"]'))?.focus()
    })
  }
  return (
    <div className={cssClass("project-select-field")} ref={root}>
      <span id={`${id}-label`} className={cssClass("text-label-sm")}>
        {label}
      </span>
      <Button
        variant="neutral"
        className={cssClass("project-select-trigger")}
        role="combobox"
        aria-labelledby={`${id}-label ${id}-value`}
        aria-expanded={open}
        aria-controls={id}
        aria-haspopup="listbox"
        iconEnd={<ChevronDown className={cssClass("control-icon")} />}
        onClick={show}
        onKeyDown={(event) => {
          if (event.key === "ArrowDown" || event.key === "ArrowUp") {
            event.preventDefault()
            show()
          }
        }}
      >
        <span id={`${id}-value`}>
          {options.find((option) => option.value === value)?.label ||
            "Selecione"}
        </span>
      </Button>
      <div
        id={id}
        ref={popup}
        popover="auto"
        role="listbox"
        aria-labelledby={`${id}-label`}
        className={cssClass("project-select-popover floating-glass")}
        style={geometry}
        onToggle={(event) => setOpen(event.newState === "open")}
        onKeyDown={(event) => {
          event.stopPropagation()
          if (event.key === "Escape") {
            event.preventDefault()
            popup.current?.hidePopover()
            getTrigger()?.focus()
          }
          const items = Array.from(
            popup.current?.querySelectorAll('[role="option"]') || [],
          )
          const index = items.indexOf(document.activeElement)
          if (["ArrowDown", "ArrowUp", "Home", "End"].includes(event.key)) {
            event.preventDefault()
            const next =
              event.key === "Home"
                ? 0
                : event.key === "End"
                  ? items.length - 1
                  : (index +
                      (event.key === "ArrowDown" ? 1 : -1) +
                      items.length) %
                    items.length
            items[next]?.focus()
          }
        }}
      >
        {options.length ? (
          options.map((option) => (
            <Button
              key={option.value}
              role="option"
              variant="subtle"
              aria-selected={option.value === value}
              onClick={() => {
                onChange(option.value)
                popup.current?.hidePopover()
                getTrigger()?.focus()
              }}
            >
              {option.label}
            </Button>
          ))
        ) : (
          <span className={cssClass("text-label-sm")}>
            Nenhum registro disponível.
          </span>
        )}
      </div>
    </div>
  )
}
