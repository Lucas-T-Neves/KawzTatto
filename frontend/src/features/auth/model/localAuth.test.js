import { strict as assert } from "node:assert"
import { test, beforeEach, afterEach } from "node:test"
import {
  activeLocalProfile,
  activeProfileKey,
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

test("local profiles return saved registrations only", () => {
  const existing = {
    name: "Local user",
    email: "local@example.invalid",
    passwordHash: "keep",
    passwordSalt: "keep",
  }
  localStorage.setItem(profileKey, JSON.stringify([existing]))
  assert.deepEqual(localProfiles(), [existing])
  assert.deepEqual(JSON.parse(localStorage.getItem(profileKey)), [existing])
})

test("active profile resolves only when saved profile exists", () => {
  const existing = {
    name: "Local user",
    email: "local@example.invalid",
    passwordHash: "keep",
    passwordSalt: "keep",
  }
  assert.equal(activeLocalProfile(), null)
  localStorage.setItem(profileKey, JSON.stringify([existing]))
  localStorage.setItem(activeProfileKey, existing.email)
  assert.deepEqual(activeLocalProfile(), existing)
})

test("malformed storage falls back to empty profiles", () => {
  localStorage.setItem(profileKey, "not-json")
  assert.deepEqual(localProfiles(), [])
  assert.equal(activeLocalProfile(), null)
})
