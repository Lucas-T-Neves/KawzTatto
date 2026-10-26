import { strict as assert } from "node:assert"
import { test } from "node:test"
import { conversationGateway } from "./conversationGateway.js"

test("conversation gateway keeps whatsapp integration as pending", async () => {
  const result = await conversationGateway.openConversation({
    clientId: "demo",
    phone: "00000000000",
  })
  assert.equal(result.connected, false)
  assert.match(result.message, /desenvolvimento/i)
})
