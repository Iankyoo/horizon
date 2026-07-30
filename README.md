# Horizon

Rastreador de candidaturas a vagas dev, com histórico de status por vaga e um dashboard de métricas sobre o funil de candidatura (aplicado → triagem → entrevista → oferta/rejeitado).

Projeto de portfólio (Java/Spring Boot + Next.js) que também serve como ferramenta pessoal para o processo real de busca de vaga.

## Documentação

- [PRD](docs/prd.md) — visão de produto, escopo da v1, modelo de domínio, métricas.
- [Arquitetura](docs/arquitetura.md) — decisões técnicas, contrato da API, estrutura de pastas.

## Stack

- **Backend:** Java + Spring Boot (Spring Security/JWT, JPA/Hibernate, PostgreSQL).
- **Frontend:** Next.js + Tailwind.
- **Gráficos:** Chart.js.
- **Infra:** Docker Compose (API + PostgreSQL).

## Ordem de execução

1. Modelagem + backend completo (entidades, endpoints, regras de transição de status).
2. Validação manual dos endpoints via Swagger UI.
3. Frontend Next.js consumindo a API já estável.

Ver [docs/arquitetura.md](docs/arquitetura.md) para detalhes.

## Status

Em desenvolvimento — v1 (MVP) ainda não iniciada. Acompanhe o progresso nas [issues](../../issues).
