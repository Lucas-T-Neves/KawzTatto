export const profileKey = "kawz-local-profiles"
export const activeProfileKey = "kawz-local-active-profile"

export function saveLocalProfile(profile, profiles) {
  localStorage.setItem(profileKey, JSON.stringify([
    ...profiles.filter((item) => item.email !== profile.email), profile,
  ]))
}

export function setActiveLocalProfile(email) {
  localStorage.setItem(activeProfileKey, email)
}

export function clearActiveLocalProfile() {
  localStorage.removeItem(activeProfileKey)
}

// Public demo fixture, not a production account or an authentication boundary.
export const demoProfile = Object.freeze({
  name: "Demo Kawz",
  email: "demo@kawztattoo.test",
  passwordSalt: "kawz-demo-only-v1",
  passwordHash:
    "3c6946685ca52d494e95f0d1a8960aacd7f0ac38e40e2cd5955ba298b51f974f",
})

export function localProfiles() {
  let profiles = []
  try {
    const saved = JSON.parse(localStorage.getItem(profileKey) || "[]")
    if (Array.isArray(saved)) {
      profiles = saved.filter(
        (profile) =>
          profile &&
          typeof profile.name === "string" &&
          typeof profile.email === "string",
      )
    }
  } catch {
    // The public fixture remains available when no local registrations exist.
  }
  return profiles.some((profile) => profile.email === demoProfile.email)
    ? profiles
    : [...profiles, { ...demoProfile }]
}

export function activeLocalProfile() {
  try {
    const email = localStorage.getItem(activeProfileKey)
    return (
      localProfiles().find(
        (profile) =>
          profile.email === email &&
          profile.passwordHash &&
          profile.passwordSalt,
      ) || null
    )
  } catch {
    return null
  }
}

export async function localPasswordHash(password, salt) {
  const encoder = new TextEncoder()
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(password),
    "PBKDF2",
    false,
    ["deriveBits"],
  )
  const bits = await crypto.subtle.deriveBits(
    {
      name: "PBKDF2",
      salt: encoder.encode(salt),
      iterations: 100000,
      hash: "SHA-256",
    },
    key,
    256,
  )
  return Array.from(new Uint8Array(bits), (byte) =>
    byte.toString(16).padStart(2, "0"),
  ).join("")
}
