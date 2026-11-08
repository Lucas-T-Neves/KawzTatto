import { ChevronLeft, ChevronRight } from "lucide-react"
import { Button } from "./index.jsx"

export default function FormStepper({
  steps,
  currentIndex,
  onBack,
  onNext,
  onFinish,
  finishing = false,
  className = "",
}) {
  if (!steps?.length) return null
  const lastIndex = steps.length - 1
  const isLastStep = currentIndex >= lastIndex
  return (
    <div className={className}>
      <div>
        Etapa {currentIndex + 1} de {steps.length}
      </div>
      <div>
        {steps.map((step, index) => (
          <span key={step.id} data-active={index === currentIndex}>
            {index + 1}. {step.title}
          </span>
        ))}
      </div>
      <div>
        {currentIndex > 0 && (
          <Button variant="neutral" iconStart={<ChevronLeft size={16} />} onClick={onBack}>
            Voltar
          </Button>
        )}
        {!isLastStep ? (
          <Button iconEnd={<ChevronRight size={16} />} onClick={onNext}>
            Avançar
          </Button>
        ) : (
          <Button onClick={onFinish} disabled={finishing}>
            {finishing ? "Finalizando..." : "Finalizar cadastro"}
          </Button>
        )}
      </div>
    </div>
  )
}
