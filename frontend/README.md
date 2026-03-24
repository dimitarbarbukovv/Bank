# Bank System Frontend

React + TypeScript + Vite frontend for the Bank Control Center UI.

## Requirements

- Node.js 20+
- npm 10+
- Running backend on `http://127.0.0.1:8080`

## Run locally

```bash
npm install
npm run dev -- --host 127.0.0.1 --port 5173 --strictPort
```

Open:

- `http://127.0.0.1:5173`

## Demo login

- Username: `admin`
- Password: `admin123`

## Main scripts

- `npm run dev` - start development server
- `npm run build` - production build
- `npm run preview` - preview production build
- `npm run lint` - lint project

## Backend API base URL

The app points to:

- `http://localhost:8080/api`

If you run backend on another host/port, update `API` in `src/App.tsx`.

## Notes

- The UI uses JWT bearer token returned by `/api/auth/login`.
- If data does not appear in the browser, hard refresh the page and make sure backend CORS allows the current frontend origin.
