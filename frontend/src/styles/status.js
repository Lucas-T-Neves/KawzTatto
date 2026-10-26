export function statusTone(value = "") {
  const status = value.trim().toLocaleLowerCase("pt-BR")
  if (
    [
      "cliente",
      "pago",
      "confirmada",
      "realizada",
      "realizado",
      "finalizado",
      "aprovado",
      "assinado",
      "em andamento",
    ].includes(status)
  ) {
    return "status-positive"
  }
  if (["cancelado", "cancelada", "recusado", "rejeitado"].includes(status)) {
    return "status-negative"
  }
  if (
    [
      "interessado",
      "pendente",
      "a confirmar",
      "a iniciar",
      "enviado",
      "adiado",
      "remarcada",
      "aguardando assinatura",
    ].includes(status)
  ) {
    return "status-pending"
  }
  return "status-neutral"
}
