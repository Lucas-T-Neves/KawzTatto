import { strict as assert } from "node:assert"
import { test } from "node:test"
import {
  clientPersonalFields,
  clientPreferenceFields,
  getFormSteps,
  projectFormSections,
  recordNameFor,
  stepForSection,
  validateForm,
  validateFormStep,
  visibleFormFields,
} from "./formConfiguration.js"

test("client data keeps personal/contact fields and preference notes", () => {
  for (const key of ["name", "phone", "instagram", "channel", "folder"])
    assert.ok(clientPersonalFields.includes(key), key)
  for (const key of ["location", "body", "size", "chat", "idea", "budget", "availability", "status"])
    assert.ok(!clientPersonalFields.includes(key), key)
  assert.ok(!clientPersonalFields.includes("notes"))
  assert.ok(clientPreferenceFields.includes("notes"))
  assert.deepEqual(
    visibleFormFields("Clientes", {}, "Dados pessoais"),
    clientPersonalFields,
  )
  assert.deepEqual(
    visibleFormFields("Clientes", {}, "Preferências"),
    clientPreferenceFields,
  )
})

test("follow-ups have a date and generate a name without a description input", () => {
  const keys = visibleFormFields("Acompanhamentos", {}, "")
  assert.ok(keys.includes("date"))
  assert.ok(!keys.includes("name"))
  assert.equal(
    recordNameFor("Acompanhamentos", { type: "Pós-tatuagem" }, {
      clientName: "Ana",
    }),
    "Pós-tatuagem · Ana",
  )
  assert.equal(
    recordNameFor("Acompanhamentos", { id: "old", name: "Contato existente" }),
    "Contato existente",
  )
})

test("session forms omit activity choice and charged amount but keep scheduling and project", () => {
  const keys = visibleFormFields("Agenda", { kind: "Sessão" }, "")
  for (const key of ["projectId", "date", "start", "end"])
    assert.ok(keys.includes(key), key)
  assert.ok(!keys.includes("kind"))
  assert.ok(!keys.includes("value"))
  assert.ok(
    !visibleFormFields("Agenda", { kind: "Tarefa" }, "").includes("projectId"),
  )
})

test("project forms omit accepted proposal, difficulty, notes and payment method", () => {
  const all = Object.values(projectFormSections).flat()
  for (const key of ["accepted", "difficulty", "notes", "method"])
    assert.ok(!all.includes(key), key)
  for (const key of ["name", "clientId", "reference", "description", "total"])
    assert.ok(all.includes(key), key)
})

test("payment forms omit session selection without affecting payment naming", () => {
  const keys = visibleFormFields("Financeiro", { kind: "Receita" }, "")
  assert.ok(!keys.includes("sessionId"))
  assert.ok(keys.includes("projectId"))
  assert.equal(
    recordNameFor("Financeiro", { kind: "Receita", type: "Sinal" }, {
      projectName: "Lua",
    }),
    "Sinal · Lua",
  )
  assert.equal(
    recordNameFor("Financeiro", { kind: "Despesa", name: "Material" }),
    "Material",
  )
})

test("stepper configuration maps sections and keeps final action in last step", () => {
  const projectSteps = getFormSteps("Projetos", {})
  assert.equal(projectSteps.length, 4)
  assert.equal(projectSteps[0].id, "identificacao")
  assert.equal(stepForSection("Projetos", "Orçamento", {}), "orcamento")
  const expenseSteps = getFormSteps("Financeiro", { kind: "Despesa" })
  assert.deepEqual(expenseSteps.map((step) => step.id), [
    "contextoDespesa",
    "valorData",
    "observacoes",
  ])
})

test("step validation blocks required fields by current step and before finish", () => {
  const firstStepErrors = validateFormStep("Orçamentos", { status: "Rascunho" }, "vinculos")
  assert.equal(firstStepErrors.projectId, "Selecione o projeto do orçamento.")
  assert.equal(firstStepErrors.clientId, "Selecione o cliente do orçamento.")
  const finishErrors = validateForm("Financeiro", {
    kind: "Receita",
    projectId: "",
    value: "",
    date: "",
    status: "",
  })
  assert.equal(finishErrors.projectId, "Selecione o projeto da movimentação.")
  assert.equal(finishErrors.value, "Informe um valor válido.")
  assert.equal(finishErrors.date, "Informe a data da movimentação.")
  assert.equal(finishErrors.status, "Informe o status da movimentação.")
})
