import { useEffect, useState } from "react"
import { Button, InputField, TextareaField } from "../../../ui/index.jsx"
import { cssClass } from "../../../styles/classes.js"
import { emptyAnamnese } from "../model/ApiAnamneseRepository.js"

const healthFields = [
  ["tratamentoMedico", "Tratamento médico"],
  ["cirurgiaRecente", "Cirurgia recente"],
  ["alergia", "Alergia"],
  ["gestante", "Gestante"],
  ["amamentando", "Amamentando"],
  ["marcapasso", "Marcapasso"],
  ["disturbioCirculatorio", "Distúrbio circulatório"],
  ["cancer", "Câncer"],
  ["usoDrogas", "Uso de drogas"],
  ["alimentou24h", "Alimentou-se nas últimas 24h"],
  ["queloide", "Queloide"],
  ["diabetes", "Diabetes"],
  ["convulsoes", "Convulsões"],
  ["infectocontagiosas", "Doença infectocontagiosa"],
  ["vitiligo", "Vitiligo"],
  ["hipertensao", "Hipertensão"],
  ["anemia", "Anemia"],
  ["cardiopatia", "Cardiopatia"],
  ["hemofilia", "Hemofilia"],
  ["problemaPele", "Problema de pele"],
  ["hepatite", "Hepatite"],
  ["dormiuUltimaNoite", "Dormiu na última noite"],
]

export default function AnamnesePanel({ cliente, model }) {
  const [form, setForm] = useState(emptyAnamnese(cliente?.id))

  useEffect(() => {
    if (model.anamnese) setForm(model.anamnese)
  }, [model.anamnese])

  const update = (key, value) => setForm((current) => ({ ...current, [key]: value }))
  const updateHealth = (key, value) => setForm((current) => ({
    ...current,
    saude: { ...current.saude, [key]: value },
  }))
  const updateProcedure = (key, value) => setForm((current) => ({
    ...current,
    procedimento: { ...current.procedimento, [key]: value },
  }))
  const save = async () => {
    await model.create(form)
  }
  const remove = async () => {
    if (window.confirm("Excluir este documento de anamnese? Esta ação não pode ser desfeita.")) {
      await model.remove()
    }
  }
  const generatePdf = async () => {
    await model.generatePdf()
  }
  const uploadSignedDocument = async (event) => {
    const file = event.target.files?.[0]
    event.target.value = ""
    if (!file) return
    if (file.type !== "application/pdf") return
    await model.uploadSignedDocument(file)
  }
  const removeSignedDocument = async () => {
    if (window.confirm("Remover o documento assinado armazenado?")) await model.removeSignedDocument()
  }

  if (model.listState === "loading") return <p role="status">Carregando anamnese…</p>
  if (model.listState === "error") return <p role="alert">{model.error}</p>

  return (
    <div className={cssClass("section-stack") }>
      <div className={cssClass("action-row")}>
        <strong>{form.id ? "Documento de anamnese" : "Anamnese ainda não preenchida"}</strong>
      </div>
      {model.error && <p role="alert">{model.error}</p>}
      {!form.id ? (
        <div className={cssClass("form-grid")}>
          <InputField label="Nome" value={form.nome} onChange={(value) => update("nome", value)} />
          <InputField label="Data de nascimento" type="date" value={form.dataNascimento} onChange={(value) => update("dataNascimento", value)} />
          <InputField label="Ocupação" value={form.ocupacao} onChange={(value) => update("ocupacao", value)} />
          <InputField label="Telefone" value={form.telefone} onChange={(value) => update("telefone", value)} />
          <InputField label="RG" value={form.rg} onChange={(value) => update("rg", value)} />
          <InputField label="CPF" value={form.cpf} onChange={(value) => update("cpf", value)} />
          <InputField label="Endereço" value={form.endereco} onChange={(value) => update("endereco", value)} />
          <InputField label="Data da assinatura" type="date" value={form.dataAssinatura} onChange={(value) => update("dataAssinatura", value)} />
          <label className={cssClass("checkbox-field")}><input type="checkbox" checked={Boolean(form.whatsapp)} onChange={(event) => update("whatsapp", event.target.checked)} /> WhatsApp</label>
          <div className={cssClass("form-grid-wide")}>
            <strong>Saúde</strong>
            <div className={cssClass("checkbox-grid")}>
              {healthFields.map(([key, label]) => <label key={key} className={cssClass("checkbox-field")}><input type="checkbox" checked={Boolean(form.saude?.[key])} onChange={(event) => updateHealth(key, event.target.checked)} /> {label}</label>)}
            </div>
          </div>
          <TextareaField label="Outro problema de saúde" value={form.saude?.outroProblema || ""} onChange={(value) => updateHealth("outroProblema", value)} />
          <InputField label="Local do procedimento" value={form.procedimento?.local} onChange={(value) => updateProcedure("local", value)} />
          <InputField label="Data do procedimento" type="date" value={form.procedimento?.data || ""} onChange={(value) => updateProcedure("data", value)} />
          <InputField label="Profissional" value={form.procedimento?.profissional} onChange={(value) => updateProcedure("profissional", value)} />
          <TextareaField label="Observações do procedimento" value={form.procedimento?.observacoes || ""} onChange={(value) => updateProcedure("observacoes", value)} />
          <div className={cssClass("action-row")}><Button disabled={model.saving} onClick={save}>Criar documento de anamnese</Button></div>
        </div>
      ) : (
        <>
          <dl className={cssClass("detail-grid")}>
            <div><dt>Nome</dt><dd>{form.nome || "Não informado"}</dd></div>
            <div><dt>CPF</dt><dd>{form.cpf || "Não informado"}</dd></div>
            <div><dt>Data de nascimento</dt><dd>{form.dataNascimento || "Não informado"}</dd></div>
            <div><dt>Ocupação</dt><dd>{form.ocupacao || "Não informado"}</dd></div>
            <div><dt>Procedimento</dt><dd>{form.procedimento?.local || "Não informado"}</dd></div>
            <div><dt>Assinatura</dt><dd>{form.dataAssinatura || "Não informado"}</dd></div>
          </dl>
          <div className={cssClass("action-row")}>
            <Button disabled={model.saving} onClick={generatePdf}>Gerar PDF</Button>
            {form.pdfUrl && <a href={form.pdfUrl} target="_blank" rel="noreferrer">Abrir PDF</a>}
            <label>Documento assinado (PDF)
              <input type="file" accept="application/pdf,.pdf" disabled={model.uploading} onChange={uploadSignedDocument} />
            </label>
            {form.documentoAssinadoUrl && <>
              <a href={form.documentoAssinadoUrl} target="_blank" rel="noreferrer">Abrir documento assinado{form.documentoAssinadoNome ? ` (${form.documentoAssinadoNome})` : ""}</a>
              <Button disabled={model.uploading} onClick={removeSignedDocument}>Remover documento assinado</Button>
            </>}
            <Button disabled={model.saving} onClick={remove}>Excluir documento</Button>
          </div>
        </>
      )}
    </div>
  )
}
