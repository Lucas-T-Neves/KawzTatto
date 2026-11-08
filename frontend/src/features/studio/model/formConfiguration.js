export const clientPersonalFields = [
  "name",
  "phone",
  "instagram",
  "channel",
  "folder",
]
export const clientPreferenceFields = [
  "music",
  "food",
  "style",
  "habits",
  "notes",
]

const agendaSessionFields = [
  "name",
  "projectId",
  "clientId",
  "date",
  "start",
  "end",
  "location",
  "status",
  "notes",
]

const agendaActivityFields = ["name", "date", "start", "end", "notes"]

const financeExpenseFields = ["projectId", "name", "value", "date", "notes"]

const financeIncomeFields = [
  "projectId",
  "type",
  "value",
  "date",
  "method",
  "status",
]

export const projectFormSections = {
  "Dados gerais": ["name", "clientId", "status", "folder", "reference"],
  Características: [
    "description",
    "style",
    "size",
    "body",
    "complexity",
    "colors",
  ],
  Orçamento: ["total", "deposit", "estimate"],
  Acompanhamento: ["pre", "post"],
}

const baseSteps = {
  Clientes: [
    { id: "dados-pessoais", title: "Dados pessoais", fields: clientPersonalFields },
    { id: "preferencias", title: "Preferências", fields: clientPreferenceFields },
  ],
  Projetos: [
    {
      id: "identificacao",
      title: "Identificação",
      fields: ["name", "clientId", "status"],
    },
    {
      id: "caracteristicas",
      title: "Características",
      fields: [
        "description",
        "style",
        "size",
        "body",
        "complexity",
        "colors",
        "reference",
        "folder",
      ],
    },
    {
      id: "orcamento",
      title: "Orçamento",
      fields: ["total", "deposit", "estimate"],
    },
    { id: "acompanhamento", title: "Acompanhamento", fields: ["pre", "post"] },
  ],
  Orçamentos: [
    {
      id: "vinculos",
      title: "Vínculos",
      fields: ["projectId", "clientId", "name", "status"],
    },
    {
      id: "valores",
      title: "Valores",
      fields: ["total", "deposit", "estimate", "perSession"],
    },
    {
      id: "condicoes",
      title: "Condições",
      fields: ["method", "date", "expires", "accepted"],
    },
  ],
  Acompanhamentos: [
    {
      id: "relacionamento",
      title: "Relacionamento",
      fields: ["clientId", "projectId", "sessionId"],
    },
    {
      id: "followup",
      title: "Follow-up",
      fields: ["type", "status", "date"],
    },
    { id: "resultado", title: "Resultado", fields: ["notes"] },
  ],
  Contratos: [
    { id: "vinculo", title: "Vínculo", fields: ["projectId", "name"] },
    {
      id: "regras",
      title: "Regras",
      fields: ["changes", "delay", "deadline"],
    },
    {
      id: "clausulas",
      title: "Cláusulas",
      fields: ["anamnesis", "exclusive", "image", "notes"],
    },
  ],
}

const sectionStepByEditor = {
  Clientes: {
    "Dados pessoais": "dados-pessoais",
    Preferências: "preferencias",
  },
  Projetos: {
    "Dados gerais": "identificacao",
    Características: "caracteristicas",
    Orçamento: "orcamento",
    Acompanhamento: "acompanhamento",
  },
}

const isRequired = (value) => String(value || "").trim()

const isNonNegativeNumber = (value) => {
  if (value === "" || value == null) return false
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed >= 0
}

const isPositiveInteger = (value) => {
  const parsed = Number(value)
  return Number.isInteger(parsed) && parsed > 0
}

