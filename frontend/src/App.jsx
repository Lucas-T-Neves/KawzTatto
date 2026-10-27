import useAccessViewModel from "./features/auth/viewmodel/useAccessViewModel.js"
import { apiMode } from "./shared/model/http.js"
import useApiClientsViewModel from "./features/clients/viewmodel/useApiClientsViewModel.js"
import useApiProjectsViewModel from "./features/projects/viewmodel/useApiProjectsViewModel.js"
import useApiAgendaViewModel from "./features/agenda/viewmodel/useApiAgendaViewModel.js"
import useApiBudgetsViewModel from "./features/budgets/viewmodel/useApiBudgetsViewModel.js"
import useApiFinanceViewModel from "./features/finance/viewmodel/useApiFinanceViewModel.js"
import useApiFollowUpsViewModel from "./features/followups/viewmodel/useApiFollowUpsViewModel.js"
import useApiContractsViewModel from "./features/contracts/viewmodel/useApiContractsViewModel.js"
import { cssClass } from "./styles/classes.js"
import { statusTone } from "./styles/status.js"
import { useEffect, useState } from "react"
import { Link, NavLink, Navigate, Route, Routes, useLocation, useNavigate, useParams } from "react-router-dom"
import {
  Button,
  InputField,
  SelectField,
  TextareaField,
  Modal,
  SidebarNavigation,
  IconButton,
} from "./ui/index.jsx"
import Calendar from "./Calendar.jsx"
import ProjectImageField from "./features/projects/view/ProjectImageField.jsx"
import ProjectSelectField from "./features/projects/view/ProjectSelectField.jsx"
import {
  ArrowLeft,
  LogOut,
  Pencil,
  Plus,
  Search,
  Home,
  Folder,
  Trash2,
} from "lucide-react"
import DateTimeField from "./DateTimeField.jsx"
import useScrollVisibility from "./useScrollVisibility.js"
import FinanceDashboard from "./features/finance/view/FinanceDashboard.jsx"
import FinanceActions from "./features/finance/view/FinanceActions.jsx"
import MonthPicker from "./features/finance/view/MonthPicker.jsx"
import ConversationButton from "./features/clients/view/ConversationButton.jsx"
import {
  visibleFormFields,
} from "./features/studio/model/formConfiguration.js"
import { studioDashboard } from "./features/studio/model/studioDashboard.js"
const pages = ["Início", "Agenda", "Clientes", "Projetos", "Financeiro"]
const paths = {
  Início: "/inicio",
  Agenda: "/agenda",
  Clientes: "/clientes",
  Projetos: "/projetos",
  Financeiro: "/financeiro",
}
const pageIcons = {
  Início: <Home className={cssClass("nav-icon")} />,
  Agenda: (
    <svg
      className={cssClass("nav-icon")}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M16 3v4M8 3v4M3 11h18" />
    </svg>
  ),
  Clientes: (
    <svg
      className={cssClass("nav-icon")}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <circle cx="9" cy="7" r="4" />
      <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
    </svg>
  ),
  Projetos: <Folder className={cssClass("nav-icon")} />,
  Financeiro: (
    <svg
      className={cssClass("nav-icon")}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={2}
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M20 8V5a2 2 0 0 0-2-2H5a3 3 0 0 0 0 6h15a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H5a3 3 0 0 1-3-3V6" />
      <path d="M21 12h-5a2 2 0 0 0 0 4h5M17 14h.01" />
    </svg>
  ),
}
const pipeline = [
  "Rascunho",
  "A iniciar",
  "Em andamento",
  "Finalizado",
  "Cancelado",
]
const followTypes = [
  "Orçamento",
  "Pré-tatuagem",
  "Pós-tatuagem",
  "Retoque",
  "Nova sessão",
  "Relacionamento",
]
const today = () => new Date().toLocaleDateString("en-CA")
const money = (value) =>
  value.toLocaleString("pt-BR", { style: "currency", currency: "BRL" })
const number = (value) => Number(value || 0)
const dateLabel = (value) =>
  value ? new Date(`${value}T12:00:00`).toLocaleDateString("pt-BR") : "—"
