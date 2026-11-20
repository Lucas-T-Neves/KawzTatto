const normalizeConversationUrl = (value) => {
  const input = String(value || "").trim()
  if (!input) return ""
  if (/^https?:\/\//i.test(input)) return input
  return `https://${input}`
}

const normalizePhone = (value) => String(value || "").replace(/\D/g, "")

const defaultOpenWindow = (url) => {
  if (typeof window === "undefined" || typeof window.open !== "function") {
    throw new Error("Window indisponível")
  }
  return window.open(url, "_blank", "noopener,noreferrer")
}

export const conversationGateway = {
  async openConversation({ phone, conversationUrl }, openWindow = defaultOpenWindow) {
    const directUrl = normalizeConversationUrl(conversationUrl)
    const phoneDigits = normalizePhone(phone)
    const target = directUrl || (phoneDigits ? `https://wa.me/${phoneDigits}` : "")
    if (!target) {
      return {
        connected: false,
        message: "Cliente sem link de conversa ou telefone cadastrado.",
      }
    }
    openWindow(target)
    return {
      connected: true,
      message: "Conversa aberta em nova aba.",
    }
  },
}
