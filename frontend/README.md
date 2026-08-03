# Horizon — frontend

Next.js (App Router) + Tailwind consumindo a API do `../backend`. Instruções completas de setup (backend + frontend juntos) estão no [README da raiz](../README.md).

```bash
cp .env.example .env.local   # NEXT_PUBLIC_API_URL, default http://localhost:8081
npm install
npm run dev
```

Abre em `http://localhost:3000`. Precisa do backend rodando (`docker compose up` na raiz) — sem token válido, redireciona para `/login`.

## Estrutura

```
app/
  layout.tsx      raiz: fontes (Fraunces/IBM Plex), Header persistente
  login/          tela de login (POST /api/v1/auth/login)
  vagas/          listagem + criação (issue #12)
lib/
  api.ts          fetch client centralizado + armazenamento de token
  types.ts        tipos espelhando os DTOs do backend
  status.ts       cor/rótulo por StatusVaga (fonte única de verdade)
components/
  Header.tsx      nav + faixa-horizonte (distribuição por status)
```

Sistema de design documentado em `../decisions.md` (issue #11).
