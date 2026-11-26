function whatsappPhone(phone) {
  const digits = String(phone || "").replace(/\D/g, "")
  if (!digits) throw new Error("Este cliente não possui telefone cadastrado.")
  return digits.startsWith("55") ? digits : `55${digits}`
}

export function whatsappUrl(phone) {
  return `https://wa.me/${whatsappPhone(phone)}`
}

export const conversationGateway = {
  async openConversation({ phone }) {
    const url = whatsappUrl(phone)
    if (typeof globalThis.open === "function") {
      globalThis.open(url, "_blank", "noopener,noreferrer")
    }
    return {
      connected: true,
      url,
      message: "WhatsApp aberto em uma nova aba.",
    }
  },
}
