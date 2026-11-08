# Wallet & Ledger: frontend

Single-page app for the wallet: sign in, balances, transfers and history. All money is fake.

## Stack

React, TypeScript, Vite · Tailwind CSS v4 + shadcn/ui · React Hook Form + Zod · React Router

## Run it

Requirements: Node 24.

```bash
npm ci
npm run dev      # http://localhost:5173
```

| Script            | What it does                       |
|-------------------|------------------------------------|
| `npm run dev`     | Start the dev server               |
| `npm run build`   | Type-check and build for release   |
| `npm run lint`    | Run ESLint                         |
| `npm run preview` | Serve the production build locally |

CI runs `npm run lint` and `npm run build` on every pull request.

## Structure

```
src/
├── components/
│ ├── ui/ shadcn components (keep at shadcn defaults)
│ └── auth/ sign-in and sign-up forms
├── hooks/ useTheme
├── lib/ theme helpers, utilities
├── pages/ route-level pages (AuthPage)
└── schemas/ Zod schemas shared by the forms
```

## Conventions

- **Colors come from theme tokens only** (`bg-primary`, `text-success`, `text-danger`), never raw colors or hex values,
  so light and dark mode both work.
- **Money is a decimal string** with at most 4 decimal places (matching the backend's `NUMERIC(19,4)`), never a JS
  float.
- **Dark mode:** the `dark` class on `<html>`. The choice is saved only when the user clicks the toggle; otherwise the
  page follows the operating system. `index.html` has a small inline script that applies it before the first paint, so
  keep it in sync with `src/lib/theme.ts`.
- **Imports** use the `@/` alias.
- Component files in `components/ui` stay as shadcn generated them. Size and spacing changes go in `className` where the
  component is used.

## Status

Sign-in and sign-up screens, theme and dark mode are done. Forms do not call an API yet. Router, app layout, dashboard,
transfers and history come next, using mock data first and the backend API once it exists.
