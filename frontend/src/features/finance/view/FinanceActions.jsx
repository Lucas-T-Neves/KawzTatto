import { Button, IconButton } from "../../../ui/index.jsx"
import { ArrowDownLeft, ArrowUpRight, Plus } from "lucide-react"
import useFinancePopover from "../viewmodel/useFinancePopover.js"
import styles from "./Finance.module.css"

export default function FinanceActions({ onCreate, disabled = false }) {
  const { id, root, open, toggle, close, popupProps } = useFinancePopover()
  const choose = (kind) => {
    close()
    onCreate(kind)
  }
  return (
    <div ref={root}>
      <IconButton
        className={styles.addButton}
        aria-label="Adicionar registro financeiro"
        title="Adicionar registro financeiro"
        disabled={disabled}
        aria-expanded={open}
        aria-controls={id}
        aria-haspopup="dialog"
        icon={<Plus className={styles.icon} />}
        onClick={toggle}
      />
      {!disabled && <div
        {...popupProps}
        role="dialog"
        aria-label="Novo registro financeiro"
        className={styles.popover}
      >
        <strong className={styles.popoverTitle}>Adicionar registro</strong>
        <Button
          size="small"
          variant="neutral"
          iconStart={<ArrowDownLeft className={styles.icon} />}
          onClick={() => choose("Receita")}
        >
          Registrar pagamento
        </Button>
        <Button
          size="small"
          variant="neutral"
          iconStart={<ArrowUpRight className={styles.icon} />}
          onClick={() => choose("Despesa")}
        >
          Registrar gasto
        </Button>
      </div>}
    </div>
  )
}
