import { cssClass } from "../../../styles/classes.js"
import { InputField } from "../../../ui/index.jsx"
import { ImagePlus } from "lucide-react"
export default function ProjectImageField({ value }) {
  return (
    <div className={cssClass("project-image-field")}>
      <InputField
        label="Imagem de referência"
        type="file"
        accept="image/jpeg,image/png,image/webp"
        disabled
        description="Upload de imagens ainda não integrado à API."
        prefix={<ImagePlus className={cssClass("control-icon")} />}
      />
      {value && (
        <figure className={cssClass("project-image-preview")}>
          <img src={value.url} alt={`Referência: ${value.name}`} />
          <figcaption>
            <span>{value.name}</span>
          </figcaption>
        </figure>
      )}
    </div>
  )
}
