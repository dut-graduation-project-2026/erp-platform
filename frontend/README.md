# ERP Platform Frontend

This is the frontend application for the **ERP Platform**, built with **Next.js 16**, **React 19**, and **TypeScript**. It provides the web interface for core ERP workflows and will integrate with the backend services in this repository.

## Prerequisites

- Node.js 20+
- npm 10+

## Getting Started

1. Install dependencies:

```bash
npm install
```

2. Start the development server:

```bash
npm run dev
```

3. Open the app:

```text
http://localhost:3000
```

## Available Scripts

- `npm run dev` — Run the Next.js development server.
- `npm run build` — Build the app for production.
- `npm run start` — Start the production server after build.
- `npm run lint` — Run ESLint.

## Project Structure

```text
src/
  app/         # App Router entry points, layout, and global styles
  components/  # Reusable UI components
  contexts/    # React context providers
  hooks/       # Custom React hooks
  layouts/     # Shared page layout components
  lib/         # Library and integration helpers
  types/       # Shared TypeScript types
  utils/       # Utility functions
```

## Current Status

The frontend is initialized and ready for feature development. The landing page currently displays a basic ERP Platform heading.
