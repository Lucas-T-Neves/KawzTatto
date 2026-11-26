import { cssClass } from "../../../styles/classes.js"
import { InputField } from "../../../ui/index.jsx"
import { ImagePlus } from "lucide-react"
import useProjectImageViewModel from "../viewmodel/useProjectImageViewModel.js"

export default function ProjectImageField({ value, onChange, repository, canUpload = true }) {
  const { attach, remove, busy, error } = useProjectImageViewModel(onChange, repository)
  return (
    <div className={cssClass("project-image-field")}>
      <InputField
        label="Imagem de referência"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        disabled={!canUpload || busy}
        onChange={(event) => attach(event.target.files?.[0])}
        description={busy ? "Enviando imagem…" : canUpload ? "JPG, PNG ou WebP, até 10 MB." : "Salve o projeto antes de enviar uma imagem."}
        prefix={<ImagePlus className={cssClass("control-icon")} />}
      />
      {error && <p className={cssClass("field-error")} role="alert">{error}</p>}
      {value && (
        <figure className={cssClass("project-image-preview")}>
          <img src={value.url} alt={`Referência: ${value.name}`} />
          <figcaption>
            <span>{value.name}</span>
            <button type="button" onClick={remove} disabled={busy}>Remover</button>
          </figcaption>
        </figure>
      )}
    </div>
  )
}
