// Frontend-only adapter. Replace this object when the external API is available.
// It intentionally does not navigate to the stored link or send customer data.
export const conversationGateway = {
  async openConversation() {
    return {
      connected: false,
      message:
        "A integração do WhatsApp ainda não está conectada. O botão está preparado para a futura API.",
    }
  },
}
