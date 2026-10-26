import { initializeDemoData } from "./demoData.js"
export default class LocalStudioRepository {
  storage
  constructor(storage) {
    this.storage = storage
  }
  load() {
    let data = {}
    try {
      const stored = JSON.parse(this.storage.getItem("kawz-data") || "{}")
      if (stored && typeof stored === "object" && !Array.isArray(stored)) {
        data = Object.fromEntries(
          Object.entries(stored)
            .filter(([, entries]) => Array.isArray(entries))
            .map(([key, entries]) => [
              key,
              entries
                .filter(
                  (item) =>
                    item &&
                    typeof item.id === "string" &&
                    typeof item.name === "string",
                )
                .map((item) =>
                  key === "Agenda" && !item.kind
                    ? {
                        ...item,
                        kind: "Sessão",
                        status:
                          item.status === "Rascunho"
                            ? "A confirmar"
                            : item.status,
                      }
                    : item,
                ),
            ]),
        )
      }
    } catch {
      // Preserve the existing fallback for malformed or unavailable storage.
    }
    return initializeDemoData(data)
  }
  save(data) {
    this.storage.setItem("kawz-data", JSON.stringify(data))
  }
}
