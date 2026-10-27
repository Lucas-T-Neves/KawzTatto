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

export function visibleFormFields(editor, form, section) {
  if (editor === "Clientes")
    return section === "Dados pessoais"
      ? clientPersonalFields
      : clientPreferenceFields
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
      ? [
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
      : ["name", "date", "start", "end", "notes"]
  if (editor === "Financeiro")
    return form.kind === "Despesa"
      ? ["projectId", "name", "value", "date", "notes"]
      : ["projectId", "type", "value", "date", "method", "status"]
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
