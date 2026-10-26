import { registerHooks } from "node:module"
import { readFileSync } from "node:fs"
import ts from "typescript"

// Only used by Node's rendering tests. Production CSS Modules remain Vite-owned.
registerHooks({
  load(url, context, nextLoad) {
    if (url.startsWith("file:") && url.endsWith(".jsx")) {
      const source = readFileSync(new URL(url), "utf8")
      return {
        format: "module",
        shortCircuit: true,
        source: ts.transpileModule(source, {
          compilerOptions: {
            module: ts.ModuleKind.ESNext,
            target: ts.ScriptTarget.ES2022,
            jsx: ts.JsxEmit.ReactJSX,
          },
        }).outputText,
      }
    }
    if (url.startsWith("file:") && url.endsWith(".css")) {
      const source = readFileSync(new URL(url), "utf8")
      const names = [...source.matchAll(/\.([\p{L}_-][\p{L}\p{N}_-]*)/gu)].map(
        (match) => match[1],
      )
      return {
        format: "module",
        shortCircuit: true,
        source: `export default ${JSON.stringify(Object.fromEntries(names.map((name) => [name, name])))};`,
      }
    }
    return nextLoad(url, context)
  },
})
