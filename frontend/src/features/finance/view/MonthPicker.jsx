import { useState } from "react"
import { Button, IconButton } from "../../../ui/index.jsx"
import { CalendarDays, ChevronLeft, ChevronRight } from "lucide-react"
import useFinancePopover from "../viewmodel/useFinancePopover.js"
import styles from "./Finance.module.css"

export default function MonthPicker({ value, onChange }) {
  const { id, root, open, toggle, close, popupProps } = useFinancePopover()
  const [year, setYear] = useState(Number(value.slice(0, 4)))
  const choose = (month) => {
    onChange(month)
    close()
  }
  return (
    <div ref={root} className={styles.monthField}>
      <Button
        size="small"
        variant="neutral"
        className={styles.monthTrigger}
        aria-label={`Selecionar mês: ${value}`}
        aria-expanded={open}
        aria-controls={id}
        aria-haspopup="dialog"
        iconEnd={<CalendarDays className={styles.icon} />}
        onClick={() => {
          if (!open) setYear(Number(value.slice(0, 4)))
          toggle()
        }}
      >
        {new Date(`${value}-01T12:00:00`).toLocaleDateString("pt-BR", {
          month: "long",
          year: "numeric",
        })}
      </Button>
      <div
        {...popupProps}
        role="dialog"
        aria-label="Selecionar mês"
        className={styles.popover}
      >
        <div className={styles.pickerHeading}>
          <IconButton
            size="small"
            variant="neutral"
            aria-label="Ano anterior"
            icon={<ChevronLeft className={styles.icon} />}
            onClick={() => setYear(year - 1)}
          />
          <strong>{year}</strong>
          <IconButton
            size="small"
            variant="neutral"
            aria-label="Próximo ano"
            icon={<ChevronRight className={styles.icon} />}
            onClick={() => setYear(year + 1)}
          />
        </div>
        <div className={styles.monthGrid}>
          {Array.from({ length: 12 }, (_, index) => {
            const month = `${year}-${String(index + 1).padStart(2, "0")}`
            return (
              <Button
                size="small"
                key={month}
                variant={value === month ? "primary" : "neutral"}
                aria-pressed={value === month}
                onClick={() => choose(month)}
              >
                {new Date(year, index, 1)
                  .toLocaleDateString("pt-BR", { month: "short" })
                  .replace(".", "")}
              </Button>
            )
          })}
        </div>
        <Button
          size="small"
          variant="neutral"
          onClick={() =>
            choose(new Date().toLocaleDateString("en-CA").slice(0, 7))
          }
        >
          Mês atual
        </Button>
      </div>
    </div>
  )
}
