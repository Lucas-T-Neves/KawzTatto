import { strict as assert } from "node:assert"
import { test } from "node:test"
import { conversationGateway } from "./conversationGateway.js"

test("the mock conversation adapter does not claim a live API connection", async () => {
  const result = await conversationGateway.openConversation({
    clientId: "demo",
    phone: "00000000000",
  })
  assert.equal(result.connected, false)
  assert.match(result.message, /ainda não está conectada/)
})
