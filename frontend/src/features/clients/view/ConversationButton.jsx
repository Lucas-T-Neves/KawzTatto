import { Button } from "../../../ui/index.jsx"
import { MessageCircle } from "lucide-react"
import { cssClass } from "../../../styles/classes.js"

export default function ConversationButton() {
  return (
    <Button
      size="small"
      variant="neutral"
      className={cssClass("client-conversation-button")}
      disabled
      title="Conversa ainda não integrada à API"
      iconStart={<MessageCircle className={cssClass("control-icon")} />}
    >
      Conversa não integrada
    </Button>
  )
}
