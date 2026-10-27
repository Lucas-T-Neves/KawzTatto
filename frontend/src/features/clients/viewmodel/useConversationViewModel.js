import { useEffect, useRef, useState } from "react"
import { conversationGateway } from "../model/conversationGateway.js"

export default function useConversationViewModel(
  client,
  onFeedback,
  gateway = conversationGateway,
) {
  const [busy, setBusy] = useState(false)
  const active = useRef(true)
  useEffect(() => {
    active.current = true
    return () => {
      active.current = false
    }
  }, [])
  const open = async () => {
    if (busy) return
    if (!client) return
    setBusy(true)
    try {
      const result = await gateway.openConversation({
        clientId: client.id,
        phone: client.phone || "",
        conversationUrl: client.chat || "",
      })
      if (active.current) onFeedback(result.message)
    } catch {
      if (active.current)
        onFeedback("Não foi possível abrir a conversa. Tente novamente.")
    } finally {
      if (active.current) setBusy(false)
    }
  }
  return { busy, open }
}
