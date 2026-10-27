# figma-make-app

JavaScript + React + Vite + CSS Modules project running inside Figma Make.

## Development Server

A Vite development server is **already running** on `$PORT` (default 8443). You don't need to start it manually.

- Preview URL: The user can access the running app through the preview panel
- Hot reload: Changes to source files are reflected immediately

## Project Structure

This is the canonical project structure. Start with task-relevant files below. Only follow imports or inspect other files when required, when a documented path is missing, or when the repository contradicts this guide.

- `src/main.jsx` - React entrypoint; imports `src/index.css` and mounts `src/App.jsx` into the `#root` element
- `src/App.jsx` - Primary application component and the usual starting point for UI work
- `src/App.module.css` - Locally scoped application and component styles
- `src/styles/classes.js` - Resolves local CSS Module class names and leaves global utility classes unchanged
- `src/ui/index.jsx` and `src/ui/Controls.module.css` - Local accessible React controls, fields, modal and sidebar; no external component kit
- `src/index.css` - Global design-system tokens, font/base rules and Tailwind CSS v4 import
- `index.html` - Vite HTML shell containing the `#root` element and loading `src/main.jsx`
- `package.json` - Project dependencies and the Vite build, development, preview, and formatting scripts
- `vite.config.js` - Vite configuration with React, Tailwind CSS v4, and Figma Make plugins plus the `@` alias for `src`
- `jsconfig.json` - JavaScript editor configuration and path aliases
- `.mise.toml` - Toolchain versions for Node.js

## Dependencies

- Runtime: React 19 and React DOM 19
- Styling: Tailwind CSS v4 with the `@tailwindcss/vite` plugin
- Build tooling: Vite 8 and `@vitejs/plugin-react`
- Formatting: oxfmt

## Styling

This project uses **CSS Modules** for application styles. Use `src/App.module.css` and resolve its class names through `cssClass` from `src/styles/classes.js`. For DOM queries, use `classSelector` to resolve module class names rather than hardcoded selectors. Global utility names are passed through unchanged. Reuse local React primitives from `src/ui/index.jsx`.

Tailwind CSS v4 remains installed for the existing global reset and utilities via `@tailwindcss/vite` in `vite.config.js`. `src/index.css` owns all design-system variables, font wiring and base styles; do not hardcode visual values in JSX. Keep CSS `@import` statements first, then any `@font-face` rules and font-family defaults. No external UI component library is required.

## Verification

- Production build: `npm run build`
- Local data and demo-auth tests: `node --test src/features/studio/model/demoData.test.js src/features/auth/model/localAuth.test.js`
- Local UI rendering tests: `npm run test:ui` (HTML rendering only, not browser layout or interaction)
- The application source uses `.js` and `.jsx`, not TypeScript.

## Code quality

- Use double quotes for strings containing apostrophes (`"We're here to help"`), or escape them in single-quoted strings. An unescaped apostrophe in a single-quoted string breaks the build.
- Ensure JSX tags are closed and braces are balanced.
- Export components as default exports.