const validationByEditor = {
  Clientes: {
    "dados-pessoais": (form) => ({
      ...(isRequired(form.name) ? {} : { name: "Informe o nome do cliente." }),
    }),
  },
  Projetos: {
    identificacao: (form) => ({
      ...(form.clientId ? {} : { clientId: "Selecione o cliente do projeto." }),
      ...(isRequired(form.status) ? {} : { status: "Informe o status do projeto." }),
    }),
    caracteristicas: (form) => ({
      ...(
        isRequired(form.name) || isRequired(form.description)
          ? {}
          : { description: "Informe a descrição do projeto." }
      ),
    }),
    orcamento: (form) => ({
      ...(isNonNegativeNumber(form.total)
        ? {}
        : { total: "Informe valor total válido." }),
      ...(isNonNegativeNumber(form.deposit)
        ? {}
        : { deposit: "Informe valor de sinal válido." }),
      ...(isPositiveInteger(form.estimate)
        ? {}
        : { estimate: "Informe sessões estimadas válidas." }),
    }),
  },
  Agenda: {
    vinculacao: (form) => ({
      ...(form.projectId ? {} : { projectId: "Vincule a sessão a um projeto." }),
      ...(form.clientId ? {} : { clientId: "Selecione o cliente da sessão." }),
      ...(isRequired(form.name) ? {} : { name: "Informe o nome da sessão." }),
    }),
    agendamento: (form) => ({
      ...(form.date ? {} : { date: "Informe a data da sessão." }),
      ...(form.start ? {} : { start: "Informe o início da sessão." }),
    }),
    basicos: (form) => ({
      ...(isRequired(form.name) ? {} : { name: "Informe o título da atividade." }),
      ...(isRequired(form.kind) ? {} : { kind: "Informe o tipo da atividade." }),
    }),
  },
  Orçamentos: {
    vinculos: (form) => ({
      ...(form.projectId ? {} : { projectId: "Selecione o projeto do orçamento." }),
      ...(form.clientId ? {} : { clientId: "Selecione o cliente do orçamento." }),
      ...(isRequired(form.status) ? {} : { status: "Informe o status do orçamento." }),
    }),
    valores: (form) => ({
      ...(isNonNegativeNumber(form.total)
        ? {}
        : { total: "Informe valor total válido." }),
      ...(isNonNegativeNumber(form.deposit)
        ? {}
        : { deposit: "Informe valor de sinal válido." }),
      ...(isPositiveInteger(form.estimate)
        ? {}
        : { estimate: "Informe sessões estimadas válidas." }),
    }),
  },
  Financeiro: {
    contextoReceita: (form) => ({
      ...(form.projectId ? {} : { projectId: "Selecione o projeto da movimentação." }),
      ...(isRequired(form.status)
        ? {}
        : { status: "Informe o status da movimentação." }),
    }),
    contextoDespesa: (form) => ({
      ...(form.projectId ? {} : { projectId: "Selecione o projeto da movimentação." }),
    }),
    valorData: (form) => ({
      ...(isNonNegativeNumber(form.value)
        ? {}
        : { value: "Informe um valor válido." }),
      ...(form.date ? {} : { date: "Informe a data da movimentação." }),
    }),
    pagamento: (form) => ({
      ...(isNonNegativeNumber(form.value)
        ? {}
        : { value: "Informe um valor válido." }),
      ...(form.date ? {} : { date: "Informe a data da movimentação." }),
    }),
  },
  Acompanhamentos: {
    relacionamento: (form) => ({
      ...(form.clientId ? {} : { clientId: "Selecione o cliente do acompanhamento." }),
    }),
    followup: (form) => ({
      ...(form.date ? {} : { date: "Informe a data prevista." }),
    }),
  },
  Contratos: {
    vinculo: (form) => ({
      ...(form.projectId ? {} : { projectId: "Selecione o projeto do contrato." }),
    }),
  },
}

export function getFormSteps(editor, form = {}) {
  if (editor === "Agenda")
    return form.kind === "Sessão"
      ? [
          { id: "vinculacao", title: "Vinculação", fields: ["projectId", "clientId", "name"] },
          {
            id: "agendamento",
            title: "Agendamento",
            fields: ["date", "start", "end", "location", "status"],
          },
          { id: "observacoes", title: "Observações", fields: ["notes"] },
        ]
      : [
          { id: "basicos", title: "Dados básicos", fields: ["kind", "name"] },
          { id: "agenda", title: "Agenda", fields: ["date", "start", "end"] },
          { id: "observacoes", title: "Observações", fields: ["notes"] },
        ]
  if (editor === "Financeiro")
    return form.kind === "Despesa"
      ? [
          { id: "contextoDespesa", title: "Contexto", fields: ["projectId", "name"] },
          { id: "valorData", title: "Valor e data", fields: ["value", "date"] },
          { id: "observacoes", title: "Observações", fields: ["notes"] },
        ]
      : [
          {
            id: "contextoReceita",
            title: "Contexto",
            fields: ["projectId", "type", "status"],
          },
          {
            id: "pagamento",
            title: "Pagamento",
            fields: ["value", "date", "method"],
          },
          { id: "observacoes", title: "Observações", fields: ["notes"] },
        ]
  return baseSteps[editor] || []
}

export function fieldsForStep(editor, form, stepId) {
  return (
    getFormSteps(editor, form).find((step) => step.id === stepId)?.fields || []
  )
}

export function stepForSection(editor, section, form) {
  const mapped = sectionStepByEditor[editor]?.[section]
  if (mapped) return mapped
  return getFormSteps(editor, form)[0]?.id || ""
}

export function validateFormStep(editor, form, stepId) {
  const run = validationByEditor[editor]?.[stepId]
  return run ? run(form) : {}
}

export function validateForm(editor, form) {
  return getFormSteps(editor, form).reduce(
    (errors, step) => ({ ...errors, ...validateFormStep(editor, form, step.id) }),
    {},
  )
}

export function visibleFormFields(editor, form, section) {
  if (editor === "Clientes")
    return section === "Dados pessoais"
      ? clientPersonalFields
      : section === "Preferências"
        ? clientPreferenceFields
        : fieldsForStep(editor, form, section)
  if (editor === "Projetos") return projectFormSections[section] || []
  if (editor === "Acompanhamentos")
    return [
      "clientId",
      "projectId",
      "sessionId",
      "type",
      "date",
      "status",
      "notes",
    ]
  if (editor === "Agenda")
    return form.kind === "Sessão"
      ? agendaSessionFields
      : agendaActivityFields
  if (editor === "Financeiro")
    return form.kind === "Despesa"
      ? financeExpenseFields
      : financeIncomeFields
  if (editor === "Orçamentos")
    return [
      "name",
      "date",
      "projectId",
      "clientId",
      "status",
      "total",
      "deposit",
      "estimate",
      "method",
      "expires",
      "accepted",
    ]
  return null
}

export function recordNameFor(editor, form, { projectName, clientName } = {}) {
  if (editor === "Financeiro" && form.kind !== "Despesa") {
    return `${form.type || "Pagamento"} · ${projectName || "Projeto"}`
  }
  if (editor === "Acompanhamentos") {
    return form.id && form.name
      ? form.name
      : `${form.type || "Acompanhamento"} · ${clientName || "Cliente"}`
  }
  return form.name || ""
}
