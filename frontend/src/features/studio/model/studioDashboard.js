const number = (value) => Number(value || 0)
const today = () => new Date().toLocaleDateString("en-CA")

export function studioDashboard(data) {
  const list = (key) => data[key] || []
  const find = (key, id) => list(key).find((entry) => entry.id === id)
  const hasDeposit = (projectId) => list("Financeiro").some((item) =>
    item.projectId === projectId && item.kind !== "Despesa" &&
    item.type === "Sinal" && item.status === "Pago" && number(item.value) > 0)
  const clientStatus = (id) => list("Projetos").some((project) =>
    project.clientId === id && hasDeposit(project.id)) ? "Cliente" : "Interessado"
  const sessionStatus = (item) => item.kind === "Sessão" &&
    item.status === "A confirmar" && hasDeposit(item.projectId) ? "Confirmada" : item.status
  const status = (item) => {
    if (item.status === "Cancelado") return item.status
    const sessions = list("Agenda").filter((session) =>
      session.projectId === item.id && session.kind === "Sessão" && session.status !== "Cancelada")
    if (sessions.length && sessions.every((session) => session.status === "Realizada")) return "Finalizado"
    if (sessions.some((session) => session.status === "Realizada")) return "Em andamento"
    return item.accepted === "Sim" && hasDeposit(item.id) && item.status === "Rascunho" ? "A iniciar" : item.status
  }
  const alerts = list("Acompanhamentos")
    .filter((item) => item.status === "Pendente")
    .map((item) => ({ ...item, collection: "Acompanhamentos" }))
  list("Agenda").filter((session) => session.kind === "Sessão" && session.status !== "Cancelada")
    .forEach((session) => {
      const project = find("Projetos", session.projectId)
      const gap = Math.round((new Date(`${session.date}T12:00:00`).getTime() - new Date(`${today()}T12:00:00`).getTime()) / 86400000)
      if (project?.pre !== "Não" && sessionStatus(session) === "Confirmada" && gap >= 0 && gap <= 7)
        alerts.push({ ...session, id: `pre-${session.id}`, name: `Preparação: ${session.name}`, type: "Pré-tatuagem", collection: "Agenda", sourceId: session.id, notes: "Comer bem, hidratar-se, descansar e cuidar da região antes da sessão." })
      if (project?.post !== "Não" && session.status === "Realizada" && gap <= -15 && gap >= -30)
        alerts.push({ ...session, id: `post-${session.id}`, name: `Cicatrização: ${session.name}`, type: "Pós-tatuagem", collection: "Agenda", sourceId: session.id, notes: "Verificar cicatrização, dúvidas e necessidade de retoque." })
    })
  list("Projetos").filter((project) =>
    status(project) === "Rascunho" && project.expires && project.expires >= today() &&
    project.expires <= new Date(Date.now() + 7 * 86400000).toLocaleDateString("en-CA"))
    .forEach((project) => alerts.push({ ...project, name: `Orçamento próximo do vencimento: ${project.name}`, type: "Orçamento", collection: "Projetos" }))
  return { hasDeposit, clientStatus, sessionStatus, status, alerts }
}
