import styles from "../App.module.css"

// Resolve local CSS Module classes while retaining global utility names.
// Already-resolved classes can pass through component props unchanged.
export function cssClass(value) {
  if (!value) return value
  return value
    .split(/\s+/)
    .filter(Boolean)
    .map((name) => styles[name] || name)
    .join(" ")
}

export function classSelector(name) {
  return `.${styles[name] || name}`
}
