import { cssClass, classSelector } from "./styles/classes.js"
import { Button, IconButton } from "./ui/index.jsx"
import { ChevronLeft, ChevronRight, Filter, Plus, X } from "lucide-react"
import { useEffect, useRef, useState } from "react"
export default function Calendar({
  view,
  setView,
  focus,
  setFocus,
  dates,
  entries,
  move,
  filters,
  create,
  inspect,
  unavailable = false,
}) {
  const [popover, setPopover] = useState("")
  const root = useRef(null)
  const timeline = useRef(null)
  const timeHeader = useRef(null)
  const monthGrid = useRef(null)
  const [monthCapacity, setMonthCapacity] = useState(2)
  useEffect(() => {
    if (view !== "Mês" || !monthGrid.current) return
    const grid = monthGrid.current
    const measure = () => {
      const cell = grid.querySelector(classSelector("calendar-day"))
      const number = grid.querySelector(classSelector("day-number"))
      const event = grid.querySelector(
        `${classSelector("month-event")}, ${classSelector("month-more")}`,
      )
      if (!cell || !number || !event) return
      const cellStyle = getComputedStyle(cell)
      const numberStyle = getComputedStyle(number)
      const eventStyle = getComputedStyle(event)
      const available =
        cell.clientHeight -
        parseFloat(cellStyle.paddingTop) -
        parseFloat(cellStyle.paddingBottom) -
        number.getBoundingClientRect().height -
        parseFloat(numberStyle.marginBottom)
      const line =
        event.getBoundingClientRect().height +
        parseFloat(eventStyle.marginBottom)
      setMonthCapacity(Math.max(1, Math.floor(available / line)))
    }
    measure()
    const observer = new ResizeObserver(measure)
    observer.observe(grid)
    return () => observer.disconnect()
  }, [view, dates.length, entries.length])
  useEffect(() => {
    if (view !== "Mês" && timeline.current) {
      const firstCell = timeline.current.querySelector(
        classSelector("hour-cell"),
      )
      timeline.current.scrollTop =
        (firstCell?.getBoundingClientRect().height || 0) * 8
    }
  }, [view])
  useEffect(() => {
    const close = (event) => {
      if (!root.current?.contains(event.target)) setPopover("")
    }
    const escape = (event) => {
      if (event.key === "Escape") setPopover("")
    }
    document.addEventListener("pointerdown", close)
    document.addEventListener("keydown", escape)
    return () => {
      document.removeEventListener("pointerdown", close)
      document.removeEventListener("keydown", escape)
    }
  }, [])
  const label =
    view === "Ano"
      ? focus.slice(0, 4)
      : new Date(`${focus}T12:00:00`).toLocaleDateString(
          "pt-BR",
          view === "Mês"
            ? { month: "long", year: "numeric" }
            : { day: "numeric", month: "long", year: "numeric" },
        )
  const eventButton = (entry) => (
    <Button
      key={entry.id}
      variant="neutral"
      className={cssClass(
        `calendar-event ${view === "Mês" ? "month-event" : ""}`,
      )}
      title={`${entry.start} · ${entry.name} · ${entry.kind}`}
      aria-label={`${entry.start} · ${entry.name} · ${entry.kind}`}
      onClick={() => inspect(entry)}
    >
      {view === "Mês" ? (
        `${entry.start} · ${entry.kind}`
      ) : (
        <span>
          <strong>
            {entry.start} · {entry.name}
          </strong>
          <small>
            {entry.kind} · {entry.status}
          </small>
        </span>
      )}
    </Button>
  )
  const renderedFilters =
    typeof filters === "function"
      ? filters({ close: () => setPopover("") })
      : filters
  const filtersAvailable = Boolean(renderedFilters)
  return (
    <section className={cssClass("panel agenda-panel")}>
      <div className={cssClass("calendar-toolbar")} ref={root}>
        <div className={cssClass("calendar-tools")}>
          <div className={cssClass("view-switch calendar-view-switch")}>
            {["Dia", "Semana", "Mês", "Ano"].map((value) => (
              <Button
                size="small"
                key={value}
                variant={view === value ? "primary" : "neutral"}
                aria-pressed={view === value}
                onClick={() => setView(value)}
              >
                {value}
              </Button>
            ))}
          </div>
          <div className={cssClass("popover-anchor")}>
              <IconButton
               className={cssClass("calendar-filter-button")}
               disabled={!filtersAvailable}
               variant="neutral"
               aria-label="Filtros da agenda"
               title="Filtros"
               aria-expanded={popover === "filters"}
               onClick={() => setPopover(popover === "filters" ? "" : "filters")}
              icon={
                <Filter
                  className={cssClass("control-icon calendar-filter-icon")}
                />
              }
            />
            {popover === "filters" && filtersAvailable && (
              <div
                className={cssClass("calendar-popover floating-glass")}
                role="group"
                aria-label="Filtros da agenda"
              >
                <div className={cssClass("section-head")}>
                  <strong>Filtrar agenda</strong>
                  <IconButton
                    size="small"
                    variant="subtle"
                    aria-label="Fechar filtros"
                    icon={<X className={cssClass("control-icon")} />}
                    onClick={() => setPopover("")}
                  />
                </div>
                 {renderedFilters}
              </div>
            )}
          </div>
          <div className={cssClass("popover-anchor")}>
              <IconButton
               className={cssClass("calendar-add-button")}
               disabled={unavailable}
              aria-label="Cadastrar atividade"
              title="Cadastrar atividade"
              aria-expanded={popover === "create"}
              onClick={() => setPopover(popover === "create" ? "" : "create")}
              icon={<Plus className={cssClass("control-icon")} />}
            />
            {popover === "create" && (
              <div
                className={cssClass(
                  "calendar-popover create-menu floating-glass",
                )}
                role="group"
                aria-label="Nova atividade"
              >
                {["Tarefa", "Evento", "Sessão", "Projeto"].map((kind) => (
                  <Button
                    variant="neutral"
                    key={kind}
                    onClick={() => {
                      setPopover("")
                      create(kind, focus)
                    }}
                  >
                    {kind}
                  </Button>
                ))}
              </div>
            )}
          </div>
        </div>
        <div className={cssClass("calendar-period")}>
          <IconButton
            className={cssClass("calendar-period-back")}
            variant="neutral"
            aria-label="Período anterior"
            title="Período anterior"
            icon={<ChevronLeft className={cssClass("control-icon")} />}
            onClick={() => move(-1)}
          />
          <IconButton
            className={cssClass("calendar-period-forward")}
            variant="neutral"
            aria-label="Próximo período"
            title="Próximo período"
            icon={<ChevronRight className={cssClass("control-icon")} />}
            onClick={() => move(1)}
          />
          <strong>{label}</strong>
          <Button
            size="small"
            variant="neutral"
            onClick={() => setFocus(new Date().toLocaleDateString("en-CA"))}
          >
            Hoje
          </Button>
        </div>
      </div>
      {view === "Ano" ? (
        <div className={cssClass("year-grid")}>
          {Array.from({ length: 12 }, (_, month) => {
            const date = new Date(
              `${focus.slice(0, 4)}-${String(month + 1).padStart(2, "0")}-01T12:00:00`,
            )
            const prefix = `${focus.slice(0, 4)}-${String(month + 1).padStart(2, "0")}`
            return (
              <Button
                className={cssClass("year-month")}
                variant="neutral"
                key={month}
                onClick={() => {
                  setFocus(`${prefix}-01`)
                  setView("Mês")
                }}
              >
                <span>
                  <strong>
                    {date.toLocaleDateString("pt-BR", { month: "long" })}
                  </strong>
                  <small>
                    {
                         unavailable ? "Agenda não integrada" : entries.filter((entry) => entry.date.startsWith(prefix)).length
                      }{" "}
                     {!unavailable && "atividades"}
                  </small>
                </span>
              </Button>
            )
          })}
        </div>
      ) : view === "Mês" ? (
        <div className={cssClass("month-scroll")}>
          <div
            className={cssClass("calendar month-grid")}
            ref={monthGrid}
            style={{ "--month-weeks": Math.ceil(dates.length / 7) }}
          >
            {["DOM", "SEG", "TER", "QUA", "QUI", "SEX", "SÁB"].map((day) => (
              <div className={cssClass("weekday")} key={day}>
                {day}
              </div>
            ))}
            {dates.map((date, index) => {
              const dayEntries = entries
                .filter((entry) => entry.date === date)
                .sort((a, b) => a.start.localeCompare(b.start))
              const visibleCount =
                dayEntries.length > monthCapacity
                  ? Math.max(0, monthCapacity - 1)
                  : dayEntries.length
              const remaining = dayEntries.length - visibleCount
              return (
                <div
                  className={cssClass(
                    `calendar-day ${
                      date === new Date().toLocaleDateString("en-CA")
                        ? "today"
                        : ""
                    }`,
                  )}
                  key={date || index}
                >
                  {date && (
                    <>
                      <Button
                        variant="subtle"
                        className={cssClass("day-number")}
                        aria-label={`Abrir dia ${date}`}
                        onClick={() => {
                          setFocus(date)
                          setView("Dia")
                        }}
                      >
                        {Number(date.slice(-2))}
                      </Button>
                      {dayEntries.slice(0, visibleCount).map(eventButton)}
                      {remaining > 0 && (
                        <Button
                          size="small"
                          variant="subtle"
                          className={cssClass("month-more")}
                          aria-label={`Ver ${remaining} atividades adicionais em ${date}`}
                          onClick={() => {
                            setFocus(date)
                            setView("Dia")
                          }}
                        >
                          +{remaining} atividades
                        </Button>
                      )}
                    </>
                  )}
                </div>
              )
            })}
          </div>
        </div>
      ) : (
        <div className={cssClass("timeline-frame")}>
          <div className={cssClass("time-header-scroll")} ref={timeHeader}>
            <div
              className={cssClass(
                `time-grid ${view === "Semana" ? "week-grid" : "single-grid"}`,
              )}
            >
              <div className={cssClass("time-heading hour-heading")}>
                Horário
              </div>
              {dates.map((date) => (
                <div
                  className={cssClass(
                    `time-heading ${date === focus ? "focused-day" : ""}`,
                  )}
                  key={date}
                >
                  <strong>{new Date(`${date}T12:00:00`).getDate()}</strong>
                  <span>
                    {new Date(`${date}T12:00:00`).toLocaleDateString("pt-BR", {
                      weekday: "short",
                    })}
                  </span>
                </div>
              ))}
            </div>
          </div>
          <div
            className={cssClass("time-grid-scroll")}
            ref={timeline}
            onScroll={(event) => {
              if (timeHeader.current)
                timeHeader.current.scrollLeft = event.currentTarget.scrollLeft
            }}
          >
            <div
              className={cssClass(
                `time-grid ${view === "Semana" ? "week-grid" : "single-grid"}`,
              )}
            >
              {Array.from({ length: 24 }, (_, hour) => {
                const time = `${String(hour).padStart(2, "0")}:00`
                return (
                  <div className={cssClass("time-row")} key={hour}>
                    <div className={cssClass("hour-label")}>{time}</div>
                    {dates.map((date) => (
                      <div className={cssClass("hour-cell")} key={date}>
                        <Button
                          variant="subtle"
                          className={cssClass("slot-button")}
                          disabled={unavailable}
                          aria-label={`Nova atividade em ${date} às ${time}`}
                          onClick={() => create?.("Evento", date, time)}
                        >
                          <span className={cssClass("slot-hint")}>+</span>
                        </Button>
                        {entries
                          .filter(
                            (entry) =>
                              entry.date === date &&
                              Number(entry.start?.split(":")[0]) === hour,
                          )
                          .map(eventButton)}
                      </div>
                    ))}
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
