# PRD — Job Tracker (Rastreador de Candidaturas)

## 1. Visão geral

Aplicação web para registrar e acompanhar candidaturas a vagas dev, com um dashboard de métricas que dá visibilidade sobre o funil de candidatura (aplicado → triagem → entrevista → oferta/rejeitado).

**Objetivo duplo:**
- Projeto de portfólio em Java/Spring Boot (reaproveitando a base do `rest-api`), reforçando a narrativa técnica em Java.
- Ferramenta pessoal para o processo real de busca de vaga.

## 2. Problema

Hoje o acompanhamento de candidaturas é manual/disperso (planilha, memória, e-mails soltos). Não há visibilidade de:
- Quantas vagas estão em cada etapa do funil.
- Taxa de conversão entre etapas (ex: quantas triagens viram entrevista).
- Onde o funil está travando (ex: muita aplicação, pouca resposta).

## 3. Escopo da v1 (MVP)

**Dentro do escopo:**
- CRUD de vaga (criar, listar, editar, excluir/arquivar).
- Histórico de mudança de status por vaga (tabela de eventos, não só um campo mutável).
- Dashboard com métricas agregadas (ver seção 6).
- Autenticação simples (login único, é uso pessoal — não precisa multi-usuário na v1).

**Fora do escopo (v2+):**
- Parsing automático de descrição de vaga / match score com currículo.
- Integração com e-mail (Gmail) para detectar respostas automaticamente.
- Lembretes/notificações automáticas de follow-up.
- Multi-usuário / multi-tenant.

Esse corte é deliberado: a v1 precisa estar funcionando e publicada antes de qualquer feature de v2 ser tocada.

## 4. Modelo de domínio

### Entidade `Vaga`
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID/Long | PK |
| empresa | String | obrigatório |
| cargo | String | obrigatório |
| plataforma | String | ex: Gupy, LinkedIn, Programathor |
| link | String | opcional |
| statusAtual | Enum (`StatusVaga`) | derivado do último evento em `StatusHistorico` |
| dataCriacao | Date | data da candidatura |

### Enum `StatusVaga`
```
APLICADO → TRIAGEM → ENTREVISTA → OFERTA
                                 → REJEITADO
```
Transições válidas modeladas explicitamente (não é um campo string livre) — qualquer status pode ir para `REJEITADO`, mas a progressão "positiva" segue a ordem acima.

### Entidade `StatusHistorico`
| Campo | Tipo | Observação |
|---|---|---|
| id | UUID/Long | PK |
| vagaId | FK → Vaga | |
| status | Enum `StatusVaga` | |
| dataMudanca | Timestamp | |
| observacao | String | opcional, ex: "recrutador pediu case técnico" |

Motivo de ter histórico e não só um campo mutável: permite calcular tempo médio em cada etapa e mostra domínio de modelagem de dados no portfólio.

## 5. Funcionalidades

1. **Cadastrar vaga** — formulário com empresa, cargo, plataforma, link.
2. **Atualizar status** — ação que grava um novo registro em `StatusHistorico` e atualiza `statusAtual`.
3. **Listar vagas** — tabela filtrável por status e plataforma.
4. **Dashboard de métricas** — página única com os indicadores da seção 6.
5. **Login** — autenticação simples (Spring Security), uso pessoal.

## 6. Métricas do dashboard (v1)

- Total de vagas aplicadas (geral e por período).
- Distribuição por status atual (quantas em cada etapa, ex: gráfico de barras).
- Taxa de conversão entre etapas (ex: % de aplicações que viraram triagem, % de triagens que viraram entrevista).
- Tempo médio em cada etapa (baseado nos timestamps do `StatusHistorico`).
- Top plataformas por volume de candidatura.

## 7. Stack técnica

- **Backend:** Java + Spring Boot (Spring Security, JPA/Hibernate, PostgreSQL) — mesma base do `rest-api`.
- **Frontend:** a definir (opção simples: Thymeleaf server-side para não abrir uma segunda frente de aprendizado; opção mais ambiciosa: React/Next.js separado, mas isso é decisão consciente de aumentar escopo).
- **Gráficos do dashboard:** Chart.js (consumindo endpoint REST que retorna os agregados).
- **Deploy:** Docker (reaproveitando o que já foi feito no `rest-api`/`pulse-monitor`).

## 8. Critérios de sucesso da v1

- CRUD de vaga funcionando ponta a ponta, com histórico de status persistido.
- Dashboard exibindo as 5 métricas da seção 6 com dados reais.
- Projeto publicado (repo documentado, README, deploy funcional ou instruções claras de execução local).
- Zero features da lista "fora do escopo" implementadas antes disso estar pronto.

## 9. Riscos identificados

- **Escopo inflar antes do MVP fechar** (risco já identificado como padrão pessoal em projetos anteriores) — mitigação: este documento é o corte oficial; qualquer feature de v2 discutida vai para uma seção separada, não entra na v1.
- **Frontend virar a segunda frente de aprendizado e atrasar o backend** — decidir a stack de frontend antes de começar o backend, não durante.
