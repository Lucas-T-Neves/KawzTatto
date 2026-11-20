import { strict as assert } from "node:assert"
import { test } from "node:test"
import { conversationGateway } from "./conversationGateway.js"

test("conversation gateway opens WhatsApp link when phone is present", async () => {
  let openedUrl = ""
  const result = await conversationGateway.openConversation(
    { phone: "(11) 90000-1000" },
    (url) => {
      openedUrl = url
      return {}
    },
  )
  assert.equal(openedUrl, "https://wa.me/11900001000")
  assert.equal(result.connected, true)
})

test("conversation gateway returns guidance when no contact is available", async () => {
  const result = await conversationGateway.openConversation({})
  assert.equal(result.connected, false)
  assert.match(result.message, /sem link de conversa/i)
})
