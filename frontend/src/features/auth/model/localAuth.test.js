import { strict as assert } from "node:assert"
import { test, beforeEach, afterEach } from "node:test"
import {
  activeLocalProfile,
  activeProfileKey,
  demoProfile,
  localPasswordHash,
  localProfiles,
  profileKey,
} from "./localAuth.js"

let previousStorage
beforeEach(() => {
  previousStorage = globalThis.localStorage
  const memory = new Map()
  globalThis.localStorage = {
    getItem: (key) => memory.get(key) ?? null,
    setItem: (key, value) => memory.set(key, value),
  }
})
afterEach(() => {
  if (previousStorage === undefined) delete globalThis.localStorage
  else globalThis.localStorage = previousStorage
})

test("public demo credentials match the same password verifier as local login", async () => {
  assert.equal(
    await localPasswordHash("KawzDemo2026!", demoProfile.passwordSalt),
    demoProfile.passwordHash,
  )
  assert.notEqual(
    await localPasswordHash("incorrect", demoProfile.passwordSalt),
    demoProfile.passwordHash,
  )
})

test("demo profile is available without creating or overwriting local registrations", () => {
  const existing = {
    name: "Local user",
    email: "local@example.invalid",
    passwordHash: "keep",
    passwordSalt: "keep",
  }
  localStorage.setItem(profileKey, JSON.stringify([existing]))
  assert.deepEqual(localProfiles()[0], existing)
  assert.equal(
    localProfiles().filter((profile) => profile.email === demoProfile.email)
      .length,
    1,
  )
  assert.deepEqual(JSON.parse(localStorage.getItem(profileKey)), [existing])
})

test("demo login can be restored from the active email", () => {
  assert.equal(activeLocalProfile(), null)
  localStorage.setItem(activeProfileKey, demoProfile.email)
  assert.deepEqual(activeLocalProfile(), demoProfile)
})

test("existing registration with the demo email is preserved", () => {
  const existing = {
    ...demoProfile,
    name: "Existing name",
    passwordHash: "existing-hash",
  }
  localStorage.setItem(profileKey, JSON.stringify([existing]))
  assert.deepEqual(localProfiles(), [existing])
})
