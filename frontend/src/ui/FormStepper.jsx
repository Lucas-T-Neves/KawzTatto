import { ArrowLeft, ArrowRight, Check } from "lucide-react"
import { Button } from "./index.jsx"
import styles from "./FormStepper.module.css"

export default function FormStepper({
  steps,
  currentIndex,
  onBack,
  onNext,
  onFinish,
  finishing = false,
  className = "",
  display = "all",
  editing = false,
  compactProgress = false,
}) {
  if (!steps?.length) return null
  const lastIndex = steps.length - 1
  const isLastStep = currentIndex >= lastIndex
  const progress = steps.length > 1 ? Math.min(1, currentIndex / lastIndex) : 0
  return (
    <div
      className={`${styles.root} ${
        compactProgress ? styles.compactProgress : ""
      } ${className}`}
    >
      {display !== "actions" && (
        <>
          <ol
            className={styles.progress}
            aria-label={editing ? "Etapas da edição" : "Etapas do cadastro"}
            style={{ "--wizard-progress": progress }}
          >
            {steps.map((step, index) => {
              const state =
                index < currentIndex
                  ? "complete"
                  : index === currentIndex
                    ? "active"
                    : "upcoming"
              return (
                <li
                  key={step.id}
                  className={styles.step}
                  data-state={state}
                  aria-current={state === "active" ? "step" : undefined}
                >
                  <span
                    className={styles.marker}
                    role="img"
                    aria-label={`Etapa ${index + 1}${
                      state === "complete" ? ", concluída" : ""
                    }`}
                  >
                    {state === "complete" ? (
                      <Check className={styles.check} aria-hidden="true" />
                    ) : (
                      index + 1
                    )}
                  </span>
                  <span className={styles.stepTitle}>{step.title}</span>
                </li>
              )
            })}
          </ol>
        </>
      )}
      {display !== "progress" && (
        <div className={styles.actions}>
          {currentIndex > 0 && (
            <Button
              variant="subtle"
              className={styles.back}
              iconStart={<ArrowLeft className={styles.arrow} />}
              disabled={finishing}
              onClick={onBack}
            >
              Voltar
            </Button>
          )}
          {!isLastStep ? (
            <Button
              className={styles.continue}
              iconEnd={<ArrowRight className={styles.arrow} />}
              disabled={finishing}
              onClick={onNext}
            >
              Continuar
            </Button>
          ) : (
            <Button
              className={styles.continue}
              onClick={onFinish}
              disabled={finishing}
              iconEnd={!finishing && <Check className={styles.arrow} />}
            >
              {finishing
                ? "Salvando…"
                : editing
                  ? "Salvar alterações"
                  : "Finalizar cadastro"}
            </Button>
          )}
        </div>
      )}
    </div>
  )
}
