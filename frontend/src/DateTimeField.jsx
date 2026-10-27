import { cssClass } from "./styles/classes.js"
import { useState } from "react"
import useAnchoredPopover from "./shared/viewmodel/useAnchoredPopover.js"
import { Button, IconButton } from "./ui/index.jsx"
import { CalendarDays, Clock, ChevronLeft, ChevronRight } from "lucide-react"
const pad = (value) => String(value).padStart(2, "0")
const localDate = (date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
export default function DateTimeField({ label, type, value, onChange }) {
  const { id, root, open, toggle, close, popupProps } = useAnchoredPopover()
  const [month, setMonth] = useState(
    () =>
      new Date(
        `${type === "date" && value ? value : localDate(new Date())}T12:00:00`,
      ),
  )
  const hour = value.split(":")[0] || "00"
  const minute = value.split(":")[1] || "00"
  const first = new Date(month.getFullYear(), month.getMonth(), 1)
  const last = new Date(month.getFullYear(), month.getMonth() + 1, 0)
  const chooseDate = (date) => {
    onChange(date)
    close()
  }
  return (
    <div
      className={cssClass("date-time-field")}
      ref={root}
      onKeyDown={(event) => {
        if (event.key === "Escape" && open) {
          event.stopPropagation()
          close()
        }
      }}
    >
      <span id={`${id}-label`} className={cssClass("text-label-sm")}>
        {label}
      </span>
      <Button
        variant="neutral"
        className={cssClass("date-time-trigger")}
        aria-labelledby={`${id}-label ${id}-value`}
        aria-expanded={open}
        aria-controls={id}
        aria-haspopup="dialog"
        iconEnd={
          type === "date" ? (
            <CalendarDays className={cssClass("control-icon")} />
          ) : (
            <Clock className={cssClass("control-icon")} />
          )
        }
        onClick={() => {
          if (!open && type === "date" && value)
            setMonth(new Date(`${value}T12:00:00`))
          toggle()
        }}
      >
        <span id={`${id}-value`}>
          {value
            ? type === "date"
              ? new Date(`${value}T12:00:00`).toLocaleDateString("pt-BR")
              : value
            : type === "date"
              ? "Selecione a data"
              : "--:--"}
        </span>
      </Button>
      {
        <div
          {...popupProps}
          className={cssClass(`date-time-picker picker-${type} floating-glass`)}
          role="dialog"
          aria-label={`Selecionar ${label.toLowerCase()}`}
        >
          {type === "date" ? (
            <>
              <div className={cssClass("picker-heading")}>
                <IconButton
                  variant="neutral"
                  size="small"
                  aria-label="Mês anterior"
                  icon={<ChevronLeft className={cssClass("control-icon")} />}
                  onClick={() =>
                    setMonth(
                      new Date(month.getFullYear(), month.getMonth() - 1, 1),
                    )
                  }
                />
                <strong>
                  {month.toLocaleDateString("pt-BR", {
                    month: "long",
                    year: "numeric",
                  })}
                </strong>
                <IconButton
                  variant="neutral"
                  size="small"
                  aria-label="Próximo mês"
                  icon={<ChevronRight className={cssClass("control-icon")} />}
                  onClick={() =>
                    setMonth(
                      new Date(month.getFullYear(), month.getMonth() + 1, 1),
                    )
                  }
                />
              </div>
              <div className={cssClass("picker-days")}>
                {["D", "S", "T", "Q", "Q", "S", "S"].map((day, index) => (
                  <span className={cssClass("picker-weekday")} key={index}>
                    {day}
                  </span>
                ))}
                {Array.from({ length: first.getDay() }, (_, index) => (
                  <span key={`empty-${index}`} />
                ))}
                {Array.from({ length: last.getDate() }, (_, index) => {
                  const date = localDate(
                    new Date(month.getFullYear(), month.getMonth(), index + 1),
                  )
                  return (
                    <Button
                      size="small"
                      variant={value === date ? "primary" : "neutral"}
                      aria-pressed={value === date}
                      aria-label={new Date(
                        `${date}T12:00:00`,
                      ).toLocaleDateString("pt-BR")}
                      key={date}
                      onClick={() => chooseDate(date)}
                    >
                      {index + 1}
                    </Button>
                  )
                })}
              </div>
            </>
          ) : (
            <div className={cssClass("picker-time-columns")}>
              {["Hora", "Minuto"].map((part, column) => (
                <div className={cssClass("picker-time-part")} key={part}>
                  <span className={cssClass("text-label-sm")}>{part}</span>
                  <div
                    className={cssClass("picker-time-options")}
                    role="group"
                    aria-label={part}
                  >
                    {Array.from(
                      { length: column === 0 ? 24 : 60 },
                      (_, index) => {
                        const option = pad(index)
                        const selected =
                          option === (column === 0 ? hour : minute)
                        return (
                          <Button
                            size="small"
                            key={option}
                            variant={selected ? "primary" : "neutral"}
                            aria-pressed={selected}
                            onClick={() =>
                              onChange(
                                column === 0
                                  ? `${option}:${minute}`
                                  : `${hour}:${option}`,
                              )
                            }
                          >
                            {option}
                          </Button>
                        )
                      },
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
          <div className={cssClass("picker-actions")}>
            <Button
              size="small"
              variant="neutral"
              onClick={() => {
                onChange("")
                close()
              }}
            >
              Limpar
            </Button>
            {type === "date" ? (
              <Button
                size="small"
                onClick={() => chooseDate(localDate(new Date()))}
              >
                Hoje
              </Button>
            ) : (
              <Button
                size="small"
                onClick={() => {
                  if (!value) onChange("00:00")
                  close()
                }}
              >
                Concluir
              </Button>
            )}
          </div>
        </div>
      }
    </div>
  )
}
