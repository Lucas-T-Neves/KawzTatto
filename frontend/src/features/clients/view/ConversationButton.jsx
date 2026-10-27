import { Button } from "../../../ui/index.jsx"
import { MessageCircle } from "lucide-react"
import { cssClass } from "../../../styles/classes.js"
import useConversationViewModel from "../viewmodel/useConversationViewModel.js"

export default function ConversationButton({ client, onFeedback }) {
  const { busy, open } = useConversationViewModel(client, onFeedback)
  return (
    <Button
      size="small"
      variant="neutral"
      className={cssClass("client-conversation-button")}
      disabled={busy}
      title={busy ? "Abrindo conversa..." : "Abrir conversa"}
      iconStart={<MessageCircle className={cssClass("control-icon")} />}
      onClick={open}
    >
      {busy ? "Abrindo..." : "Abrir conversa"}
    </Button>
  )
}
