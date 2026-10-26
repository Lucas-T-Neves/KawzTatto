import { strict as assert } from "node:assert"
import { test, beforeEach, afterEach } from "node:test"
import { createElement } from "react"
import { renderToStaticMarkup } from "react-dom/server"
import { MemoryRouter, Route, Routes } from "react-router-dom"
import {
  Button,
  IconButton,
  InputField,
  TextareaField,
  SelectField,
  Modal,
  SidebarNavigation,
} from "./index.jsx"
import App, { LocalAccess, Workspace } from "../App.jsx"

const render = (component, props, ...children) =>
  renderToStaticMarkup(createElement(component, props, ...children))
let previousWindow
let previousStorage
beforeEach(() => {
  previousWindow = globalThis.window
  previousStorage = globalThis.localStorage
  const memory = new Map()
  const storage = {
    getItem: (key) => memory.get(key) ?? null,
    setItem: (key, value) => {
      memory.set(key, value)
    },
    removeItem: (key) => {
      memory.delete(key)
    },
  }
  globalThis.localStorage = storage
  globalThis.window = { localStorage: storage }
})
afterEach(() => {
  if (previousWindow === undefined) delete globalThis.window
  else globalThis.window = previousWindow
  if (previousStorage === undefined) delete globalThis.localStorage
  else globalThis.localStorage = previousStorage
})

test("local buttons preserve events, content wrappers and explicit form behavior", () => {
  const onClick = () => {}
  const button = Button({ children: "Salvar", onClick })
  assert.equal(button.type, "button")
  assert.equal(button.props.type, "button")
  assert.equal(button.props.onClick, onClick)
  const html = render(Button, { type: "submit", variant: "neutral" }, "Entrar")
  assert.match(html, /type="submit"/)
  assert.match(html, /data-variant="neutral"/)
  assert.match(html, />Entrar<\/span>/)
})

test("icon buttons retain accessible labels and disabled state", () => {
  const html = render(IconButton, {
    "aria-label": "Adicionar",
    disabled: true,
    icon: createElement("span", null, "+"),
  })
  assert.match(html, /aria-label="Adicionar"/)
  assert.match(html, /disabled=""/)
})

test("text inputs and textareas associate labels and descriptions", () => {
  const input = render(InputField, {
    id: "client-name",
    label: "Nome",
    value: "Ana",
    description: "Nome do cliente",
    onChange() {},
  })
  assert.match(input, /for="client-name"/)
  assert.match(input, /value="Ana"/)
  assert.match(input, /aria-describedby="client-name-description"/)
  const textarea = render(TextareaField, {
    id: "notes",
    label: "Observações",
    value: "Preferências",
    onChange() {},
  })
  assert.match(textarea, /for="notes"/)
  assert.match(textarea, />Preferências<\/textarea>/)
})

test("file inputs are uncontrolled even when a value is supplied", () => {
  const html = render(InputField, {
    type: "file",
    label: "Referência",
    value: "must-not-be-written",
    accept: "image/png",
  })
  assert.match(html, /type="file"/)
  assert.doesNotMatch(html, /value=/)
})

test("local selectors expose a labeled combobox and native popover options", () => {
  const html = render(SelectField, {
    label: "Status",
    value: "active",
    onChange() {},
    options: [
      { value: "active", label: "Em andamento" },
      { value: "done", label: "Finalizado" },
    ],
  })
  assert.match(html, /role="combobox"/)
  assert.match(html, /aria-expanded="false"/)
  assert.match(html, /popover="auto"/)
  assert.match(html, /role="listbox"/)
  assert.equal((html.match(/role="option"/g) || []).length, 2)
  assert.match(html, /aria-selected="true"/)
})

