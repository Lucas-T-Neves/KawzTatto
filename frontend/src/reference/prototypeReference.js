// Reference-only prototype contracts. Do not import this file in runtime UI flows.
export const prototypeCollections = {
  Clientes: [
    "id", "name", "phone", "instagram", "channel", "chat", "folder", "status",
    "location", "size", "idea", "music", "food", "style", "budget",
    "habits", "availability", "notes",
  ],
  Projetos: [
    "id", "name", "clientId", "status", "folder", "reference", "referenceId",
    "referenceName", "description", "style", "size", "body", "complexity", "colors",
    "total", "deposit", "estimate", "pre", "post",
  ],
  Agenda: [
    "id", "name", "projectId", "clientId", "kind", "date", "start", "end",
    "location", "status", "notes",
  ],
  Orçamentos: [
    "id", "name", "date", "projectId", "clientId", "status", "total", "deposit",
    "estimate", "perSession", "method", "expires", "accepted",
  ],
  Financeiro: [
    "id", "name", "projectId", "sessionId", "kind", "type", "value", "date",
    "method", "status", "notes",
  ],
  Acompanhamentos: [
    "id", "clientId", "projectId", "sessionId", "type", "date", "status", "notes",
  ],
  Contratos: [
    "id", "name", "projectId", "clientId", "status", "changes", "delay", "deadline",
    "anamnesis", "exclusive", "image", "notes",
  ],
}

export const prototypeStatuses = {
  orcamentos: ["Rascunho", "Enviado", "Aprovado", "Recusado"],
  agenda: ["A confirmar", "Confirmada", "Realizada", "Remarcada", "Cancelada"],
  financeiro: ["Pago", "Pendente", "Cancelado"],
}