const fields = {
  Orçamentos: [
    ["name", "Orçamento"],
    ["date", "Data", "date"],
    ["projectId", "Projeto", "Projetos"],
    ["clientId", "Cliente", "Clientes"],
    ["status", "Status", ["Rascunho", "Enviado", "Aprovado", "Recusado"]],
    ["total", "Valor total", "number"],
    ["deposit", "Sinal obrigatório", "number"],
    ["estimate", "Sessões estimadas", "number"],
    ["perSession", "Valor estimado por sessão", "number"],
    ["method", "Forma de pagamento", ["Pix", "Dinheiro", "Cartão", "Outro"]],
    ["expires", "Validade", "date"],
    ["accepted", "Proposta aceita", ["Não", "Sim"]],
  ],
  Clientes: [
    ["name", "Nome completo"],
    ["phone", "Telefone / WhatsApp", "tel"],
    ["instagram", "Instagram"],
    ["status", "Status", ["Interessado", "Cliente"]],
    [
      "channel",
      "Canal de aquisição",
      ["Indicação", "Instagram", "Anúncio", "Outro"],
    ],
    ["folder", "Link da pasta de referências", "url"],
    ["notes", "Observações gerais", "textarea"],
    ["music", "Música favorita"],
    ["food", "Comida favorita"],
    ["style", "Estilo preferido"],
    ["habits", "Hábitos"],
  ],
  Projetos: [
    ["name", "Nome"],
    ["clientId", "Cliente", "Clientes"],
    ["status", "Status", pipeline],
    ["folder", "Link da pasta do projeto", "url"],
    ["reference", "Imagem de referência", "image"],
    ["description", "Descrição da arte", "textarea"],
    ["style", "Estilo"],
    ["size", "Tamanho"],
    ["body", "Localização no corpo"],
    ["complexity", "Complexidade"],
    ["colors", "Cores"],
    ["total", "Valor total", "number"],
    ["deposit", "Sinal obrigatório", "number"],
    ["estimate", "Sessões estimadas", "number"],
    ["pre", "Lembrete pré-tatuagem", ["Sim", "Não"]],
    ["post", "Lembrete pós-tatuagem", ["Sim", "Não"]],
  ],
  Agenda: [
    ["name", "Título da atividade"],
    ["projectId", "Projeto", "Projetos"],
    ["clientId", "Cliente", "Clientes"],
    ["date", "Data", "date"],
    ["start", "Início", "time"],
    ["end", "Término", "time"],
    ["location", "Local", ["Mauá", "Vila Mariana"]],
    [
      "status",
      "Status",
      ["A confirmar", "Confirmada", "Realizada", "Remarcada", "Cancelada"],
    ],
    ["notes", "Tarefas prévias / observações", "textarea"],
  ],
  Acompanhamentos: [
    ["clientId", "Cliente", "Clientes"],
    ["projectId", "Projeto", "Projetos"],
    ["sessionId", "Sessão", "Agenda"],
    ["type", "Tipo", followTypes],
    ["date", "Data prevista", "date"],
    ["status", "Status", ["Pendente", "Realizado", "Adiado", "Cancelado"]],
    ["notes", "Observações / resultado do contato", "textarea"],
  ],
  Financeiro: [
    ["name", "Descrição"],
    ["projectId", "Projeto", "Projetos"],
    ["sessionId", "Sessão (opcional)", "Agenda"],
    ["kind", "Movimentação", ["Receita", "Despesa"]],
    [
      "type",
      "Tipo de receita",
      ["Sinal", "Pagamento de sessão", "Pagamento final", "Outro"],
    ],
    ["value", "Valor", "number"],
    ["date", "Data", "date"],
    ["method", "Forma de pagamento", ["Pix", "Dinheiro", "Cartão", "Outro"]],
    ["status", "Status", ["Pago", "Pendente", "Cancelado"]],
    ["notes", "Observações", "textarea"],
  ],
  Contratos: [
    ["name", "Identificação do documento"],
    ["projectId", "Projeto", "Projetos"],
    ["changes", "Alterações da arte sem custo", "number"],
    ["delay", "Tolerância de atraso"],
    ["deadline", "Prazo de desenvolvimento"],
    ["anamnesis", "Anamnese", "textarea"],
    ["exclusive", "Exclusividade", "textarea"],
    ["image", "Uso de imagem", "textarea"],
    ["notes", "Condições adicionais", "textarea"],
  ],
}
function Panel({ title, action, children, className = "" }) {
  return (
    <section className={cssClass(`panel ${className}`)}>
      <div className={cssClass("section-head")}>
        <div role="heading" aria-level={2} className={cssClass("text-heading")}>
          {title}
        </div>
        {action}
      </div>
      {children}
    </section>
  )
}
export function Workspace({ onExit, profile, page }) {
  const routerNavigate = useNavigate()
  const routeLocation = useLocation()
  const { id: selected = "" } = useParams()
  const apiClientPage = page === "Clientes"
  const apiProjectPage = page === "Projetos"
  const apiClients = useApiClientsViewModel(profile.token, true, apiClientPage ? selected : "")
  const apiProjects = useApiProjectsViewModel(profile.token, profile, true, apiProjectPage ? selected : "")
  const apiAgenda = useApiAgendaViewModel(profile, apiMode)
  const apiBudgets = useApiBudgetsViewModel(profile.token, true, "")
  const apiFinance = useApiFinanceViewModel(profile.token, true)
  const apiFollowUps = useApiFollowUpsViewModel(profile.token, true)
  const contractProject = apiProjectPage ? selected : ""
  const contractClient = apiProjectPage ? apiProjects.project?.clientId : ""
  const apiContracts = useApiContractsViewModel(profile.token, contractProject, contractClient, apiProjectPage)
  const apiCollectionPage = apiClientPage || apiProjectPage
  const activeApiViewModel = apiClientPage
    ? apiClients
    : apiProjects
  const [collapsed, setCollapsed] = useState(false)
  const [tab, setTab] = useState("Dados")
  const [query, setQuery] = useState("")
  const [filter, setFilter] = useState("Todos")
  const [period, setPeriod] = useState(today().slice(0, 7))
  const [focus, setFocus] = useState(today())
  const [view, setView] = useState("Mês")
  const [location, setLocation] = useState("Todos")
  const [activityType, setActivityType] = useState("Todos")
  const [projectStatus, setProjectStatus] = useState("Todos")
  const [editor, setEditor] = useState("")
  const [form, setForm] = useState({ id: "", name: "" })
  const [formSection, setFormSection] = useState("Dados gerais")
  const [error, setError] = useState("")
  const [notice, setNotice] = useState(routeLocation.state?.notice || "")
  const [detail, setDetail] = useState(null)
  useEffect(() => {
    if (!notice) return
    const timer = window.setTimeout(() => setNotice(""), 5000)
    return () => window.clearTimeout(timer)
  }, [notice])
  const list = (key) => {
    if (key === "Clientes") return apiClients.clients
    if (key === "Projetos" && apiProjectPage) return apiProjects.projects
    if (key === "Projetos") return apiProjects.projects
    if (key === "Agenda") return apiAgenda.activities
    if (key === "Orçamentos") return apiBudgets.budgets
    if (key === "Financeiro") return apiFinance.entries
    if (key === "Acompanhamentos") return apiFollowUps.entries
    if (key === "Contratos") return apiContracts.entries
    return []
  }
  const find = (key, id) => list(key).find((item) => item.id === id)
  const navigate = (next, id = "") => {
    routerNavigate(`${paths[next]}${id ? `/${encodeURIComponent(id)}` : ""}`)
    setQuery("")
    setFilter("Todos")
    setTab("Dados")
    setDetail(null)
  }
  const open = (key, entry, defaults = {}) => {
    if (key === "Agenda" && !apiMode) {
      setNotice("Configure VITE_API_BASE_URL para cadastrar na agenda.")
      return
    }
    if (!["Clientes", "Projetos", "Agenda", "Orçamentos", "Financeiro", "Acompanhamentos", "Contratos"].includes(key)) return
    setEditor(key)
    setFormSection(key === "Clientes" ? "Dados pessoais" : "Dados gerais")
    setError("")
    setDetail(null)
    setForm(
      entry || {
        id: "",
        name: "",
        date: today(),
        status:
          key === "Clientes" && apiClientPage ? "" : key === "Financeiro"
            ? "Pago"
            : key === "Projetos" || key === "Orçamentos"
              ? "Rascunho"
              : key === "Acompanhamentos"
                ? "Pendente"
                : "A confirmar",
        kind: key === "Financeiro" ? "Receita" : "Sessão",
        type:
          key === "Acompanhamentos"
            ? "Orçamento"
            : key === "Financeiro"
              ? "Sinal"
              : "Tatuagem",
        location: "Mauá",
        channel: "",
        method: "Pix",
        pre: "Sim",
        post: "Sim",
        accepted: "Não",
        changes: "5",
        ...defaults,
      },
    )
  }
  const clientStatus = (id) =>
    (apiClients.client?.id === id ? apiClients.client.status : find("Clientes", id)?.status) || ""
  const status = (item) => item.status || ""
  const sessionStatus = status
  const save = async () => {
    if (!["Clientes", "Projetos", "Agenda", "Orçamentos", "Financeiro", "Acompanhamentos", "Contratos"].includes(editor)) return
    setError("")
    try {
      if (editor === "Clientes") {
        const saved = await apiClients.save(form)
        setEditor("")
        setNotice("Cliente salvo na API.")
        routerNavigate(`${paths.Clientes}/${saved.id}`, { state: { notice: "Cliente salvo na API." } })
      } else if (editor === "Projetos") {
        const saved = await apiProjects.save(form)
        setEditor("")
        setNotice("Projeto salvo na API.")
        routerNavigate(`${paths.Projetos}/${saved.id}`, { state: { notice: "Projeto salvo na API." } })
      } else if (editor === "Agenda") {
        const saved = await apiAgenda.save(form)
        setEditor("")
        if (saved.date) setFocus(saved.date)
        setNotice(form.id ? "Atividade atualizada na agenda." : "Atividade cadastrada na agenda.")
        if (page !== "Agenda") navigate("Agenda")
      } else if (editor === "Orçamentos") {
        await apiBudgets.save(form)
        setEditor("")
        setNotice("Orçamento salvo na API.")
      } else if (editor === "Financeiro") {
        await apiFinance.save(form)
        setEditor("")
        setNotice("Movimentação salva na API.")
      } else if (editor === "Acompanhamentos") {
        await apiFollowUps.save(form)
        setEditor("")
        setNotice("Acompanhamento salvo na API.")
      } else if (editor === "Contratos") {
        await apiContracts.save(form)
        setEditor("")
        setNotice("Contrato salvo na API.")
      }
    } catch (failure) {
      setError(failure.message)
    }
  }
  const related = (key, entry) =>
    list(key).filter((item) =>
      page === "Clientes"
        ? item.clientId === entry.id ||
          (item.projectId && find("Projetos", item.projectId)?.clientId === entry.id)
        : item.projectId === entry.id,
    )
  const context =
    apiClientPage && apiClients.detailState === "success" && apiClients.client?.id === selected
      ? apiClients.client
      : apiProjectPage && apiProjects.detailState === "success" && apiProjects.project?.id === selected
        ? apiProjects.project
        : null
  const clientContext =
    page === "Clientes"
      ? context ?? (selected ? { id: selected, name: "Cliente" } : null)
      : null
  const effectiveContext = page === "Clientes" ? clientContext : context
  const budgets = list("Orçamentos").filter((item) => !context || item.projectId === context.id)
  const defaults = context
    ? page === "Clientes"
      ? { clientId: context.id }
      : { projectId: context.id, clientId: context.clientId }
    : clientContext
      ? { clientId: clientContext.id }
      : {}
  const normalizeUrl = (url) => {
    const value = typeof url === "string" ? url.trim() : ""
    if (!value) return ""
    if (/^[a-z][a-z\d+.-]*:\/\//i.test(value)) return value
    if (/^\/\//.test(value)) return `https:${value}`
    if (/\s/.test(value)) return ""
    if (/^www\./i.test(value)) return `https://${value}`
    if (value.includes(".") || value.includes("/")) return `https://${value}`
    return ""
  }
  const showLink = (url, label = "Abrir link", className) => {
    const href = normalizeUrl(url)
    return href ? (
      <Button
        variant="neutral"
        className={cssClass(className)}
        onClick={() => window.open(href, "_blank", "noopener,noreferrer")}
      >
        {label}
      </Button>
    ) : null
  }
  const homeRows = (entries, emptyMessage) =>
    entries.length ? (
      <div className={cssClass("record-list")}>
        {entries.map((item) => {
          const state = item.status || item.type || ""
          const hint = [dateLabel(item.date), item.type || item.location || ""]
            .filter(Boolean)
            .join(" · ")
          return (
            <Button
              key={item.id}
              variant="subtle"
              className={cssClass("record-button")}
              onClick={() =>
                setDetail({
                  ...item,
                  collection: item.collection || "Acompanhamentos",
                })
              }
            >
              <span className={cssClass("record-content")}>
                <span className={cssClass("record-main")}>
                  <strong>{item.name || "Registro"}</strong>
                  {hint && <small>{hint}</small>}
                </span>
                {state && (
                  <span className={cssClass(`tag ${statusTone(state)}`)}>{state}</span>
                )}
              </span>
            </Button>
          )
        })}
      </div>
    ) : (
      <div className={cssClass("empty")}>
        <strong>{emptyMessage}</strong>
      </div>
    )
  const rows = (entries, key) => {
    const clientEmptyStates = {
      Agenda: "Nenhuma sessão agendada para este cliente.",
      Projetos: "Nenhum projeto cadastrado para este cliente.",
      Acompanhamentos: "Nenhum acompanhamento cadastrado para este cliente.",
      Financeiro: "Nenhum pagamento registrado para este cliente.",
    }
    return entries.length ? (
      <div className={cssClass("record-list")}>
        {entries.map((item) => {
          const state =
            key === "Clientes"
              ? clientStatus(item.id)
              : key === "Projetos"
                ? status(item)
                : key === "Agenda"
                  ? sessionStatus(item)
                  : item.status || ""
          return (
            <Button
              variant="subtle"
              className={cssClass(
                `record-button ${key === "Orçamentos" ? "budget-record" : ""} ${
                  key === "Clientes" || key === "Projetos"
                    ? "entity-record"
                    : ""
                }`,
              )}
                key={item.id}
                onClick={() =>
                  key === "Clientes" || key === "Projetos"
                    ? navigate(key, item.id)
                    : key === "Agenda" &&
                        item.kind === "Sessão" &&
                      item.projectId &&
                      !((page === "Projetos" || page === "Clientes") && context)
                    ? navigate("Projetos", item.projectId)
                    : setDetail({ ...item, collection: key })
               }
            >
              <span className={cssClass("record-content")}>
                {key === "Orçamentos" ? (
                  <>
                    <span className={cssClass("budget-date")}>
                      {dateLabel(item.date)}
                    </span>
                    <strong className={cssClass("budget-name")}>
                      {item.name}
                    </strong>
                    <span className={cssClass("budget-value")}>
                      {money(number(item.total))}
                    </span>
                  </>
                ) : (
                  <>
                    <span className={cssClass("record-main")}>
                      <strong>{item.name}</strong>
                      {key === "Projetos" && (
                        <small>
                          {find("Clientes", item.clientId)?.name ||
                            "Cliente não informado"}
                        </small>
                      )}
                      {key !== "Clientes" && key !== "Projetos" && (
                        <small>
                          {`${dateLabel(item.date)} ${item.start || ""} · ${item.location || item.type || ""}`}
                        </small>
                      )}
                    </span>
                  </>
                )}
                {key !== "Orçamentos" && (
                  <span
                    className={cssClass(
                      `tag ${statusTone(state)} ${
                        key === "Projetos" ? "tag-project-status" : ""
                      }`,
                    )}
                  >
                    {key === "Financeiro"
                      ? `${money(number(item.value))} · ${state}`
                      : state}
                  </span>
                )}
              </span>
            </Button>
          )
        })}
      </div>
    ) : (
      <div className={cssClass("empty")}>
        <strong>
          {page === "Clientes" && effectiveContext
            ? clientEmptyStates[key] || "Nenhum registro por aqui."
            : "Nenhum registro por aqui."}
        </strong>
        <p>Use o botão de adicionar deste bloco para iniciar.</p>
      </div>
    )
  }
  const calendarDate = new Date(`${focus}T12:00:00`)
  const days = new Date(
    calendarDate.getFullYear(),
    calendarDate.getMonth() + 1,
    0,
  ).getDate()
  const offset = new Date(
    calendarDate.getFullYear(),
    calendarDate.getMonth(),
    1,
  ).getDay()
  const calendarDates =
    view === "Mês"
      ? Array.from(
          { length: Math.ceil((offset + days) / 7) * 7 },
          (_, index) =>
            index < offset || index >= offset + days
              ? ""
              : `${focus.slice(0, 7)}-${String(index - offset + 1).padStart(2, "0")}`,
        )
      : Array.from({ length: view === "Semana" ? 7 : 1 }, (_, index) => {
          const date = new Date(calendarDate)
          date.setDate(
            date.getDate() + index - (view === "Semana" ? date.getDay() : 0),
          )
          return date.toLocaleDateString("en-CA")
        })
  const agendaEntries = list("Agenda")
  const agenda = agendaEntries.filter(
    (item) =>
      (location === "Todos" || item.location === location) &&
      (activityType === "Todos" || item.kind === activityType) &&
      (projectStatus === "Todos" ||
        status(
          find("Projetos", item.projectId) || { id: "", name: "", status: "" },
        ) === projectStatus),
  )
  const moveDate = (direction) => {
    const date = new Date(calendarDate)
    if (view === "Ano") {
      date.setFullYear(date.getFullYear() + direction)
    } else if (view === "Mês") {
      date.setDate(1)
      date.setMonth(date.getMonth() + direction)
    } else
      date.setDate(date.getDate() + direction * (view === "Semana" ? 7 : 1))
    setFocus(date.toLocaleDateString("en-CA"))
  }
  const selector = (label, value, onChange, values) => (
    <SelectField
      label={label}
      value={value}
      onChange={onChange}
      options={values.map((v) => ({ value: v, label: v }))}
    />
  )
  const locationOptions = (() => {
    const defaults = ["Mauá", "Vila Mariana"]
    const unique = Array.from(new Set([...defaults, ...agendaEntries.map((entry) => entry.location).filter(Boolean)]))
    return ["Todos", ...unique]
  })()
  const activityOptions = (() => {
    const unique = Array.from(new Set(agendaEntries.map((entry) => entry.kind).filter(Boolean)))
    return unique.length ? ["Todos", ...unique] : ["Todos", "Sessão", "Tarefa", "Evento"]
  })()
  const calendarFilters = ({ close }) => (
    <div className={cssClass("calendar-filters")}>
      {selector("Local", location, setLocation, locationOptions)}
      {selector("Tipo", activityType, setActivityType, activityOptions)}
      {selector(
        "Status do projeto",
        projectStatus,
        setProjectStatus,
        ["Todos", ...pipeline],
      )}
      <div className={cssClass("calendar-filters-actions")}>
        <Button
          variant="subtle"
          size="small"
          onClick={() => {
            setLocation("Todos")
            setActivityType("Todos")
            setProjectStatus("Todos")
            close?.()
          }}
        >
          Limpar filtros
        </Button>
      </div>
    </div>
  )
  const addHour = (time) => {
    if (!time) return ""
    const [hours, minutes] = time.split(":").map((value) => Number(value) || 0)
    const total = hours * 60 + minutes + 60
    const nextHour = Math.floor((total % (24 * 60)) / 60)
    const nextMinute = total % 60
    return `${String(nextHour).padStart(2, "0")}:${String(nextMinute).padStart(2, "0")}`
  }
  const handleCalendarCreate = (kind, date, time) => {
    if (kind === "Projeto") {
      open("Projetos")
      return
    }
    const start = time || "09:00"
    const end = time ? addHour(time) : ""
    open("Agenda", undefined, {
      kind: kind === "Sessão" ? "Sessão" : kind,
      date: date || focus,
      start,
      end,
    })
  }
  const handleActivityInspect = (entry) => setDetail({ ...entry, collection: "Agenda" })
  const upcomingAgenda = agendaEntries
    .filter((entry) => entry.date && entry.date >= today())
    .sort((a, b) => (a.date || "").localeCompare(b.date || "") || (a.start || "").localeCompare(b.start || ""))
  const homeAlerts = studioDashboard({
    Clientes: list("Clientes"),
    Projetos: list("Projetos"),
    Agenda: list("Agenda"),
    Financeiro: list("Financeiro"),
    Acompanhamentos: list("Acompanhamentos"),
  }).alerts
  const homeAlertsLoading =
    apiAgenda.listState === "loading" ||
    apiProjects.listState === "loading" ||
    apiFollowUps.listState === "loading"
  const homeAlertsError =
    (apiFollowUps.listState === "error" && apiFollowUps.loadError) ||
    (apiAgenda.listState === "error" && apiAgenda.loadError) ||
    (apiProjects.listState === "error" && apiProjects.loadError) ||
    ""
  const summary = (values) => (
    <div className={cssClass("overview-grid")}>
      {values.map(([label, value]) => (
        <section className={cssClass("metric")} key={label}>
          <span className={cssClass("text-label-sm text-text-secondary")}>
            {label}
          </span>
          <div className={cssClass("metric-value")}>{money(value)}</div>
        </section>
      ))}
    </div>
  )
  return (
    <div className={cssClass("app-shell")}>
      <div
        className={cssClass("sidebar-interaction")}
        onClick={(event) => {
          if (
            event.target instanceof Element &&
            !event.target.closest(
              "button, a, input, select, textarea, [role='button']",
            )
          ) {
            setCollapsed((value) => !value)
          }
        }}
      >
        <SidebarNavigation
          className={cssClass(collapsed ? "navigation compact" : "navigation")}
          footer={
            <div className={cssClass("nav-footer")}>
              {collapsed ? (
                <IconButton
                  className={cssClass("sign-out")}
                  variant="primary"
                  aria-label="Sair"
                  title="Sair"
                  icon={<LogOut className={cssClass("nav-icon")} />}
                  onClick={onExit}
                />
              ) : (
                <Button
                  className={cssClass("sign-out sign-out-expanded")}
                  variant="primary"
                  iconStart={<LogOut className={cssClass("nav-icon")} />}
                  onClick={onExit}
                >
                  Sair
                </Button>
              )}
            </div>
          }
        >
          <div className={cssClass("brand")}>
            <img src="/logo.png" alt="Kawz Tattoo" />
            <div className={cssClass("brand-copy")}>
              <strong>KAWZ TATTOO</strong>
            </div>
            <IconButton
              variant="subtle"
              className={cssClass("sidebar-toggle")}
              aria-label={collapsed ? "Expandir sidebar" : "Recolher sidebar"}
              title={collapsed ? "Expandir sidebar" : "Recolher sidebar"}
              aria-expanded={!collapsed}
              onClick={() => setCollapsed((value) => !value)}
              icon={
                <svg
                  className={cssClass("nav-icon")}
                  aria-hidden="true"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth={2}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                >
                  <rect x="3" y="3" width="18" height="18" rx="2" />
                  <path d="M9 3v18" />
                  <path d={collapsed ? "m13 9 3 3-3 3" : "m16 9-3 3 3 3"} />
                </svg>
              }
            />
          </div>
          {pages.map((label) => (
            <Button
              as={NavLink}
              to={paths[label]}
              key={label}
              title={label}
              aria-label={label}
              variant="subtle"
              iconStart={
                <span className={cssClass("nav-icon-wrap")} aria-hidden="true">
                  {pageIcons[label]}
                </span>
              }
              className={cssClass(
                `nav-link ${label === "Início" ? "nav-link-home" : ""} ${
                  page === label ? "selected" : ""
                }`,
              )}
            >
              {!collapsed && label}
            </Button>
          ))}
        </SidebarNavigation>
      </div>
      <div className={cssClass("main-wrap")}>
        <main
          className={cssClass(
            `workspace page-${page.toLowerCase()} ${
              effectiveContext ? "has-context" : ""
            }`,
          )}
        >
          {notice && (
            <div className={cssClass("feedback-toast")} role="status">
              <div className={cssClass("feedback")}>
                <span>{notice}</span>
                <Button variant="subtle" onClick={() => setNotice("")}>
                  Fechar
                </Button>
              </div>
            </div>
          )}
          <div className={cssClass("page-header")}>
            <div className={cssClass(effectiveContext ? "client-heading" : undefined)}>
              {effectiveContext && (
                <IconButton
                  size="small"
                  variant="neutral"
                  aria-label={`Voltar para ${page.toLowerCase()}`}
                  icon={<ArrowLeft className={cssClass("control-icon")} />}
                  onClick={() => navigate(page)}
                />
              )}
              <div
                className={cssClass(
                  effectiveContext && page === "Projetos"
                    ? "project-heading-details"
                    : undefined,
                )}
              >
                <div
                  role="heading"
                  aria-level={1}
                  className={cssClass("page-title")}
                >
                  {effectiveContext?.name ||
                    (page === "Início" ? `Olá, ${profile.name.trim()}!` : page)}
                </div>
                {effectiveContext && (
                  <span
                    className={cssClass(
                      `tag ${statusTone(page === "Projetos" ? status(effectiveContext) : clientStatus(effectiveContext.id))} ${
                        page === "Projetos"
                          ? "tag-project-status"
                          : ""
                      }`,
                    )}
                  >
                    {page === "Projetos"
                      ? status(effectiveContext)
                      : clientStatus(effectiveContext.id)}
                  </span>
                )}
                {effectiveContext && page === "Projetos" && (
                  <span className={cssClass("project-client-name")}>
                    {find("Clientes", effectiveContext.clientId)?.name ||
                      "Cliente não informado"}
                  </span>
                )}
              </div>
            </div>
            <div className={cssClass("action-row")}>
               {page === "Financeiro" && (
                 <>
                   <MonthPicker value={period} onChange={setPeriod} />
                    <FinanceActions onCreate={(kind) => open("Financeiro", undefined, { kind })} />
                  </>
                )}
              {page !== "Início" &&
                page !== "Agenda" &&
                page !== "Financeiro" &&
                !context && (
                  <Button
                    disabled={!(["Clientes", "Projetos"].includes(page))}
                    title={!(["Clientes", "Projetos"].includes(page)) ? "Funcionalidade ainda não integrada à API" : undefined}
                    onClick={() =>
                      open(
                        context
                          ? page
                          : page === "Clientes"
                            ? "Clientes"
                            : page === "Projetos"
                              ? "Projetos"
                              : page === "Financeiro"
                                ? "Financeiro"
                                : "Agenda",
                        context,
                      )
                    }
                  >
                    {context
                      ? "Editar dados"
                      : page === "Clientes"
                        ? "+ Novo cliente"
                        : page === "Projetos"
                          ? "+ Novo projeto"
                          : page === "Financeiro"
                            ? "+ Registrar pagamento"
                            : "+ Nova sessão"}
                  </Button>
                )}
            </div>
          </div>
            {selected && !context && page !== "Clientes" && !["Clientes", "Projetos"].includes(page) ? (
              <Panel title={page}><p role="status">Esta ficha ainda não está integrada à API.</p><Button as={Link} to={paths[page]} variant="neutral">Voltar para {page.toLowerCase()}</Button></Panel>
            ) : selected && !context && page !== "Clientes" ? (
              (apiClientPage ? apiClients.detailState : apiProjects.detailState) === "notFound" ? (
              <Panel title="Registro não encontrado">
                <p>{apiClientPage ? "Não foi possível encontrar este cliente na API." : "Não foi possível encontrar este projeto na API."}</p>
                <Button as={Link} to={paths[page]} variant="neutral">Voltar para {page.toLowerCase()}</Button>
              </Panel>
              ) : (apiClientPage ? apiClients.detailState : apiProjects.detailState) === "error" ? (
                <Panel title={apiClientPage ? "Falha ao carregar cliente" : "Falha ao carregar projeto"}><p role="alert">{apiClientPage ? apiClients.loadError : apiProjects.loadError}</p><Button variant="neutral" onClick={apiClientPage ? apiClients.reload : apiProjects.reload}>Tentar novamente</Button></Panel>
              ) : <Panel title={apiClientPage ? "Dados do cliente" : "Dados do projeto"}><p role="status">{apiClientPage ? "Carregando cliente…" : "Carregando projeto…"}</p></Panel>
            ) : page === "Início" ? (
            <>
              <Panel
                title="Próximos compromissos"
                action={
                  <div className={cssClass("action-row")}>
                    <Button variant="subtle" onClick={() => navigate("Agenda")}>
                       Ver agenda →
                     </Button>
                  </div>
                }
              >
                 {!apiMode ? (
                   <p role="status">Compromissos ainda não integrados à API.</p>
                 ) : apiAgenda.listState === "error" ? (
                   <p role="alert">{apiAgenda.loadError}</p>
                 ) : apiAgenda.listState !== "success" ? (
                   <p role="status">Carregando agenda…</p>
                 ) : (
                   rows(upcomingAgenda.slice(0, 5), "Agenda")
                 )}
              </Panel>
              <div className={cssClass("section-stack")}>
                <Panel
                  title="Avisos e acompanhamentos"
                 >
                    {homeAlertsError ? (
                      <p role="alert">{homeAlertsError}</p>
                    ) : homeAlertsLoading ? (
                      <p role="status">Carregando avisos e acompanhamentos…</p>
                    ) : (
                      homeRows(homeAlerts, "Nenhum registro por aqui.")
                    )}
                 </Panel>
               </div>
             </>
          ) : effectiveContext ? (
            <>
              <div className={cssClass("lower-grid")}>
                <Panel
                  className={cssClass(
                    page === "Clientes"
                      ? "client-information"
                      : "project-information",
                  )}
                  title={"Dados Gerais"}
                  action={
                    page === "Clientes" ? (
                      <div className={cssClass("action-row detail-tabs")}>
                        {["Dados", "Preferências"].map((label) => (
                          <Button
                            key={label}
                            size="small"
                            variant={tab === label ? "primary" : "neutral"}
                            onClick={() => setTab(label)}
                          >
                            {label}
                          </Button>
                        ))}
                        <IconButton
                          size="small"
                          className={cssClass(
                            "client-icon-button client-edit-button",
                          )}
                          variant="neutral"
                          aria-label={
                            tab === "Preferências"
                              ? "Editar preferências"
                              : "Editar dados"
                          }
                          icon={<Pencil className={cssClass("control-icon")} />}
                          disabled={!context}
                          onClick={() => {
                            if (!context) return
                            open("Clientes", context)
                            setFormSection(
                              tab === "Preferências"
                                ? "Preferências"
                                : "Dados pessoais",
                            )
                          }}
                        />
                        <IconButton
                          size="small"
                          className={cssClass(
                            "client-icon-button client-delete-button",
                          )}
                          variant="neutral"
                          aria-label="Excluir cliente"
                          title="Excluir cliente"
                          icon={<Trash2 className={cssClass("control-icon")} />}
                          disabled={!context || apiClients.saving}
                          onClick={async () => {
                            if (!context) return
                            if (!window.confirm(`Excluir ${context.name} da API?`)) return
                            try {
                              await apiClients.remove(context.id)
                              routerNavigate(paths.Clientes, {
                                state: { notice: "Cliente excluído da API." },
                              })
                            } catch (failure) {
                              setNotice(failure.message)
                            }
                          }}
                        />
                      </div>
                    ) : (
                      <div className={cssClass("action-row detail-tabs")}>
                        {["Dados", "Características", "Acompanhamento"].map(
                          (label) => (
                            <Button
                              key={label}
                              size="small"
                              variant={tab === label ? "primary" : "neutral"}
                              onClick={() => setTab(label)}
                            >
                              {label}
                            </Button>
                          ),
                        )}
                        <IconButton
                          size="small"
                          className={cssClass(
                            "client-icon-button client-edit-button",
                          )}
                          variant="neutral"
                          aria-label={
                            tab === "Acompanhamento"
                              ? "Editar acompanhamento"
                              : tab === "Características"
                                ? "Editar características"
                                : "Editar dados"
                          }
                          icon={<Pencil className={cssClass("control-icon")} />}
                          onClick={() => {
                            if (!context) return
                            open(page, context)
                            setFormSection(
                              tab === "Características"
                                ? "Características"
                                : tab === "Acompanhamento"
                                  ? "Acompanhamento"
                                  : "Dados gerais",
                            )
                          }}
                          disabled={!context}
                        />
                        <IconButton
                          size="small"
                          className={cssClass(
                            "client-icon-button client-delete-button",
                          )}
                          variant="neutral"
                          aria-label="Excluir projeto"
                          title="Excluir projeto"
                          icon={<Trash2 className={cssClass("control-icon")} />}
                          disabled={!context || apiProjects.saving}
                          onClick={async () => {
                            if (!context) return
                            if (!window.confirm(`Excluir ${context.name} da API?`)) return
                            try {
                              await apiProjects.remove(context.id)
                              routerNavigate(paths.Projetos, {
                                state: { notice: "Projeto excluído da API." },
                              })
                            } catch (failure) {
                              setNotice(failure.message)
                            }
                          }}
                        />
                      </div>
                    )
                  }
                >
                  <dl className={cssClass("detail-grid")}>
                    {fields[page]
                      .filter(([key]) =>
                        page === "Clientes"
                          ? (tab === "Preferências"
                              ? ["music", "food", "style", "habits", "notes"]
                              : [
                                  "name",
                                  "phone",
                                  "instagram",
                                  "channel",
                                  "status",
                                ]
                            ).includes(key)
                          : tab === "Dados"
                            ? ["name", "status", "clientId"].includes(
                                key,
                              )
                            : tab === "Características"
                              ? [
                                  "description",
                                  "style",
                                  "size",
                                  "body",
                                  "complexity",
                                  "colors",
                                ].includes(key)
                              : ["pre", "post"].includes(key),
                      )
                      .map(([key, label]) => (
                        <div key={key}>
                          <dt>{label}</dt>
                          <dd>
                            {key === "clientId" ? (
                              find("Clientes", effectiveContext.clientId)?.name
                            ) : key === "status" ? (
                              status(effectiveContext)
                            ) : (
                              effectiveContext[key] || "Não informado"
                            )}
                          </dd>
                        </div>
                      ))}
                  </dl>
                  {page === "Clientes" && tab === "Dados" && (
                    <div className={cssClass("action-row")}> 
                      {(() => {
                        const folderValue =
                          effectiveContext.folder || effectiveContext.linkPasta || ""
                        const folderHref = normalizeUrl(folderValue)
                        return (
                          <>
                      <ConversationButton
                        client={effectiveContext}
                        onFeedback={setNotice}
                      />
                            {folderHref ? (
                              <Button
                                as="a"
                                href={folderHref}
                                target="_blank"
                                rel="noopener noreferrer"
                                size="small"
                                variant="neutral"
                                className={cssClass("client-conversation-button")}
                                iconStart={
                                  <Folder className={cssClass("control-icon")} />
                                }
                              >
                                Abrir pasta de referências
                              </Button>
                            ) : (
                              <Button
                                size="small"
                                variant="neutral"
                                className={cssClass("client-conversation-button")}
                                iconStart={
                                  <Folder className={cssClass("control-icon")} />
                                }
                                onClick={() => {
                                  setNotice(
                                    "Cliente sem pasta de referências cadastrada.",
                                  )
                                }}
                              >
                                Abrir pasta de referências
                              </Button>
                            )}
                          </>
                        )
                      })()}
                    </div>
                  )}
                  {page === "Projetos" && tab === "Dados" && (
                    <div className={cssClass("action-row")}>
                      {showLink(
                        effectiveContext.folder,
                        "Abrir pasta do projeto",
                        "client-info-link",
                      )}
                    </div>
                  )}
                  {page === "Projetos" && effectiveContext.reference && (
                    <figure className={cssClass("project-reference")}>
                      <img
                        src={effectiveContext.reference}
                        alt={
                          effectiveContext.referenceName ||
                          `Referência de ${effectiveContext.name}`
                        }
                      />
                      <figcaption>
                        {effectiveContext.referenceName || "Imagem de referência"}
                      </figcaption>
                    </figure>
                  )}
                </Panel>
                <Panel
                  className={cssClass(
                    page === "Clientes"
                      ? "client-sessions"
                      : "project-sessions",
                  )}
                  title={page === "Clientes" ? "Próximas sessões" : "Sessões"}
                  action={
                    <IconButton
                      size="small"
                      className={cssClass("client-icon-button")}
                      variant="neutral"
                      aria-label="Cadastrar sessão"
                      title="Cadastrar sessão"
                      icon={<Plus className={cssClass("control-icon")} />}
                      onClick={() =>
                        open("Agenda", undefined, {
                          ...defaults,
                          kind: "Sessão",
                        })
                      }
                    />
                  }
                >
                  {rows(
                    related("Agenda", effectiveContext)
                      .filter(
                        (s) =>
                          s.kind === "Sessão" &&
                          (page !== "Clientes" ||
                            (s.date >= today() && s.status !== "Cancelada")),
                      )
                      .sort((a, b) => a.date.localeCompare(b.date)),
                    "Agenda",
                  )}
                </Panel>
                {page === "Clientes" ? (
                  <Panel
                    className={cssClass("client-projects")}
                    title="Projetos"
                    action={
                      <IconButton
                        size="small"
                        className={cssClass("client-icon-button")}
                        variant="neutral"
                        aria-label="Cadastrar projeto"
                        title="Cadastrar projeto"
                        icon={<Plus className={cssClass("control-icon")} />}
                        onClick={() => open("Projetos", undefined, defaults)}
                      />
                    }
                  >
                     {rows(related("Projetos", effectiveContext), "Projetos")}
                  </Panel>
                ) : (
                  <Panel
                    className={cssClass("project-budget")}
                    title="Orçamentos"
                    action={
                      <IconButton
                        className={cssClass("client-icon-button")}
                        variant="neutral"
                        aria-label="Adicionar orçamento"
                        title="Adicionar orçamento"
                        icon={<Plus className={cssClass("control-icon")} />}
                        onClick={() =>
                          open("Orçamentos", undefined, {
                            ...defaults,
                            name: `Orçamento · ${effectiveContext.name}`,
                            deposit: "0",
                            estimate: effectiveContext.estimate || "1",
                            expires: effectiveContext.expires || "",
                          })
                        }
                      />
                    }
                  >
                    {rows(budgets, "Orçamentos")}
                  </Panel>
                )}
                {page === "Clientes" && (
                  <Panel
                    className={cssClass("client-followups")}
                    title="Acompanhamentos"
                    action={
                      <IconButton
                        size="small"
                        className={cssClass("client-icon-button")}
                        variant="neutral"
                        aria-label="Cadastrar acompanhamento"
                        title="Cadastrar acompanhamento"
                        icon={<Plus className={cssClass("control-icon")} />}
                        onClick={() =>
                          open("Acompanhamentos", undefined, defaults)
                        }
                      />
                    }
                  >
                    {rows(
                      related("Acompanhamentos", effectiveContext),
                      "Acompanhamentos",
                    )}
                  </Panel>
                )}
                <Panel
                  className={cssClass(
                    page === "Clientes" ? "client-payments" : "project-finance",
                  )}
                  title={page === "Clientes" ? "Pagamentos" : "Financeiro"}
                  action={
                    <IconButton
                      size="small"
                      className={cssClass("client-icon-button")}
                      variant="neutral"
                      aria-label={
                        page === "Clientes"
                          ? "Registrar pagamento"
                          : "Novo lançamento"
                      }
                      title={
                        page === "Clientes"
                          ? "Registrar pagamento"
                          : "Novo lançamento"
                      }
                      icon={<Plus className={cssClass("control-icon")} />}
                      onClick={() => open("Financeiro", undefined, defaults)}
                    />
                  }
                >
                  {rows(
                    related("Financeiro", effectiveContext).filter(
                      (entry) =>
                        page !== "Clientes" || entry.kind !== "Despesa",
                    ),
                    "Financeiro",
                  )}
                </Panel>
                {page === "Projetos" && (
                  <Panel
                    className={cssClass("project-contracts")}
                    title="Contratos"
                    action={
                      <IconButton
                        className={cssClass("client-icon-button")}
                        variant="neutral"
                        aria-label="Gerar contrato"
                        title="Gerar contrato"
                        icon={<Plus className={cssClass("control-icon")} />}
                        onClick={() =>
                          open("Contratos", undefined, {
                            ...defaults,
                            name: `Contrato · ${effectiveContext.name}`,
                          })
                        }
                      />
                    }
                  >
                    {rows(related("Contratos", effectiveContext), "Contratos")}
                  </Panel>
                )}
              </div>
            </>
           ) : page === "Agenda" ? (
             !apiMode ? (
               <>
                 <p role="status">A agenda ainda não está integrada à API. Nenhuma atividade pode ser consultada ou cadastrada aqui.</p>
                 <Calendar
                   unavailable
                   view={view}
                   setView={setView}
                   focus={focus}
                   setFocus={setFocus}
                   dates={calendarDates}
                   entries={[]}
                   move={moveDate}
                 />
               </>
             ) : apiAgenda.listState === "error" ? (
               <Panel title="Agenda">
                 <p role="alert">{apiAgenda.loadError}</p>
                 <Button variant="neutral" onClick={apiAgenda.reload}>Tentar novamente</Button>
               </Panel>
             ) : (
               <>
                 {apiAgenda.listState !== "success" && (
                   <p role="status">Carregando agenda…</p>
                 )}
                 <Calendar
                   unavailable={apiAgenda.listState !== "success"}
                   view={view}
                   setView={setView}
                   focus={focus}
                   setFocus={setFocus}
                   dates={calendarDates}
                   entries={agenda}
                   move={moveDate}
                   filters={calendarFilters}
                   create={handleCalendarCreate}
                   inspect={handleActivityInspect}
                 />
               </>
             )
             ) : page === "Financeiro" ? (
               <FinanceDashboard
                 period={period}
                 transactions={list("Financeiro")}
                 clients={list("Clientes")}
                 projects={list("Projetos")}
                 onInspect={(entry) => setDetail({ ...entry, collection: "Financeiro" })}
                 unavailable={apiFinance.listState !== "success"}
               />
             ) : activeApiViewModel.listState === "error" ? (
              <Panel title={apiClientPage ? "Falha ao carregar clientes" : "Falha ao carregar projetos"}><p role="alert">{activeApiViewModel.loadError}</p><Button variant="neutral" onClick={activeApiViewModel.reload}>Tentar novamente</Button></Panel>
            ) : activeApiViewModel.listState !== "success" ? (
              <Panel title={apiClientPage ? "Clientes" : "Projetos"}><p role="status">{apiClientPage ? "Carregando clientes…" : "Carregando projetos…"}</p></Panel>
            ) : (
              <Panel title="">
               <div className={cssClass("records-toolbar")}>
                <InputField
                  className={cssClass("records-search")}
                   aria-label="Pesquisar"
                   placeholder={
                      page === "Clientes"
                        ? "nome, contato ou status"
                        : "nome do projeto"
                    }
                  prefix={<Search className={cssClass("control-icon")} />}
                  value={query}
                  onChange={setQuery}
                />
                <div className={cssClass("compact-filter")}>
                  {selector(
                     "Status",
                     filter,
                     setFilter,
                     page === "Clientes"
                       ? ["Interessado", "Cliente"]
                       : ["Todos", ...pipeline],
                   )}
                 </div>
               </div>
              {rows(
                list(page).filter(
                  (item) =>
                    (page === "Clientes"
                      ? [
                          item.name,
                          item.phone,
                          item.email,
                          item.instagram,
                          clientStatus(item.id),
                        ].join(" ")
                      : item.name
                    )
                      .toLowerCase()
                      .includes(query.toLowerCase()) &&
                    ((page === "Clientes" && !["Interessado", "Cliente"].includes(filter)) ||
                      filter === "Todos" ||
                      (page === "Clientes"
                        ? clientStatus(item.id)
                        : status(item)) === filter),
                ),
                page,
              )}
               {apiCollectionPage && <div className={cssClass("action-row")}>
                  <Button variant="neutral" disabled={activeApiViewModel.pageIndex === 0} onClick={() => activeApiViewModel.setPageIndex(activeApiViewModel.pageIndex - 1)}>Anterior</Button>
                  <span>Página {activeApiViewModel.pageIndex + 1} de {Math.max(1, activeApiViewModel.totalPages)}</span>
                  <Button variant="neutral" disabled={activeApiViewModel.pageIndex + 1 >= activeApiViewModel.totalPages} onClick={() => activeApiViewModel.setPageIndex(activeApiViewModel.pageIndex + 1)}>Próxima</Button>
               </div>}
            </Panel>
          )}
        </main>
      </div>
      <Modal
        className={cssClass("floating-glass")}
        isOpen={!!editor}
        title={
          editor === "Financeiro"
            ? form.kind === "Despesa"
              ? "Registrar gasto"
              : "Registrar pagamento"
            : `${form.id ? "Editar" : "Cadastrar"} · ${
                editor === "Agenda" ? form.kind : editor
              }`
        }
        onClose={() => setEditor("")}
        size="large"
        footer={
          <>
            <Button variant="neutral" onClick={() => setEditor("")}>
              Cancelar
            </Button>
            <Button
              disabled={
                (editor === "Clientes" && apiClients.saving) ||
                (editor === "Projetos" && apiProjects.saving) ||
                (editor === "Agenda" && apiAgenda.saving) ||
                (editor === "Orçamentos" && apiBudgets.saving) ||
                (editor === "Financeiro" && apiFinance.saving) ||
                (editor === "Acompanhamentos" && apiFollowUps.saving) ||
                (editor === "Contratos" && apiContracts.saving)
              }
              onClick={save}
            >
              {(editor === "Clientes" && apiClients.saving) ||
              (editor === "Projetos" && apiProjects.saving) ||
              (editor === "Agenda" && apiAgenda.saving) ||
              (editor === "Orçamentos" && apiBudgets.saving) ||
              (editor === "Financeiro" && apiFinance.saving) ||
              (editor === "Acompanhamentos" && apiFollowUps.saving) ||
              (editor === "Contratos" && apiContracts.saving)
                ? "Salvando…"
                : "Salvar"}
            </Button>
          </>
        }
      >
        {["Clientes", "Projetos"].includes(editor) && (
          <div className={cssClass("action-row form-sections")}>
            {(editor === "Clientes"
              ? ["Dados pessoais", "Preferências"]
              : [
                  "Dados gerais",
                  "Características",
                  "Orçamento",
                  "Acompanhamento",
                ]
            ).map((section) => (
              <Button
                size="small"
                key={section}
                variant={formSection === section ? "primary" : "neutral"}
                onClick={() => setFormSection(section)}
              >
                {section}
              </Button>
            ))}
          </div>
        )}
        <div
          className={cssClass(
            editor === "Agenda"
              ? "activity-editor"
              : editor === "Clientes"
                ? "client-editor"
                : ["Projetos", "Orçamentos"].includes(editor)
                  ? "project-editor"
                  : "",
          )}
        >
          <div className={cssClass("editor-fields")}>
            {(fields[editor] || [])
              .filter(([key]) => {
                const visible = visibleFormFields(editor, form, formSection)
                return !visible || visible.includes(key)
              })
              .map(([key, originalLabel, type]) => {
                const label =
                  editor === "Agenda" &&
                  form.kind === "Sessão" &&
                  key === "name"
                    ? "Nome da sessão"
                    : editor === "Financeiro" && key === "name"
                      ? "Descrição do gasto"
                      : editor === "Financeiro" &&
                          key === "date" &&
                          form.kind !== "Despesa"
                        ? form.status === "Pendente"
                          ? "Vencimento"
                          : "Data do recebimento"
                        : originalLabel
                const update = (value) =>
                  setForm((previous) => ({
                    ...previous,
                    [key]: value,
                    ...(key === "projectId"
                      ? {
                          clientId:
                            find("Projetos", value)?.clientId ||
                            previous.clientId,
                          sessionId: "",
                        }
                      : {}),
                  }))
                if (editor === "Projetos" && type === "image")
                  return (
                    <ProjectImageField
                      key={`${editor}-${form.id || "new"}-${key}`}
                      value={
                        form.reference
                          ? {
                              id: form.referenceId || "",
                              name: form.referenceName || "Referência",
                              url: form.reference,
                            }
                          : null
                      }
                      onChange={(image) =>
                        setForm((previous) => ({
                          ...previous,
                          reference: image?.url || "",
                          referenceId: image?.id || "",
                          referenceName: image?.name || "",
                        }))
                      }
                    />
                  )
                const SelectionField = ["Projetos", "Orçamentos"].includes(
                  editor,
                )
                  ? ProjectSelectField
                  : SelectField
                if (Array.isArray(type))
                  return (
                    <SelectionField
                      key={key}
                      label={label}
                      value={form[key] || ""}
                      onChange={update}
                      options={type.map((value) => ({ value, label: value }))}
                    />
                  )
                if (type && ["Clientes", "Projetos", "Agenda"].includes(type))
                  return (
                    <SelectionField
                      key={key}
                      label={label}
                      value={form[key] || ""}
                      onChange={update}
                      options={[
                        { value: "", label: "Selecione" },
                        ...list(type)
                          .filter((item) =>
                            type === "Agenda"
                              ? item.kind === "Sessão" &&
                                (!form.projectId ||
                                  item.projectId === form.projectId)
                              : type === "Projetos"
                                ? !form.clientId ||
                                  item.clientId === form.clientId
                                : true,
                          )
                          .map((item) => ({
                            value: item.id,
                            label: item.name,
                          })),
                      ]}
                    />
                  )
                if (type === "date" || type === "time")
                  return (
                    <DateTimeField
                      key={key}
                      label={label}
                      type={type}
                      value={form[key] || ""}
                      onChange={update}
                    />
                  )
                return type === "textarea" ? (
                  <TextareaField
                    key={key}
                    label={label}
                    value={form[key] || ""}
                    onChange={update}
                    placeholder="Adicione as informações"
                  />
                ) : (
                  <InputField
                    key={key}
                    label={label}
                    type={type || "text"}
                    min={type === "number" ? "0" : undefined}
                    step={type === "number" ? "any" : undefined}
                    value={form[key] || ""}
                    maxLength={editor === "Clientes" && apiClientPage ? { name: 150, phone: 20, instagram: 100, status: 45, folder: 255 }[key] : undefined}
                    onChange={update}
                    placeholder={label}
                  />
                )
              })}
          </div>
        </div>
        {editor === "Agenda" &&
          form.start &&
          form.end &&
          form.end < form.start && (
            <p className={cssClass("overnight-note")} role="status">
              O término às {form.end} será no dia seguinte.
            </p>
          )}
        {(() => {
          const editorError =
            editor === "Clientes"
              ? apiClients.error
              : editor === "Projetos"
                ? apiProjects.error
                : editor === "Agenda"
                  ? apiAgenda.error
                  : editor === "Orçamentos"
                    ? apiBudgets.error
                    : editor === "Financeiro"
                      ? apiFinance.error
                      : editor === "Acompanhamentos"
                        ? apiFollowUps.error
                        : editor === "Contratos"
                          ? apiContracts.error
                          : ""
          return error || editorError
            ? (
                <p className={cssClass("validation-error")} role="alert">
                  {error || editorError}
                </p>
              )
            : null
        })()}
      </Modal>
      <Modal
        className={cssClass("floating-glass")}
        isOpen={!!detail}
        title={detail?.name}
        onClose={() => setDetail(null)}
        size="large"
        footer={
          detail && (
            <>
              <Button
                variant="neutral"
                onClick={() => {
                  if (
                    detail.collection === "Orçamentos" &&
                    detail.source === "project"
                  ) {
                    const project = find("Projetos", detail.projectId)
                    if (project) {
                      open("Projetos", project)
                      setFormSection("Orçamento")
                    }
                  } else open(detail.collection, detail)
                }}
              >
                Editar dados
              </Button>
              {detail.collection === "Agenda" && (
                <Button
                  variant="neutral"
                  onClick={() => open("Agenda", detail, {})}
                >
                  Remarcar / cancelar
                </Button>
              )}
              {detail.projectId && (
                <Button
                  variant="neutral"
                  onClick={() => navigate("Projetos", detail.projectId)}
                >
                  Ver projeto
                </Button>
              )}
              {detail.clientId && (
                <Button
                  variant="neutral"
                  onClick={() => navigate("Clientes", detail.clientId)}
                >
                  Ver cliente
                </Button>
              )}
              {detail.collection === "Agenda" && detail.kind === "Sessão" && (
                <Button
                  onClick={() =>
                      open("Financeiro", undefined, {
                        projectId: detail.projectId,
                        clientId: detail.clientId,
                        sessionId: detail.sourceId || detail.id,
                        name: `Pagamento · ${detail.name}`,
                      })
                  }
                >
                  Registrar pagamento
                </Button>
              )}
              {detail.collection === "Financeiro" &&
                detail.status === "Pendente" && (
                  <Button
                    onClick={() =>
                      open("Financeiro", { ...detail, status: "Pago" })
                    }
                  >
                    Dar baixa
                  </Button>
                )}
              {detail.collection === "Contratos" && (
                <Button onClick={() => window.print()}>
                  Imprimir / salvar PDF
                </Button>
              )}
            </>
          )
        }
      >
        {detail && (
          <div
            className={cssClass(
              detail.collection === "Contratos" ? "contract-document" : "",
            )}
          >
            {detail.collection === "Contratos" && (
              <>
                <p>MINUTA · {detail.id}</p>
                <p>
                  Cliente: {find("Clientes", detail.clientId)?.name} · Projeto:{" "}
                  {find("Projetos", detail.projectId)?.name}
                </p>
                <p>
                  Valor:{" "}
                  {money(number(find("Projetos", detail.projectId)?.total))} ·
                  Sinal:{" "}
                  {money(number(find("Projetos", detail.projectId)?.deposit))}
                </p>
                <p>
                  Documento sem assinatura digital. Revise as condições antes da
                  formalização.
                </p>
              </>
            )}
            <dl className={cssClass("detail-grid")}>
              {(fields[detail.collection] || [])
                .filter(([key]) => detail[key] !== undefined && detail[key] !== "")
                .filter(
                  ([key]) =>
                    !(
                      detail.collection === "Agenda" &&
                      detail.kind !== "Sessão" &&
                      key === "location"
                    ),
                )
                .map(([key, label, type]) => (
                  <div key={key}>
                    <dt>{label}</dt>
                    <dd>
                      {type &&
                      typeof type === "string" &&
                      ["Clientes", "Projetos", "Agenda"].includes(type)
                        ? find(type, detail[key])?.name || "Não encontrado"
                        : key === "date" || key === "expires"
                          ? dateLabel(detail[key])
                          : [
                                "value",
                                "total",
                                "deposit",
                                "perSession",
                              ].includes(key)
                            ? money(number(detail[key]))
                            : key === "status" && detail.collection === "Agenda"
                              ? sessionStatus(detail)
                              : detail[key]}
                    </dd>
                  </div>
                ))}
            </dl>
          </div>
        )}
      </Modal>
    </div>
  )
}
export function LocalAccess({ onEnter }) {
  const { mode, name, setName, email, setEmail, password, setPassword, submitting, error, changeMode, submit } = useAccessViewModel(onEnter)
  return (
    <main className={cssClass("local-access")}>
      <section className={cssClass("local-access-card floating-glass")}>
        <img
          className={cssClass("local-access-logo")}
          src="/logo.png"
          alt="Kawz Tattoo"
        />
        <div className={cssClass("local-access-heading")}>
          <div role="heading" aria-level={1} className={cssClass("page-title")}>
            {mode === "login" ? "Bem-vindo de volta" : "Crie seu cadastro"}
          </div>
        </div>
        <form
          className={cssClass("local-access-form")}
          onSubmit={(event) => {
            event.preventDefault()
            submit()
          }}
        >
          {mode === "register" && (
            <InputField
              id="local-profile-name"
              label="Nome"
              value={name}
              onChange={setName}
              autoComplete="name"
              placeholder="Seu nome"
              required
            />
          )}
          <InputField
            id="local-profile-email"
            label="E-mail"
            type="email"
            value={email}
            onChange={setEmail}
            autoComplete="email"
            placeholder="seu@email.com"
            required
          />
          <InputField
            id="local-profile-password"
            label="Senha"
            type="password"
            value={password}
            onChange={setPassword}
            autoComplete={
              mode === "login" ? "current-password" : "new-password"
            }
            placeholder="Sua senha"
            required
            disabled={submitting}
          />
          {error && (
            <p role="alert" className={cssClass("validation-error")}>
              {error}
            </p>
          )}
          <Button type="submit" disabled={submitting}>
            {submitting
              ? "Aguarde…"
              : mode === "login"
                ? "Entrar"
                : "Cadastrar e entrar"}
          </Button>
          <Button
            type="button"
            variant="neutral"
            disabled={submitting}
            onClick={() => {
              changeMode()
            }}
          >
            {mode === "login" ? "Cadastrar" : "Já tenho cadastro"}
          </Button>
        </form>
      </section>
    </main>
  )
}
export default function App() {
  useScrollVisibility()
  const [profile, setProfile] = useState(null)
  const exit = () => setProfile(null)
  return (
    <>
      {profile ? (
        <Routes>
          <Route path="/" element={<Navigate to="/inicio" replace />} />
          {pages.map((page) => (
            <Route key={page} path={paths[page]} element={<Workspace onExit={exit} profile={profile} page={page} />} />
          ))}
          <Route path="/clientes/:id" element={<Workspace onExit={exit} profile={profile} page="Clientes" />} />
          <Route path="/projetos/:id" element={<Workspace onExit={exit} profile={profile} page="Projetos" />} />
          <Route path="*" element={<main className={cssClass("local-access")}><section className={cssClass("local-access-card floating-glass")}><h1>Página não encontrada</h1><Link to="/inicio">Voltar ao início</Link></section></main>} />
        </Routes>
      ) : apiMode ? <LocalAccess onEnter={setProfile} /> : (
        <main className={cssClass("local-access")}>
          <section className={cssClass("local-access-card floating-glass")} role="status">
            <h1>API não configurada</h1>
            <p>Configure VITE_API_BASE_URL no build para habilitar o login e os dados de Clientes.</p>
          </section>
        </main>
      )}
    </>
  )
}
