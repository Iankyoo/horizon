# Documento de Arquitetura — Horizon

Baseado no PRD já definido. Escopo: v1 = CRUD de vaga + histórico de status + dashboard de métricas, sem integrações externas.

## 1. Visão geral

Arquitetura desacoplada: API REST (Spring Boot) como única fonte de verdade, consumida por um frontend Next.js separado.

```
[ Next.js (frontend) ]  --HTTP/JSON-->  [ Spring Boot API ]  -->  [ PostgreSQL ]
        |                                      |
   Tailwind/UI                        Controller → Service → Repository
```

Por que desacoplado e não monolito (Thymeleaf): o objetivo declarado do projeto é sinalizar arquitetura de API REST — padrão que fintechs (alvo: Itaú, Santander, C6) esperam ver — e reaproveitar a experiência já existente com Next.js/Tailwind do `cardapio-digital`.

## 2. Ordem de execução (importante)

Como o nível de frontend ainda é básico (HTML/CSS + Next.js começando agora), a arquitetura foi desenhada para que o backend seja **completo e validável sozinho**, via Swagger/Postman, antes de qualquer linha de frontend:

1. Modelagem + backend completo (entidades, endpoints, regras de transição de status).
2. Validação manual de todos os endpoints via Swagger UI.
3. Só então o frontend Next.js começa a consumir a API já estável.

Isso evita que dúvidas de frontend bloqueiem ou distorçam decisões de backend.

## 3. Camadas do backend

```
Controller  → recebe requisição HTTP, valida entrada (DTO), chama Service
Service     → regra de negócio (ex: validar transição de status, calcular métricas)
Repository  → JPA, acesso a dados
Entity      → mapeamento com o banco (Vaga, StatusHistorico)
DTO         → objetos de entrada/saída da API (nunca expor Entity diretamente)
```

Pastas sugeridas (padrão que você já usa no `rest-api`):
```
com.iankyoo.horizon
├── controller
├── service
├── repository
├── model (entities)
├── dto
├── enums (StatusVaga)
├── security
└── exception (handler global)
```

## 4. Modelo de dados

### Entidade `Vaga`
- id, empresa, cargo, plataforma, link, statusAtual (enum), dataCriacao

### Entidade `StatusHistorico`
- id, vagaId (FK), status (enum), dataMudanca, observacao

### Enum `StatusVaga`
```
APLICADO ⇄ TRIAGEM ⇄ ENTREVISTA ⇄ OFERTA
                                 → REJEITADO (a partir de qualquer etapa)
```
Transições são reversíveis (permite corrigir misclick ou engano) em qualquer direção entre etapas, além de `REJEITADO` a partir de qualquer estado. Regra de negócio no Service: validar que o novo status é um valor válido do enum antes de gravar em `StatusHistorico` e atualizar `statusAtual` — é aqui que fica a lógica de "máquina de estado", não no controller. Como o modelo é reversível, a validação é mais simples (não precisa de tabela de transições permitidas por origem) — qualquer status pode ir para qualquer outro.

## 5. Contrato da API (v1)

| Método | Endpoint | Descrição |
|---|---|---|
| POST | `/api/v1/vagas` | Cria vaga (status inicial: APLICADO) |
| GET | `/api/v1/vagas` | Lista vagas (filtros: status, plataforma — query params) |
| GET | `/api/v1/vagas/{id}` | Detalhe da vaga + histórico completo |
| PATCH | `/api/v1/vagas/{id}/status` | Registra mudança de status (body: novo status + observação opcional) |
| DELETE | `/api/v1/vagas/{id}` | Arquiva/remove vaga |
| GET | `/api/v1/dashboard/metrics` | Retorna todas as métricas agregadas de uma vez |

`GET /dashboard/metrics` retorna um único payload (total, distribuição por status, taxa de conversão por etapa, tempo médio por etapa, top plataformas) — evita múltiplas chamadas do frontend para montar uma única tela.

## 6. Segurança

Spring Security com JWT (mesmo padrão já dominado nos projetos anteriores) — mesmo sendo uso pessoal/single-user, mantém consistência de portfólio (mostra o mesmo padrão de segurança em todos os projetos Java). Login único, sem cadastro público de usuários na v1.

## 7. Frontend (Next.js)

Estrutura mínima, dado o nível ainda inicial em frontend — evitar padrões avançados de Next (server actions, middleware complexo) na v1:

```
/app
  /vagas         → listagem + formulário de criação
  /vagas/[id]    → detalhe + histórico + mudança de status
  /dashboard     → página única consumindo GET /dashboard/metrics
/lib
  api.ts         → funções fetch centralizadas (uma por endpoint)
```

Recomendação: usar `fetch` simples client-side (sem gerenciamento de estado global tipo Redux/Zustand) — o app é pequeno o suficiente para não precisar disso, e reduz a quantidade de conceitos novos de frontend de uma vez.

## 8. Fluxo de exemplo: mudar status de uma vaga

```
1. Frontend: usuário clica "Mover para Entrevista" na tela de detalhe
2. PATCH /api/v1/vagas/{id}/status  { status: "ENTREVISTA", observacao: "..." }
3. Controller valida DTO → Service
4. Service valida se a transição é permitida (regra da máquina de estado)
5. Service grava novo registro em StatusHistorico + atualiza statusAtual da Vaga
6. Retorna 200 com a Vaga atualizada
7. Frontend atualiza a UI local com a resposta
```

## 9. Infraestrutura

- Docker Compose local: container da API + container PostgreSQL (mesmo padrão do `rest-api`/`pulse-monitor`).
- Frontend roda separado (`npm run dev` local na v1; deploy na Vercel é decisão de v2).
- Sem CI/CD na v1 — adicionar depois que o MVP estiver estável, não antes.

## 10. Decisões fechadas

- Nome do projeto/pacote Java: **Horizon** (`com.iankyoo.horizon`).
- Histórico de status: **reversível** em qualquer direção, além de `REJEITADO` a partir de qualquer estado.
- `observacao` no `StatusHistorico`: **opcional**.
