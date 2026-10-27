import { activitiesOverlap } from "../../../schedule.js"
import { recordNameFor } from "./formConfiguration.js"

const number = (value) => Number(value || 0)
const today = () => new Date().toLocaleDateString("en-CA")

export function prepareStudioEntry(editor, form, { find, list, hasDeposit }) {
  const recordName = recordNameFor(editor, form, {
    projectName: find("Projetos", form.projectId)?.name,
    clientName: find("Clientes", form.clientId)?.name,
  })
  const invalid = (message) => { throw new Error(message) }
  if (!recordName.trim()) invalid("Preencha o nome ou a descrição.")
  if (editor === "Clientes" && (!form.phone?.trim() || !form.channel))
    invalid("Informe telefone e canal de aquisição.")
  if (["Projetos", "Orçamentos"].includes(editor) &&
      (!form.total || !form.deposit || !form.estimate || number(form.estimate) < 1 || !Number.isInteger(number(form.estimate))))
    invalid("Informe valor total, sinal e uma quantidade inteira de sessões maior que zero.")
  if (["Projetos", "Acompanhamentos"].includes(editor) && !form.clientId)
    invalid("Selecione um cliente.")
  if (["Financeiro", "Contratos", "Orçamentos"].includes(editor) && !form.projectId)
    invalid("Selecione o projeto correspondente.")
  if (editor === "Orçamentos" && !form.date) invalid("Informe a data do orçamento.")
  if (editor === "Clientes" && form.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
    invalid("Informe um e-mail válido.")
  if (["Projetos", "Orçamentos", "Financeiro"].includes(editor)) {
    for (const key of (["Projetos", "Orçamentos"].includes(editor) ? ["total", "deposit", "estimate"] : ["value"])) {
      if (form[key] && (!Number.isFinite(Number(form[key])) || number(form[key]) < 0))
        invalid("Informe valores numéricos não negativos.")
    }
  }
  if (editor === "Financeiro" && (!form.date || number(form.value) <= 0))
    invalid("Informe a data e um valor maior que zero.")
  if (editor === "Agenda") {
    if (!form.date || !form.start || !form.end || form.end === form.start)
      invalid("Informe a data e horários válidos. Início e término devem ser diferentes.")
    if (form.kind === "Sessão" && !form.projectId) invalid("Vincule a sessão a um projeto.")
    if (form.status !== "Cancelada" && list("Agenda").some((item) =>
      item.id !== form.id && item.status !== "Cancelada" &&
      activitiesOverlap({ date: item.date, start: item.start, end: item.end }, { date: form.date, start: form.start, end: form.end })))
      invalid("Conflito de horários: já existe uma atividade neste intervalo.")
    if (form.kind === "Sessão" && form.status === "Confirmada" && !hasDeposit(form.projectId))
      invalid("Registre o pagamento do sinal antes de confirmar a sessão.")
  }
  if (editor === "Acompanhamentos" && !form.date) invalid("Informe a data prevista.")
  const project = find("Projetos", form.projectId)
  if (editor === "Acompanhamentos" && form.sessionId && find("Agenda", form.sessionId)?.projectId !== form.projectId)
    invalid("A sessão selecionada não pertence ao projeto.")
  if (form.projectId && form.clientId && project?.clientId !== form.clientId)
    invalid("O projeto selecionado não pertence ao cliente.")
  const entry = {
    ...form, name: recordName,
    ...(editor === "Financeiro" && form.kind === "Despesa" ? { status: "Pago" } : {}),
    id: form.id || crypto.randomUUID(),
    created: form.created || new Date().toISOString(),
    updated: new Date().toISOString(),
    ...(project ? { clientId: project.clientId } : {}),
    ...(editor === "Acompanhamentos" && form.status === "Realizado" ? { completed: new Date().toISOString() } : {}),
  }
  if (editor === "Projetos") entry.expires = new Date(
    new Date(`${entry.created.slice(0, 10)}T12:00:00`).getTime() + 30 * 86400000,
  ).toLocaleDateString("en-CA")
  if (editor === "Contratos") entry.status = "Aguardando assinatura"
  return entry
}