test("modal and sidebar render native accessible boundaries", () => {
  const html = render(
    Modal,
    {
      isOpen: true,
      title: "Cadastrar cliente",
      onClose() {},
      footer: createElement(Button, null, "Salvar"),
    },
    "Dados pessoais",
  )
  assert.match(html, /<dialog/)
  assert.match(html, /aria-labelledby=/)
  assert.match(html, /Cadastrar cliente/)
  assert.match(html, /aria-label="Fechar janela"/)
  assert.match(html, /Dados pessoais/)
  const closed = render(Modal, { isOpen: false, onClose() {} }, "Não exibir")
  assert.doesNotMatch(closed, /Não exibir/)
  const nav = render(SidebarNavigation, { footer: "Sair" }, "Clientes")
  assert.match(nav, /<nav aria-label="Navegação principal"/)
  assert.match(nav, /Clientes/)
  assert.match(nav, /Sair/)
})

test("API login renders with local controls", () => {
  const html = render(LocalAccess, { onEnter() {} })
  assert.match(html, /Bem-vindo de volta/)
  assert.match(html, /type="email"/)
  assert.match(html, /type="password"/)
  assert.match(html, /type="submit"/)
})

test("authenticated home keeps navigation without claiming simulated records", () => {
  const html = render(MemoryRouter, { initialEntries: ["/inicio"] }, createElement(Workspace, {
    profile: { name: "Tatuador", token: "token-test" }, page: "Início", onExit() {},
  }))
  assert.match(html, /Olá, Tatuador!/)
  assert.match(html, /Navegação principal/)
  assert.match(html, /Próximos compromissos/)
  assert.match(html, /Compromissos ainda não integrados/)
  assert.doesNotMatch(html, /Lua botânica|Tudo em ordem por aqui/)
  assert.match(html, /Sair/)
  assert.match(html, /href="\/clientes"/)
  assert.match(html, /href="\/agenda"/)
})

test("direct URLs determine the rendered page and unknown URLs show recovery link", () => {
  const profile = { name: "Tatuador", token: "token-test" }
  const agenda = render(MemoryRouter, { initialEntries: ["/agenda"] }, createElement(Workspace, { profile, page: "Agenda", onExit() {} }))
  assert.match(agenda, /page-agenda/)
  assert.match(agenda, /agenda ainda não está integrada/)
  const missing = render(MemoryRouter, { initialEntries: ["/nao-existe"] }, createElement(App))
  assert.match(missing, /API não configurada|Bem-vindo de volta/)
})

test("projects and finance screens show empty states when data is unavailable", () => {
  const profile = { name: "Tatuador", token: "token-test" }
  const projects = render(
    MemoryRouter,
    { initialEntries: ["/projetos"] },
    createElement(Workspace, { profile, page: "Projetos", onExit() {} }),
  )
  assert.match(projects, /Projetos/)
  const finance = render(
    MemoryRouter,
    { initialEntries: ["/financeiro"] },
    createElement(Workspace, { profile, page: "Financeiro", onExit() {} }),
  )
  assert.match(finance, /Nenhuma movimentação paga neste mês/)
  assert.match(finance, /Nenhum pagamento pendente neste mês/)
})

test("client detail keeps all panels visible without API detail data", () => {
  const profile = { name: "Tatuador", token: "token-test" }
  const html = render(
    MemoryRouter,
    { initialEntries: ["/clientes/cliente-sem-api"] },
    createElement(
      Routes,
      null,
      createElement(Route, {
        path: "/clientes/:id",
        element: createElement(Workspace, { profile, page: "Clientes", onExit() {} }),
      }),
    ),
  )
  assert.match(html, /Dados do cliente/)
  assert.match(html, /Próximas sessões/)
  assert.match(html, /Projetos/)
  assert.match(html, /Acompanhamentos/)
  assert.match(html, /Pagamentos/)
  assert.match(html, /Nenhum acompanhamento cadastrado para este cliente\./)
  assert.match(html, /Use o botão de adicionar deste bloco para iniciar\./)
  assert.match(html, /Editar dados/)
  assert.match(html, /Excluir cliente/)
})

test("missing API configuration never restores a demonstrative login", () => {
  const missing = render(MemoryRouter, { initialEntries: ["/nao-existe"] }, createElement(App))
  assert.doesNotMatch(missing, /Demo Kawz|demo@kawztattoo/)
  if (missing.includes("API não configurada")) assert.match(missing, /VITE_API_BASE_URL/)
})
