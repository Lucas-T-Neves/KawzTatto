import { cssClass } from "../styles/classes.js"
import { statusTone } from "../styles/status.js"
import styles from "./AppointmentBadges.module.css"

export default function AppointmentBadges({ kind, status }) {
  const type = kind === "Tarefa" || kind === "Evento" ? kind : ""
  if (!type && !status) return null
  return (
    <span className={styles.badges}>
      {type && (
        <span
          className={`${cssClass("tag")} ${
            type === "Tarefa" ? styles.task : styles.event
          }`}
          data-appointment-kind={type}
        >
          {type}
        </span>
      )}
      {status && (
        <span className={cssClass(`tag ${statusTone(status)}`)}>{status}</span>
      )}
    </span>
  )
}
