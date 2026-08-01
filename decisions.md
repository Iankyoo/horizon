# Decisions Log — Horizon

Registro cronológico de decisões tomadas durante o desenvolvimento, com ação, motivo e trade-off. Objetivo: preservar o "porquê" que não fica óbvio só lendo o código ou o PRD/arquitetura.

---

## 2026-07-30 — Regra de transição de status: seguir a Arquitetura, não o PRD

**Ação:** Adotada a regra de `docs/arquitetura.md` (transições livres entre qualquer status, mais `REJEITADO` a partir de qualquer estado) em vez da regra de `docs/prd.md` (progressão ordenada `APLICADO → TRIAGEM → ENTREVISTA → OFERTA`, com `REJEITADO` a qualquer momento). Os dois documentos divergiam nesse ponto.

**Motivo:** A arquitetura marca essa regra explicitamente como "decisão fechada" (seção 10), o que indica que foi a última palavra sobre o assunto entre os dois documentos.

**Trade-off:** Perde-se a validação de que o funil só anda "para frente" — um bug de UI poderia, por exemplo, levar uma vaga de `OFERTA` de volta para `APLICADO` sem nenhuma trava no backend. Em compensação, a implementação fica mais simples (não precisa de tabela de transições permitidas por origem) e cobre o caso real de corrigir um misclick.

## 2026-07-30 — Repositório público no GitHub

**Ação:** `horizon` criado como repositório público em `github.com/Iankyoo/horizon`.

**Motivo:** O objetivo declarado do projeto é portfólio (sinalizar arquitetura de API REST para recrutadores/fintechs) — um repo privado não cumpriria esse objetivo.

**Trade-off:** Nenhuma credencial ou dado sensível pode ser commitado a partir de agora (ex: JWT secret real, credenciais de banco) — tudo precisa ir por variável de ambiente/`.env` (ignorado no git).

## 2026-07-30 — Docs organizados em `docs/`, `.claude/` fora do repo

**Ação:** PRD e documento de arquitetura movidos para `docs/prd.md` e `docs/arquitetura.md`; README criado na raiz; `.claude/` (artefatos do Claude Code) adicionado ao `.gitignore` e nunca commitado.

**Motivo:** Pedido explícito do usuário — os docs de produto fazem parte do histórico do projeto, mas os artefatos de uso da ferramenta de IA não têm valor para quem visita o repo.

**Trade-off:** Nenhum relevante — decisão de organização sem custo técnico.

## 2026-07-30 — 15 issues cobrindo a v1, com gate de Swagger antes do frontend

**Ação:** Backlog da v1 quebrado em 15 issues (labels `backend`/`frontend`/`infra`/`documentation`, milestone "v1 - MVP"), na ordem: setup backend → modelagem → 6 endpoints → auth → validação manual via Swagger (#10) → setup frontend → 3 páginas → checklist de fechamento da v1.

**Motivo:** `docs/arquitetura.md` seção 2 pede explicitamente que o backend fique completo e validável sozinho antes de qualquer linha de frontend, para não deixar dúvidas de frontend (nível ainda básico) distorcerem decisões de backend.

**Trade-off:** O frontend fica bloqueado por mais tempo (nada de tela para mostrar progresso cedo) — aceito conscientemente porque o risco identificado no PRD (seção 9) é justamente escopo/frontend atrasando o backend, não o contrário.

---

## Issue #1 — Setup inicial do backend

## 2026-07-30 — Reaproveitar a base do `rest-api` (Spring Boot 3.5.16, Java 21) em vez de gerar um projeto novo do zero

**Ação:** `pom.xml`, Maven Wrapper (`mvnw`/`.mvn/wrapper`) e `.gitignore` do backend copiados/adaptados do projeto `rest-api` (`c:/Projetos/rest-api`), trocando apenas `groupId`/`artifactId`/pacote para `com.iankyoo.horizon`. Dependências: `spring-boot-starter-web`, `-security`, `-data-jpa`, `-validation`, `postgresql`, `lombok`, `devtools`, `jjwt` (api/impl/jackson 0.12.6).

**Motivo:** O PRD (seção 7) pede explicitamente "mesma base do `rest-api`" — reaproveitar reduz risco de drift de versão entre os projetos de portfólio e evita decidir de novo escolhas já validadas (versão do Spring Boot, biblioteca de JWT).

**Trade-off:** Herda eventuais decisões datadas do `rest-api` (ex: Spring Boot 3.5.16 pode não ser a versão mais recente quando o Horizon for revisado) em troca de consistência entre os projetos do portfólio.

## 2026-07-30 — Estrutura de monorepo: `backend/` e `frontend/` como pastas irmãs

**Ação:** Código Java entra em `backend/` (não na raiz do repo), reservando a raiz para `frontend/` (a ser criado na issue #11), `docs/` e arquivos de projeto (README, docker-compose).

**Motivo:** O PRD/arquitetura descrevem dois serviços desacoplados (API Spring Boot + Next.js) publicados a partir do mesmo repositório; `rest-api` podia ficar na raiz porque era mono-serviço, o Horizon não é.

**Trade-off:** Builds/CI (quando existirem, pós-v1) precisam apontar para o subdiretório certo em vez de assumir a raiz — custo aceito porque só paga quando CI for implementado (fora de escopo da v1, arquitetura seção 9).

## 2026-07-30 — `docker-compose.yml` na raiz do repo com serviços `api` + `postgres`

**Ação:** Diferente do `rest-api` (cujo `docker-compose.yml` só sobe o Postgres, rodando a API via `mvnw` local), o Horizon ganha um `Dockerfile` para a API e um serviço `api` no compose, além do `postgres`.

**Motivo:** `docs/arquitetura.md` (seção 9) pede literalmente "container da API + container PostgreSQL" — é um requisito explícito da issue #1, não só reaproveitamento do padrão anterior.

**Trade-off:** Um Dockerfile a mais para manter; em compensação, `docker compose up` sobe o ambiente completo sem exigir Maven instalado na máquina de quem for rodar o projeto.

## 2026-07-30 — JWT secret e credenciais de banco via variável de ambiente, nunca hardcoded

**Ação:** `application.yml` usa `${JWT_SECRET}`/`${DB_PASSWORD}` (sem default em produção; default só no profile `local` para facilitar rodar sem setup extra); valores reais ficam em `.env` (git-ignorado) com `.env.example` versionado como referência.

**Motivo:** O `rest-api` (repositório privado) commitava o secret JWT direto no `application.properties` — inaceitável aqui porque o Horizon é público desde o primeiro commit.

**Trade-off:** Mais um passo de setup para quem for rodar o projeto localmente (copiar `.env.example` para `.env`) em troca de não vazar segredo nenhum no histórico do git.
