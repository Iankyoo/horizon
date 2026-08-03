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

## 2026-08-01 — `spring.profiles.active: local` como default em `application.yml`

**Ação:** Adicionado `spring.profiles.active: ${SPRING_PROFILES_ACTIVE:local}` no `application.yml` base, em vez de deixar o profile totalmente implícito.

**Motivo:** Ao validar o setup da issue #1 rodando `./mvnw spring-boot:run` sem a variável `SPRING_PROFILES_ACTIVE` exportada, a aplicação caiu no profile `default` do Spring (sem nenhum datasource configurado) e falhou ao subir — o README já prometia "rodar local = perfil `local`" sem isso estar garantido em código.

**Trade-off:** Nenhum relevante — o `docker-compose.yml` continua sobrescrevendo explicitamente para `docker` via variável de ambiente, então o comportamento em container não muda; só corrige o caminho "rodando local sem Docker".

---

## Issue #2 — Modelagem de entidades

## 2026-08-01 — `statusAtual` não recebe default na Entity; inicialização fica para o Service (issue #3)

**Ação:** O campo `Vaga.statusAtual` é `@Column(nullable = false)` mas sem valor default no Java — não é setado automaticamente para `APLICADO` na construção do objeto.

**Motivo:** Definir o status inicial é regra de negócio do fluxo de criação (`POST /api/v1/vagas`, escopo da issue #3), não da modelagem estrutural. Fixar o default na Entity misturaria as duas responsabilidades e criaria um comportamento "escondido" fora do Service, que é onde a arquitetura (seção 3) diz que a regra de negócio deve morar.

**Trade-off:** Um `new Vaga()` sem passar `statusAtual` explicitamente vai falhar na constraint `NOT NULL` do banco — aceitável porque força quem cria uma vaga (o Service, na próxima issue) a decidir o status conscientemente, em vez de confiar num default implícito.

## 2026-08-01 — `StatusHistorico` não tem coleção `@OneToMany` de volta para `Vaga`

**Ação:** O relacionamento é modelado só como `@ManyToOne` em `StatusHistorico → Vaga` (unidirecional). `Vaga` não tem uma lista `List<StatusHistorico>`.

**Motivo:** `GET /vagas/{id}` (issue #5) precisa retornar o histórico completo, mas isso é responsabilidade de query no Service/Repository (`findByVagaIdOrderByDataMudancaAsc`), não de navegação de grafo de entidade. Evita o risco clássico de N+1/coleção lazy carregada sem controle dentro da Entity.

**Trade-off:** O Service da issue #5 precisa fazer uma query explícita em vez de simplesmente chamar `vaga.getHistorico()` — um pouco mais verboso, mas mais previsível em termos de performance e mais fácil de testar isoladamente.

## 2026-08-01 — Validação da modelagem via schema real, não via teste automatizado

**Ação:** A modelagem foi validada subindo o Postgres do `docker-compose` e rodando a aplicação localmente (`ddl-auto=update`), inspecionando as tabelas geradas via `psql` (`\d vaga`, `\d status_historico`) — confirmando colunas, constraints `NOT NULL`, `CHECK` dos enums e a FK `status_historico.vaga_id → vaga.id`. Nenhum teste automatizado foi adicionado nesta issue.

**Motivo:** O escopo da issue #2 é estritamente "entidades + enum + DDL" — repositórios, services e testes entram nas issues seguintes (#3 em diante). Adicionar testes agora seria antecipar trabalho de fora do escopo definido, o que o PRD (seção 9) já identifica como o principal risco do projeto (escopo inflar antes do MVP fechar).

**Trade-off:** Sem teste automatizado, uma regressão futura na modelagem só seria pega manualmente ou pelas issues seguintes que dependem dela — aceitável porque o schema já foi verificado byte a byte contra o banco real nesta validação.

---

## Issue #3 — `POST /api/v1/vagas`

## 2026-08-01 — `SecurityConfig` placeholder (CSRF off + `permitAll`) adicionado antes da hora, para viabilizar validação manual dos endpoints

**Ação:** Criado `security/SecurityConfig.java` desligando CSRF e liberando todas as rotas (`permitAll`), mesmo essa issue não pedindo isso — a issue de autenticação de verdade (JWT) é a #9.

**Motivo:** Sem nenhum `SecurityConfig`, o Spring Security aplica o padrão (Basic Auth + sessão + CSRF ativo). Ao testar o `POST /api/v1/vagas` via curl com o usuário/senha gerados automaticamente, a requisição voltava `401` mesmo com credenciais corretas — o `CsrfFilter` roda antes do `BasicAuthenticationFilter` no chain padrão, então uma requisição `POST` sem token CSRF é rejeitada como se o usuário não estivesse autenticado (o `ExceptionTranslationFilter` trata `AccessDeniedException` de um principal anônimo como se fosse falta de autenticação, chamando o `AuthenticationEntryPoint` → 401 com `WWW-Authenticate: Basic`, disfarçando o real motivo). Sem isso resolvido, seria impossível validar manualmente qualquer endpoint de escrita (#3 a #8) antes da issue #9 — e a arquitetura (seção 2) exige justamente essa validação manual completa do backend antes do frontend começar.

**Trade-off:** A API fica **sem nenhuma autenticação real** entre agora e a issue #9 — aceitável só porque o ambiente é local/dev (nunca fica exposto publicamente nesse estado) e porque o arquivo está marcado explicitamente como placeholder no próprio código e será totalmente substituído, não incrementado, quando o JWT entrar.

## 2026-08-01 — `VagaService.createVaga` é `@Transactional`

**Ação:** O método que salva a `Vaga` e o primeiro `StatusHistorico` está anotado com `@Transactional`, garantindo que os dois `save()` aconteçam na mesma transação.

**Motivo:** A issue pede explicitamente "ao criar, também gravar o primeiro registro em StatusHistorico" — sem transação, uma falha entre os dois `save()` deixaria uma `Vaga` sem histórico algum, quebrando a premissa do domínio (toda vaga sempre tem pelo menos um evento de histórico).

**Trade-off:** Nenhum relevante para este caso de uso (é uma operação de escrita simples, sem chamada externa lenta dentro da transação).

## 2026-08-01 — Validação end-to-end confirmada via `docker compose` + `psql`, sem endpoint de leitura ainda

**Ação:** O endpoint foi validado subindo `docker compose` e testando `POST /api/v1/vagas` via curl (caso de sucesso 201 + caso de validação 400 com `empresa`/`cargo` em branco), depois conferindo as duas linhas persistidas (`vaga` e `status_historico`) direto via `psql` — não existe ainda `GET /vagas/{id}` (issue #5) para inspecionar pela própria API.

**Motivo:** Manter a validação restrita ao que a issue #3 realmente entrega, sem adiantar o endpoint de leitura só para "testar mais bonito".

**Trade-off:** Nenhum — é só uma nota de que a validação ponta a ponta via HTTP completa (criar e depois ler pela API) só fica possível a partir da issue #5.

---

## Issue #4 — `GET /api/v1/vagas` (listagem com filtros)

## 2026-08-01 — Listagem paginada (`Page<VagaResponse>`), não `List`

**Ação:** `GET /api/v1/vagas` recebe `Pageable` (query params `page`/`size`/`sort` padrão do Spring Data) e retorna `Page<VagaResponse>`, em vez de uma lista simples.

**Motivo:** Nem o PRD nem a arquitetura pedem paginação explicitamente, mas é o padrão já usado em todo o `rest-api` (`CategoryController`, `MenuItemController` — ambos `Page<T>` via `Pageable`). Manter o mesmo padrão entre os projetos do portfólio é uma escolha deliberada de consistência, e paginação sai de graça com Spring Data (não é código extra relevante).

**Trade-off:** O corpo da resposta fica mais verboso (metadados de paginação) do que uma lista simples — aceitável dado que é exatamente o formato que o `rest-api` já usa, e o dashboard/frontend (issues futuras) vai precisar lidar com paginação de qualquer forma se a lista de vagas crescer.

## 2026-08-01 — Filtros opcionais via JPQL com `:param IS NULL OR ...`, não Specifications

**Ação:** `VagaRepository.findByFilters` usa uma única `@Query` JPQL com `(:status IS NULL OR v.statusAtual = :status) AND (:plataforma IS NULL OR v.plataforma = :plataforma)`, em vez de `JpaSpecificationExecutor`/Criteria API.

**Motivo:** Só existem dois filtros opcionais (seção 5 da arquitetura não pede mais que isso na v1). Specifications valem a pena quando o número de combinações de filtro cresce; para dois campos, a query JPQL única é mais direta de ler e não introduz uma dependência/padrão novo (Specification) só usado neste um lugar.

**Trade-off:** Se a v2 precisar de mais filtros (ex: por empresa, por intervalo de data), essa query vai ficar longa e provavelmente compensará migrar para Specifications nesse momento — não antecipei isso agora porque seria over-engineering para o escopo atual da v1.

## 2026-08-01 — Segundo conflito de porta descoberto durante a validação: `5433` já estava em uso por outro projeto

**Ação:** Porta do Postgres no `docker-compose.yml` trocada de fixa `5433:5432` para configurável `${DB_PORT:-5544}:5432` (default novo: `5544`). `application-local.yml` e `.env.example` atualizados para acompanhar.

**Motivo:** Ao revalidar o ambiente para testar os filtros desta issue, o `horizon_db` falhou ao subir: a porta `5433` já estava alocada por um container de outro projeto (`bank-analyzer-db_test-1`, rodando havia ~20 min, sem relação com o Horizon). Mesma classe de problema já visto com a porta `8080` na issue #1 (processo local do `codearena`) — múltiplos projetos deste ambiente competem pelas mesmas portas padrão de Postgres (5432-5434 já estavam todas em uso por outros repos: `bank-analyzer`, `rest-api`, etc.).

**Trade-off:** Nenhum custo real — só documentando para não repetir o diagnóstico caso a porta `5544` também colida no futuro (nesse caso, o padrão já está estabelecido: tornar configurável via `.env`, nunca mexer no container do outro projeto).

## 2026-08-01 — Validação de filtro por `status` cobre só `APLICADO` (único status possível até a issue #6)

**Ação:** O teste manual do filtro por `status` usou `APLICADO` (retorna as 3 vagas criadas) e `REJEITADO` (retorna vazio, confirmando que o filtro realmente restringe) — não foi possível testar uma vaga em outro status real porque `PATCH /vagas/{id}/status` (issue #6) ainda não existe.

**Motivo:** Mesma lógica da nota de validação da issue #3 — não adiantar escopo de outra issue só para ter um teste "mais completo" agora.

**Trade-off:** A cobertura do filtro por status com dado real de múltiplos status só fica completa depois da issue #6 — o comportamento da query em si (WHERE condicional) já está validado por construção (mesma cláusula usada para os dois filtros, e o filtro por `plataforma` com dados reais heterogêneos já prova que a lógica `:param IS NULL OR ...` funciona).

---

## Issue #5 — `GET /api/v1/vagas/{id}` (detalhe + histórico)

## 2026-08-01 — DTO próprio (`VagaDetailResponse`) em vez de reaproveitar `VagaResponse` com um campo a mais

**Ação:** Criado `VagaDetailResponse` (todos os campos de `VagaResponse` + `List<StatusHistoricoResponse> historico`) como um DTO separado, em vez de adicionar `historico` opcional em `VagaResponse` e reusá-lo em `POST`/`GET listagem`/`GET detalhe`.

**Motivo:** `POST` e `GET` (listagem) nunca carregam histórico — misturar os dois contratos faria `VagaResponse` ter um campo `historico` que é sempre `null`/vazio nesses dois endpoints, obrigando quem consome a API a checar por isso sem necessidade. Contratos de API devem refletir exatamente o que cada endpoint retorna.

**Trade-off:** Duplica os campos base (`id`, `empresa`, `cargo`, etc.) entre `VagaResponse` e `VagaDetailResponse` — aceitável porque são `record`s (zero boilerplate) e a duplicação é só de assinatura, não de lógica.

## 2026-08-01 — Histórico buscado via query direta no repositório, não navegação de entidade

**Ação:** `VagaService.findById` chama `statusHistoricoRepository.findByVagaIdOrderByDataMudancaAsc(id)` separadamente, em vez de `vaga.getHistorico()`.

**Motivo:** Consequência direta da decisão já tomada na issue #2 de não modelar `@OneToMany` em `Vaga` — mantém a Entity sem coleção lazy e a ordenação (`ORDER BY dataMudanca ASC`) explícita na query, sem depender de `@OrderBy` na entidade.

**Trade-off:** Nenhum novo — já estava previsto desde a issue #2.

## 2026-08-01 — Validado com histórico de um único evento (só `APLICADO`)

**Ação:** O teste manual do endpoint confirmou o array `historico` com o único evento existente (`APLICADO`, gravado no `POST` da issue #3) — ainda não há como validar múltiplos eventos no histórico de uma mesma vaga porque `PATCH /vagas/{id}/status` (issue #6) não existe.

**Motivo:** Mesma disciplina de escopo das validações anteriores.

**Trade-off:** A ordenação por `dataMudanca ASC` com múltiplos registros só será provada visualmente na issue #6, quando novos eventos de histórico passarem a ser gerados.

---

## Issue #6 — `PATCH /api/v1/vagas/{id}/status`

## 2026-08-02 — `atualizarStatus` retorna `VagaResponse` (sem histórico), não `VagaDetailResponse`

**Ação:** O endpoint retorna a `Vaga` atualizada no formato "raso" (`VagaResponse`), igual ao `POST`, em vez de trazer o histórico completo (`VagaDetailResponse`) junto.

**Motivo:** É literalmente o que a issue pede ("Retorna 200 com a Vaga atualizada"). Quem quiser o histórico atualizado depois da mudança já tem `GET /vagas/{id}` (issue #5) para isso — evita um segundo formato de resposta fazendo a mesma coisa de dois jeitos diferentes.

**Trade-off:** O frontend (issues futuras), depois de mudar o status na tela de detalhe, provavelmente vai precisar re-buscar a vaga via `GET` para atualizar o histórico exibido, em vez de já receber tudo na resposta do `PATCH` — aceitável, é uma chamada HTTP a mais em um fluxo que já é de clique único.

## 2026-08-02 — Nenhuma validação de transição além de `@NotNull` no enum

**Ação:** `VagaService.atualizarStatus` aceita qualquer `StatusVaga` não nulo e simplesmente aplica — não há checagem de "de qual status para qual status" é permitido.

**Motivo:** Decisão já fechada na arquitetura (registrada na primeira entrada deste log): transições são livres entre quaisquer status, mais `REJEITADO` a partir de qualquer estado — ou seja, **toda** transição é válida, então não existe regra a mais para implementar além de "o valor é um `StatusVaga` de verdade" (o que o `@NotNull` + deserialização do enum já garantem).

**Trade-off:** Nenhum bug possível aqui é pego pelo backend (ex: usuário clicar errado e voltar uma vaga de `OFERTA` para `APLICADO`) — é o trade-off já aceito e documentado na decisão de arquitetura, não um novo.

## 2026-08-02 — Falso positivo de bug durante a validação: acentuação quebrando o JSON era o `curl` do Git Bash, não o backend

**Ação:** Nenhuma mudança de código. Registrando o diagnóstico para não repetir a investigação.

**Motivo:** O primeiro teste de transição (`observacao` com "técnico") voltou `400 Bad Request` genérico (formato de erro padrão do Spring, não o `Map` da nossa validação) — parecia um bug real. Reproduzindo o mesmo payload sem acentos funcionou (`200`), e reproduzindo o payload acentuado via arquivo `UTF-8` (`curl --data-binary @arquivo.json`) também funcionou e persistiu o texto corretamente no Postgres (`SELECT observacao ...` mostrou "técnico" intacto). Ou seja: o Git Bash/curl no Windows não estava enviando os bytes UTF-8 corretos quando o texto acentuado vinha inline no `-d '...'` do comando — um problema do ambiente de teste, não da aplicação.

**Trade-off:** Nenhum — só uma nota para futuras validações manuais: sempre que o payload de teste tiver acento, usar `--data-binary @arquivo` com o arquivo salvo em UTF-8, nunca `-d` inline no Git Bash.

---

## Issue #7 — `DELETE /api/v1/vagas/{id}`

## 2026-08-02 — Soft delete (`arquivada: boolean`), não hard delete

**Ação:** `DELETE` não remove a linha da tabela `vaga` — seta `arquivada = true`. A própria issue deixava essa decisão em aberto explicitamente.

**Motivo:** `StatusHistorico` tem FK `NOT NULL` para `vaga` — um hard delete exigiria cascade delete do histórico (ou bloquear o delete por violação de FK), destruindo justamente os dados que a arquitetura (seção 4) diz existirem para "calcular tempo médio em cada etapa". O objetivo duplo do projeto (portfólio + ferramenta real de busca de vaga) inclui dashboard de métricas (issue #8) que depende desse histórico sobrevivendo mesmo depois que uma vaga é descartada/arquivada — é exatamente o cenário mais comum (vaga rejeitada, usuário arquiva) e o mais importante para as métricas de funil.

**Trade-off:** A tabela `vaga` cresce indefinidamente (nunca libera espaço fisicamente) — irrelevante na escala de uso pessoal da v1. Fica também a responsabilidade implícita de sempre filtrar `arquivada = false` em qualquer query nova que liste vagas (documentado abaixo).

## 2026-08-02 — Coluna `arquivada` com `columnDefinition` explícito (`default false`)

**Ação:** `@Column(nullable = false, columnDefinition = "boolean not null default false")` em vez de só `@Column(nullable = false)`.

**Motivo:** O ambiente de teste já tinha 3 vagas persistidas de validações anteriores. Sem `DEFAULT` no `ALTER TABLE`, o Postgres rejeitaria a coluna `NOT NULL` nova por causa das linhas existentes sem valor. Validei isso rodando a migração de fato contra o volume com dados (`docker compose up --build` reaproveitando o volume antigo) — subiu limpo e as 3 linhas ganharam `arquivada = false` automaticamente.

**Trade-off:** Nenhum — é a forma correta de adicionar uma coluna `NOT NULL` em uma tabela que já pode ter dados, e generaliza para qualquer ambiente (não só o de teste).

## 2026-08-02 — Listagem (`GET /vagas`) passa a excluir arquivadas incondicionalmente, sem parâmetro para incluí-las

**Ação:** `VagaRepository.findByFilters` ganhou `WHERE v.arquivada = false` fixo, antes dos filtros opcionais de `status`/`plataforma`. Não existe um jeito de listar vagas arquivadas via API nesta issue.

**Motivo:** Sem isso, "excluir/arquivar" (PRD seção 5) não mudaria nada do ponto de vista do usuário — a vaga continuaria aparecendo normalmente na listagem. O propósito prático do soft delete é justamente sumir da tela de listagem enquanto preserva o dado para métricas.

**Trade-off:** Não há, na v1, nenhuma forma de "desarquivar" ou visualizar só as arquivadas pela API — aceitável porque nenhum documento de produto pede isso; se virar necessário, é uma issue nova (ex: `GET /vagas?arquivada=true`), não uma extensão desta.

## 2026-08-02 — `GET /vagas/{id}` continua funcionando para vagas arquivadas

**Ação:** `findById` não foi alterado — não checa `arquivada`. Uma vaga arquivada continua acessível pelo detalhe (confirmado no teste: `GET /vagas/2` depois do `DELETE` retornou `200` com o histórico completo).

**Motivo:** Arquivar é "tirar da lista ativa", não "esconder para sempre" — especialmente relevante para a issue #8 (dashboard), que provavelmente vai precisar acessar dados de vagas arquivadas para calcular métricas históricas corretas (uma vaga rejeitada e arquivada ainda deve contar no funil).

**Trade-off:** Nenhum custo — é consistente com o motivo de ter escolhido soft delete em primeiro lugar.

---

## Issue #8 — `GET /api/v1/dashboard/metrics`

## 2026-08-02 — Métricas incluem vagas arquivadas (diferente da listagem, que exclui)

**Ação:** `DashboardService` não filtra por `arquivada` em nenhuma das 5 métricas — `vagaRepository.count()`, a distribuição por status, a taxa de conversão e o tempo médio consideram **todas** as vagas, arquivadas ou não. Só `VagaRepository.findByFilters` (usado pela listagem, issue #4) filtra `arquivada = false`.

**Motivo:** O propósito central do dashboard (PRD seção 2) é responder "onde o funil está travando" — uma vaga rejeitada e arquivada é exatamente o tipo de dado que precisa continuar contando na métrica (senão a taxa de conversão fica artificialmente otimista, escondendo rejeições). Arquivar é um controle de visibilidade da lista de trabalho ativa, não uma exclusão lógica do histórico — mesma lógica já usada na decisão da issue #7 de manter `GET /vagas/{id}` funcionando para arquivadas. Validei isso explicitamente: arquivei uma vaga `REJEITADO` e confirmei que `totalVagas` e `distribuicaoPorStatus` no dashboard não mudaram, enquanto ela sumiu da listagem ativa.

**Trade-off:** Nenhum — é o comportamento correto para o caso de uso; documentando porque é fácil, ao ler só o código da listagem, presumir (errado) que todo endpoint filtra arquivadas.

## 2026-08-02 — Taxa de conversão calculada por "alcançou o status alguma vez" (histórico), não pelo `statusAtual`

**Ação:** `aplicadoParaTriagem`/`triagemParaEntrevista`/`entrevistaParaOferta` são calculados contando quantas vagas **distintas** têm pelo menos um evento daquele status em `StatusHistorico` (`vagasQueAtingiram`), não quantas vagas têm `statusAtual` igual àquele valor agora.

**Motivo:** Como as transições são livres em qualquer direção (decisão da arquitetura, seção 10), o `statusAtual` atual não reflete progresso histórico — uma vaga que chegou a `ENTREVISTA` e foi corrigida de volta para `TRIAGEM` por engano continuaria contando como "chegou em entrevista" para fins de funil, mesmo não estando mais lá agora. Métrica de conversão precisa responder "quantas vagas já passaram por essa etapa", não "quantas estão nela neste exato momento" (isso já é a métrica de distribuição por status, que é separada).

**Trade-off:** Uma vaga que oscila entre estados só é contada uma vez por etapa (`Set<StatusVaga>` por vaga) — nenhuma vaga infla a métrica de conversão por ter ido e voltado várias vezes. Nenhum custo relevante.

## 2026-08-02 — Tempo médio por etapa calculado em Java a partir de intervalos consecutivos, não em SQL

**Ação:** `DashboardService` busca todo o `StatusHistorico` ordenado por `vaga_id, dataMudanca` (`findAllByOrderByVagaIdAscDataMudancaAsc`) e calcula, em memória, a duração entre cada evento e o próximo evento da mesma vaga, atribuindo a duração ao status do evento anterior (o "tempo que ficou" naquele status até sair dele).

**Motivo:** Postgres resolveria isso com `LAG()`/`LEAD()` (window functions), mas isso amarraria a query a sintaxe específica do Postgres e seria bem mais difícil de ler/testar do que um loop simples em Java. Dado o volume de dados esperado (uso pessoal, não milhares de vagas), processar em memória é perfeitamente adequado e mantém a lógica portável e legível.

**Trade-off:** Não escala para um volume grande de histórico (traria todas as linhas para a JVM de uma vez) — irrelevante na escala da v1; se um dia isso importar, é uma otimização isolada dentro do mesmo método, não uma mudança de contrato da API.

## 2026-08-02 — Unidade de tempo: dias (fracionários), não horas

**Ação:** `tempoMedioPorEtapaEmDias` expressa a duração em dias (double, arredondado a 2 casas decimais), calculado como `Duration.between(...).toMinutes() / 1440.0`.

**Motivo:** Nenhum dos dois documentos especifica a unidade. Processos seletivos tipicamente se medem em dias/semanas, não horas — um valor como "2.5 dias" é mais legível num dashboard de busca de vaga do que "60 horas". Validei a conversão manipulando timestamps reais no Postgres (gaps de 2, 3 e 1 dia entre eventos de uma vaga) e conferindo que o endpoint retornou exatamente `2.0`/`3.0` (misturado com frações de segundo de outras vagas de teste, dando `0.67`/`1.5`) — matemática batendo com o cálculo manual.

**Trade-off:** Para vagas com etapas muito rápidas (minutos), o valor aparece como uma fração de dia pequena (ex: `0.0003`) em vez de um número "redondo" — aceitável, é só uma questão de formatação que o frontend pode ajustar na exibição.

## 2026-08-02 — "Total por período" interpretado como últimos 30 dias corridos (fixo, sem parâmetro)

**Ação:** `totalUltimos30Dias` conta vagas com `dataCriacao >= hoje - 30 dias`. Não há parâmetro para escolher outro período.

**Motivo:** O PRD pede "Total de vagas aplicadas (geral e por período)" sem definir o período — nem a arquitetura elabora além de listar "total" no payload. 30 dias é um recorte padrão razoável e comum em dashboards pessoais ("neste último mês"), e evita inventar uma API de range de datas que nenhum documento pede.

**Trade-off:** Se o usuário quiser outro recorte (7 dias, 90 dias, ano corrente), isso vira uma issue nova de verdade (parâmetro de período), não uma extensão silenciosa desta.

## 2026-08-02 — Top plataformas limitado a 5, via `Pageable` na query

**Ação:** `countGroupedByPlataforma` recebe um `Pageable` (`PageRequest.of(0, 5)`) para aplicar `LIMIT` direto na query SQL, em vez de truncar a lista em Java depois de buscar tudo.

**Motivo:** "Top" já sugere um recorte, e 5 é um tamanho comum de gráfico de barras/ranking em dashboards. Aplicar o limite na query evita trazer plataformas irrelevantes (com 1 candidatura cada) do banco à toa.

**Trade-off:** Nenhum documento fixa o número 5 — é uma escolha de UX razoável, documentada aqui para não parecer arbitrária depois. Fácil de ajustar (é uma constante no `DashboardService`) se o usuário preferir outro valor ao usar o dashboard de verdade.

---

## Issue #9 — Autenticação (Spring Security + JWT)

## 2026-08-02 — Usuário único hardcoded via env var (`ADMIN_USERNAME`/`ADMIN_PASSWORD_HASH`), sem entidade `User`/tabela

**Ação:** Não existe `User` entity, `UserRepository` nem endpoint de registro. As credenciais do único usuário vêm de `admin.username`/`admin.password-hash`, resolvidos via `@Value` a partir de variáveis de ambiente.

**Motivo:** A arquitetura (seção 6) é explícita: "Login único, sem cadastro público de usuários na v1". Diferente do `rest-api` (multi-usuário, com `User`/`Role`/registro), o Horizon não tem — e nunca vai ter na v1 — mais de um usuário. Modelar uma tabela `users` para guardar exatamente uma linha, que nunca é criada por um endpoint público, seria complexidade sem função: não há CRUD de usuário para justificar persistência.

**Trade-off:** Trocar a senha exige gerar um novo hash e reiniciar a aplicação com uma nova env var — não tem "esqueci minha senha" nem troca em runtime. Aceitável para uso pessoal single-user; se um dia precisar de multi-usuário (fora do escopo declarado da v1), essa decisão inteira é revisitada, não só ajustada.

## 2026-08-02 — Hash BCrypt em vez de senha em texto puro na env var, mesmo sendo local/pessoal

**Ação:** `ADMIN_PASSWORD_HASH` guarda um hash BCrypt (`passwordEncoder.matches(raw, hash)`), não a senha puro.

**Motivo:** A arquitetura justifica JWT aqui "mesmo sendo uso pessoal/single-user" para "manter consistência de portfólio" — o mesmo raciocínio vale para usar `BCryptPasswordEncoder` como no `rest-api`, em vez de comparar string direto. É o padrão que qualquer revisor de portfólio espera ver, e o custo extra (gerar o hash uma vez) é mínimo.

**Trade-off:** Fica mais chato gerar/trocar a senha (precisa rodar um comando pra gerar o hash) do que só editar uma env var em texto puro — documentei o passo a passo completo no README (via `jshell` + jars já baixados pelo Maven, sem depender de ferramenta externa nova).

## 2026-08-02 — Bug real encontrado na validação: `$` do hash BCrypt sendo corrompido pelo `docker compose` no `.env`

**Ação:** Documentado no `.env.example` e no README que todo `$` do hash precisa virar `$$` no arquivo `.env`.

**Motivo:** `docker compose` interpola `$VAR`/`${VAR}` dentro do `.env` do mesmo jeito que interpola dentro do `docker-compose.yml` — um hash como `$2a$10$y/iF9...` tem `$2a`, `$10` e `$y` tratados como referências de variável. `$y` (variável inexistente) virou string vazia silenciosamente, corrompendo o hash sem erro nenhum — o login simplesmente passou a falhar com "credenciais inválidas" mesmo com a senha certa. Só percebi porque vi o warning `The "y" variable is not set` no `docker compose ps` e fui investigar; sem prestar atenção nesse warning, o bug teria sido bem mais difícil de diagnosticar (parece erro de senha, não de parsing de arquivo).

**Trade-off:** Nenhum — é puramente documentação de uma armadilha real do Docker Compose para não repetir o mesmo debug depois. Validado end-to-end depois da correção: `POST /api/v1/auth/login` com a senha certa retornou o token, e o mesmo token autenticou `GET`/`POST` em `/api/v1/vagas` e `GET /api/v1/dashboard/metrics`.

## 2026-08-02 — `SecurityFilterChain` protege só `/api/v1/vagas/**` e `/api/v1/dashboard/**`; todo o resto é `permitAll`

**Ação:** `.requestMatchers("/api/v1/vagas/**", "/api/v1/dashboard/**").authenticated()` seguido de `.anyRequest().permitAll()` — não um `anyRequest().authenticated()` com exceções.

**Motivo:** É literalmente o escopo da issue ("Proteger os endpoints de `/api/v1/vagas/**` e `/api/v1/dashboard/**`"). Deixar tudo mais permissivo por padrão evita que a issue #10 (Swagger UI) precise editar o `SecurityConfig` de novo só para liberar `/swagger-ui/**`/`/v3/api-docs/**` — esses paths já vão funcionar sem token assim que existirem.

**Trade-off:** Qualquer endpoint novo fora de `vagas`/`dashboard` nasce público por padrão, a menos que alguém lembre de adicionar ao `SecurityConfig` — risco baixo aqui porque o único outro grupo de rotas é `/api/v1/auth/**` (que precisa mesmo ser público) e o futuro Swagger (também deve ser público). Se o projeto crescer com mais recursos protegidos, vale reconsiderar para deny-by-default.

## 2026-08-02 — Sem endpoint de logout/refresh

**Ação:** Só existe `POST /api/v1/auth/login`. Não há blacklist de token, refresh token, nem logout no servidor.

**Motivo:** JWT stateless não tem "sessão" no servidor para invalidar — logout de verdade exigiria uma blacklist (Redis ou tabela), infraestrutura que nenhum dos dois documentos pede. `jwt.expiration` (24h, herdado do `rest-api`) já limita a janela de um token vazado.

**Trade-off:** Um token vazado continua válido até expirar, sem forma de revogá-lo antes da hora — risco aceito dado o contexto (uso pessoal, token nunca sai da máquina do próprio usuário). Se isso importar no futuro, é uma feature nova (blacklist), não um ajuste desta issue.

---

## Issue #10 — Validação manual via Swagger UI (gate antes do frontend)

## 2026-08-03 — `OpenApiConfig` no pacote `security`, não um pacote `config` novo

**Ação:** `OpenApiConfig.java` (título/descrição da API + esquema `bearerAuth` para o botão Authorize) foi colocado em `com.iankyoo.horizon.security`, não em um pacote `config` separado.

**Motivo:** A estrutura de pastas fechada na arquitetura (seção 3) não prevê um pacote `config` — só `security` entre os candidatos razoáveis. O conteúdo do bean é quase todo sobre declarar o esquema de autenticação Bearer JWT do Swagger, o que é diretamente acoplado ao que já existe em `security` (`JwtService`, `SecurityConfig`), então reaproveitar o pacote existente evitou inventar uma pasta nova para uma única classe.

**Trade-off:** Se o projeto crescer com mais configuração não-relacionada a segurança (ex: config de CORS, de cache), pode valer revisitar e criar um pacote `config` de verdade — não fiz isso agora para não adiantar estrutura que nenhuma issue pede ainda.

## 2026-08-03 — Nenhuma mudança no `SecurityConfig` para liberar o Swagger UI

**Ação:** `/swagger-ui/**` e `/v3/api-docs/**` funcionam sem token, sem precisar tocar em `SecurityConfig`.

**Motivo:** Decisão já tomada (e documentada) na issue #9: o `SecurityFilterChain` protege só `/api/v1/vagas/**` e `/api/v1/dashboard/**`, com `anyRequest().permitAll()` como fallback — exatamente pensando nesta issue. Confirmado na prática: `curl http://localhost:8081/v3/api-docs` e `/swagger-ui/index.html` responderam `200` sem `Authorization` header.

**Trade-off:** Nenhum novo — é a decisão da #9 se pagando aqui.

## 2026-08-03 — Checklist de aceite validado via `curl` roteirizado, não clicando manualmente na UI

**Ação:** A validação "manual" do PRD (seção 8) foi executada como um script `curl` sequencial cobrindo o fluxo completo: login → criar vaga → listar (sem filtro e com filtro) → detalhe (1 evento de histórico) → mudar status duas vezes → detalhe de novo (3 eventos, `statusAtual` correto) → arquivar → listar (some) → dashboard (continua contando). Todos os passos confirmados com o resultado esperado.

**Motivo:** Não há uma ferramenta de automação de navegador disponível neste ambiente para clicar de fato nos botões do Swagger UI. O objetivo real da issue — confirmado em `docs/prd.md` seção 8 como critério de sucesso ("CRUD de vaga funcionando ponta a ponta, com histórico de status persistido") — é sobre o **comportamento da API**, não sobre o ato de clicar na interface do Swagger especificamente. `curl` contra os mesmos endpoints documentados no Swagger exercita exatamente o mesmo contrato HTTP. A parte que só a UI do Swagger cobre (o parser do OpenAPI conseguindo gerar a página, os schemas renderizando, o botão Authorize funcionando) foi validada separadamente confirmando que `/v3/api-docs` retorna um documento OpenAPI válido listando os 7 endpoints esperados e que `/swagger-ui/index.html` carrega com `200`.

**Trade-off:** Não fica validado visualmente se a UI do Swagger renderiza cada schema/formulário de forma legível (só que ela carrega e o JSON do OpenAPI é válido) — um problema puramente cosmético de renderização do Swagger UI poderia passar despercebido. Baixo risco: `springdoc-openapi-starter-webmvc-ui` é a biblioteca padrão de mercado, gera schemas automaticamente a partir dos DTOs/`record`s já existentes, sem nenhuma customização manual de schema que pudesse quebrar.

---

## Issue #11/#12 — Setup do frontend + página /vagas (com sistema de design deliberado)

## 2026-08-03 — Sistema de design próprio ("faixa-horizonte") em vez de UI genérica de dashboard

**Ação:** Antes de codar, defini uma direção de design específica para o Horizon: paleta fria "papel de razão" (não o cream+serif+terracota nem o near-black+neon que são os defaults genéricos de UI gerada por IA), tipografia Fraunces (display) + IBM Plex Sans (corpo) + IBM Plex Mono (dados), e um elemento-assinatura — uma faixa em gradiente (`dawn → twilight → amber → gold`) onde a posição de cada estágio no gradiente É a distribuição por status (a mesma métrica que o dashboard, issue #14, vai mostrar formalmente). REJEITADO fica deliberadamente fora do gradiente, em `slate`, reforçando "saiu do funil".

**Motivo:** O nome do projeto ("Horizon") e o domínio (funil `aplicado→triagem→entrevista→oferta/rejeitado`) davam um motivo real e específico para esse motivo visual — não é decoração genérica, é a distribuição por status virando visualização funcional, reaproveitada como header persistente em todo o app. Também é a única página onde "gastei" a ousadia visual (o resto — tabela, formulário, login — fica deliberadamente quieto/utilitário).

**Trade-off:** Mais tempo investido em decisão de design do que uma UI com componentes prontos (ex: shadcn/ui) exigiria — aceitável porque o projeto é peça de portfólio, não só ferramenta interna, e o usuário pediu explicitamente uma direção de design intencional (skill `frontend-design`) em vez do resultado padrão.

## 2026-08-03 — Tela de login e CORS: dois gaps reais que nenhuma issue original cobria

**Ação:** Adicionei `app/login/page.tsx` no frontend e `CorsConfigurationSource` no backend (`SecurityConfig`, commit `7b08d5c`), nenhum dos dois pedido explicitamente por issue nenhuma.

**Motivo:** Descobertos por necessidade, não por escolha — sem login, nenhuma chamada a `/api/v1/vagas`/`/dashboard` teria token (a API é JWT desde a issue #9); sem CORS, o navegador bloqueava toda chamada de `localhost:3000` para `localhost:8081` mesmo com token (erro de preflight sem `Access-Control-Allow-Origin`, descoberto rodando o fluxo de login de verdade via Playwright — a tela carregava, mas o `fetch` falhava silenciosamente do lado do usuário). Ambos bloqueadores totais: sem eles, zero funcionalidade do frontend funciona.

**Trade-off:** Nenhum — eram pré-requisitos, não features novas. Documentando aqui porque é o tipo de gap que só aparece quando se integra de ponta a ponta, não lendo os docs isoladamente — reforça o valor de validar com o app rodando de verdade (como fiz em todas as issues do backend) em vez de só revisar código.

## 2026-08-03 — Sem tabela `User`; sem gerenciamento de estado global (Redux/Zustand); fetch client-side simples

**Ação:** `lib/api.ts` é só funções `fetch` centralizadas (uma por endpoint, como o doc de arquitetura pede) com token em `localStorage`; sem React Query/SWR, sem Context de auth, sem middleware do Next.

**Motivo:** `docs/arquitetura.md` seção 7 pede explicitamente exatamente isso: "usar fetch simples client-side... evitar padrões avançados de Next... reduzir a quantidade de conceitos novos de frontend de uma vez" — o nível de frontend do usuário ainda é básico. A ambição de design (cores/tipografia/layout) não conflita com engenharia simples (fetch + useState/useEffect) — são eixos independentes.

**Trade-off:** Cada página refaz sua própria lógica de loading/erro em vez de um hook compartilhado (`useVagas`, etc.) — repetição pequena e aceitável no tamanho atual do app; se crescer, extrair hooks é um refactor isolado, não uma mudança de arquitetura.

## 2026-08-03 — Validação visual com Playwright avulso (não `chromium-cli`), instalado só no scratchpad

**Ação:** Como `chromium-cli` não está disponível neste ambiente, usei `playwright` via `npx` instalado num projeto descartável no diretório de scratchpad (não como dependência do `frontend/`) para dirigir um Chromium headless: screenshot do login, login de verdade (preenche+submete), screenshot de `/vagas` com dados reais, criação de vaga pela UI (não só via `curl`), conferindo consumo real da API pelo browser.

**Motivo:** É a única forma de "ver" o resultado visual real (cores, fontes, layout renderizado) neste ambiente sem GUI. Rodar só `curl`/testes de tipo não prova que o design funciona; rodar com browser real pegou dois bugs reais que análise estática não pegaria: o CORS (acima) e um gradiente incompleto (Tailwind `from/via/to` só suporta 3 stops; o conceito da faixa-horizonte precisa de 4 — corrigido com `linear-gradient` arbitrário via `var(--color-*)`).

**Trade-off:** Instalar Playwright + baixar o binário do Chromium tem custo (tempo, ~300MB) só para validação, não faz parte do app — por isso fica isolado no scratchpad, nunca vira dependência do projeto. Um artefato visual (fringing de antialiasing subpixel do Chromium headless) inicialmente pareceu um bug de CSS real; precisei validar com `--disable-lcd-text` e DPI maior para confirmar que era só um artefato de captura, não o app — documentando para não reinvestigar da próxima vez.

## 2026-08-03 — `react-hooks/set-state-in-effect` suprimido em dois pontos específicos, com comentário

**Ação:** `Header.tsx` (checar token no `localStorage` pós-montagem) e `app/vagas/page.tsx` (disparar o fetch inicial autenticado) têm `// eslint-disable-next-line react-hooks/set-state-in-effect` com justificativa inline, em vez de reestruturar para `useSyncExternalStore` ou mover a leitura para o corpo do componente.

**Motivo:** Essa regra do ESLint (nova no `eslint-config-next` para Next 16/React 19) é uma heurística útil no caso geral, mas os dois casos aqui são exatamente o cenário em que `useEffect` + `setState` é o padrão correto e recomendado pelo próprio React: ler uma API só-de-browser (`localStorage`) que não existe no SSR. Ler direto no corpo do componente (inicializador `useState(() => getToken())`) pareceria mais "limpo" para o linter, mas causaria divergência de hidratação real (servidor renderiza deslogado, cliente hidrata com valor diferente) — pior que o aviso do linter. `useSyncExternalStore` resolveria sem o aviso, mas é exatamente o tipo de "padrão avançado" que a arquitetura pede pra evitar nesta fase do frontend.

**Trade-off:** Duas supressões de lint no código — aceitável porque cada uma tem uma linha de comentário explicando o motivo, então não fica "silenciada sem explicação" para quem ler depois.

---

## Issue #13 — Página `/vagas/[id]` (detalhe + histórico + mudança de status)

## 2026-08-03 — `useParams()` em vez de `use(params)`/`params: Promise<...>`

**Ação:** `app/vagas/[id]/page.tsx` lê o `id` da rota via `useParams<{ id: string }>()` do `next/navigation`, não recebendo `params` como prop assíncrona.

**Motivo:** Conferido em `node_modules/next/dist/docs/01-app/.../dynamic-routes.md` (aviso do `AGENTS.md` do frontend: essa versão do Next tem breaking changes vs. o treino do modelo) — em Client Components, `params` como prop é uma `Promise` e precisa de `use()`; a alternativa documentada para Client Components é exatamente `useParams()`, mais simples e sem precisar tornar o componente compatível com `Suspense`/streaming. Como a página já é `"use client"` (precisa de `useState`/formulário interativo para mudar status), `useParams()` evita complexidade extra sem trade-off real.

**Trade-off:** Nenhum — é a forma recomendada pela própria doc para o caso de Client Component.

## 2026-08-03 — Formulário de mudança de status é uma ação separada da vaga, não edição inline da tabela

**Ação:** A mudança de status vive só na página de detalhe (`select` com todos os 5 status + campo de observação opcional), disparando `PATCH /api/v1/vagas/{id}/status` e re-buscando o detalhe completo (`GET /api/v1/vagas/{id}`) para atualizar histórico e status atual na mesma resposta.

**Motivo:** O backend não valida transição de estado (decisão já tomada na issue #7 — status livre, sem máquina de estado), então a UI segue o mesmo princípio: qualquer status pode ser escolhido a qualquer momento, sem lista de "próximos status válidos" calculada no frontend. Re-buscar o detalhe inteiro após o PATCH (em vez de só atualizar o `statusAtual` no estado local a partir da resposta do `PATCH`, que é um `VagaResponse` sem histórico) garante que o novo evento do histórico apareça sem lógica duplicada de merge no cliente.

**Trade-off:** Uma requisição HTTP a mais por mudança de status (`PATCH` + `GET`) em vez de só atualizar o estado local — aceitável dado o volume de uso (ferramenta pessoal, não alto tráfego) e evita manter duas fontes de verdade (resposta do PATCH vs. GET) sincronizadas manualmente.

## 2026-08-03 — Sem botão de excluir na página de detalhe

**Ação:** A página de detalhe não expõe `DELETE /api/v1/vagas/{id}` (arquivar), mesmo o endpoint já existindo desde a issue #8.

**Motivo:** Fora do escopo declarado da issue #13 (`docs/prd.md` seção 5 item 2: "detalhe + histórico + mudança de status"). Adicionar mais uma ação não pedida infla a página sem necessidade real agora.

**Trade-off:** Se o usuário quiser arquivar uma vaga pela UI, precisa esperar uma issue futura (não coberta nas #11-#15 originais) ou usar a API diretamente — aceitável porque nenhuma das 15 issues do MVP pede essa ação na UI.

---

## Issue #14 — Página `/dashboard` (métricas com Chart.js)

## 2026-08-03 — `react-chartjs-2` (wrapper React) em vez de `chart.js` puro com `useRef`/`useEffect` manual

**Ação:** Instalei `chart.js` + `react-chartjs-2` e criei um único componente `components/BarChart.tsx` reutilizado nas 3 visualizações em barra (distribuição por status, tempo médio por etapa, top plataformas), em vez de manipular um `<canvas>` via `useRef` diretamente.

**Motivo:** `docs/arquitetura.md` seção 7 pede explicitamente "reduzir a quantidade de conceitos novos de frontend de uma vez". Gerenciar o ciclo de vida de uma instância `Chart.js` manualmente (criar no mount, destruir no unmount, atualizar dados sem vazar memória) é exatamente o tipo de detalhe de baixo nível que o wrapper resolve, deixando o componente declarativo (props `labels`/`data`/`colors` → JSX), consistente com o resto do app que já é 100% componentes funcionais simples.

**Trade-off:** Mais uma dependência no `package.json` (que carrega o próprio `chart.js` como peer) só para evitar ~15 linhas de gerência manual de ciclo de vida — aceitável dado que é a biblioteca de gráficos que a própria stack técnica do PRD (seção 7) pede nominalmente.

## 2026-08-03 — Paleta duplicada em hex literal (`STATUS_HEX`) só para o Chart.js

**Ação:** `lib/status.ts` ganhou um terceiro record, `STATUS_HEX`, com os mesmos 5 valores hex já definidos em `globals.css` (`--color-dawn` etc.), usado só para colorir as barras dos gráficos.

**Motivo:** Chart.js desenha em `<canvas>`, que não entende classes Tailwind (`bg-dawn`) — precisa de uma string de cor CSS de verdade (`#2e2a6b`) passada em JS. Não existe forma direta de "ler" o valor resolvido de uma custom property do Tailwind a partir do bundle JS sem uma chamada a `getComputedStyle` em runtime (mais uma camada de indireção por nenhum ganho real, já que os hex já são fixos e conhecidos em build-time).

**Trade-off:** Se a paleta em `globals.css` mudar no futuro, `STATUS_HEX` precisa ser atualizado manualmente em paralelo — risco baixo porque as duas cópias vivem no mesmo arquivo `lib/status.ts` (ao lado de `STATUS_COR`/`STATUS_TEXTO`, já duplicados por natureza — um record por formato de consumo), então divergência é fácil de notar e corrigir.

## 2026-08-03 — Taxa de conversão como 3 cards numéricos, não gráfico de barras

**Ação:** As 3 taxas de conversão (`aplicadoParaTriagem`, `triagemParaEntrevista`, `entrevistaParaOferta`) aparecem como cards de porcentagem grande (estilo "stat"), não como mais um `BarChart`.

**Motivo:** `docs/prd.md` seção 6 pede gráfico de barras explicitamente só para "distribuição por status atual" — para taxa de conversão, só diz "ex: % de aplicações que viraram triagem". Três valores percentuais isolados (sem relação de posição num eixo comum, cada um é uma razão entre duas etapas diferentes) comunicam mais rápido como número grande do que como barras de alturas parecidas sem uma escala compartilhada óbvia.

**Trade-off:** Nenhum gráfico ali quebra a expectativa de "todo dashboard tem só gráficos" — aceitável porque a página já tem 3 gráficos de barra (status, tempo médio, plataformas) cobrindo o requisito da stack técnica (Chart.js) e o restante do app já é deliberadamente quieto/tabular (decisão da issue #11/#12).

## 2026-08-03 — Dados de teste criados via `curl` roteirizado para validar os 3 gráficos com dado real, não só com zeros

**Ação:** Antes do screenshot final, populei o banco (volume descartável, removido depois com `docker compose down -v`) com 6 vagas em estágios diferentes do funil (uma até OFERTA, uma até REJEITADO, uma parada em TRIAGEM, duas só APLICADO) via `curl` roteirizado, em vez de validar a página só com o estado vazio.

**Motivo:** Com o banco vazio ou com só 1 vaga (estado deixado pela issue #13), a maioria das barras ficaria em zero e não daria pra confirmar visualmente se as cores por status, a orientação horizontal do gráfico de plataformas e a legenda dos eixos estavam corretas. Validado via Playwright: screenshot mostra as 5 métricas com dados não-triviais (`distribuicaoPorStatus` com todos os 5 status representados, `topPlataformas` com Gupy/LinkedIn em contagens diferentes).

**Trade-off:** Mais um passo de setup manual antes de cada validação visual de dashboard — aceitável porque é o único jeito de ver o gráfico "de verdade" (com barras de tamanhos diferentes) em vez de confiar que ele vai renderizar certo com dado real só porque rendeizou com zeros.

---

## Issue #15 — Publicar v1 (fechar critérios de sucesso do MVP)

## 2026-08-03 — README raiz estava desatualizado e foi corrigido antes de fechar o checklist

**Ação:** Reescrevi partes do `README.md` raiz: seção "Status" dizia "v1 ainda não iniciada" (texto do dia 1 do projeto, nunca atualizado); "Ordem de execução" mencionava "regras de transição de status" como se existisse uma máquina de estado — decisão revertida na issue #6/#7 (status é livre, sem validação de transição) e nunca corrigida no README. Adicionei também a seção "Como rodar (frontend)" (existia só no `frontend/README.md`, não linkada/resumida na raiz) e uma seção "Páginas" listando as 4 rotas do app.

**Motivo:** O critério de sucesso da issue #15 (`docs/prd.md` seção 8) exige "repo documentado, README... instruções claras de execução local" — um README que descreve uma feature que não existe (máquina de estado) e omite como rodar metade do projeto (frontend) não cumpre isso, mesmo com o código funcionando. Só percebi a divergência lendo o arquivo de novo com o projeto inteiro pronto, não durante o desenvolvimento incremental (cada issue mexeu no código, não necessariamente no README raiz).

**Trade-off:** Nenhum — é correção de documentação, sem impacto em código.

## 2026-08-03 — Checklist de aceite fechado com validação ponta a ponta nova (não reaproveitando só os testes das issues #12-#14)

**Ação:** Rodei os 4 itens do checklist da issue #15 contra um ambiente limpo (`docker compose up --build` com volume novo): (1) roteiro `curl` cobrindo login→criar→listar→filtrar→detalhe→2x mudança de status→detalhe→arquivar→listar (confirma exclusão)→dashboard (confirma que a vaga arquivada continua contando nas métricas); (2) fluxo Playwright tocando as 4 páginas do frontend em sequência (login→criar vaga pela UI→detalhe→mudar status→dashboard) sem nenhum erro de console; (3) `grep` no código-fonte por palavras-chave das features fora de escopo (e-mail, parsing, notificação, multi-tenant) confirmando zero ocorrências; (4) `gh repo view` confirmando visibilidade `PUBLIC`.

**Motivo:** Reaproveitar só os testes já feitos nas issues #12-#14 verificaria cada peça isoladamente, mas não garante que elas continuam funcionando *juntas* depois de mudanças subsequentes (ex: dependências novas do Chart.js, edições no `lib/status.ts`). Rodar tudo de novo do zero, numa sessão só, é o que realmente valida "v1 pronta para publicar" — que é sobre o sistema como um todo, não sobre issues isoladas.

**Trade-off:** Achei um bug de ambiente (não de código) no processo: a primeira tentativa do fluxo Playwright deu timeout no login porque o Turbopack compila rotas sob demanda no primeiro acesso, e o clique no botão aconteceu antes da rota `/vagas` terminar de compilar+navegar dentro do timeout padrão. Confirmado como artefato de dev server (não reproduz em build de produção, onde tudo já vem compilado) rodando a mesma rotina de novo com as rotas já "aquecidas" — funcionou de primeira. Documentando para não reinvestigar: se um teste Playwright der timeout de navegação na primeira execução contra `next dev`, tentar de novo antes de assumir bug de aplicação.

---

## Pós-v1 — README com screenshots

## 2026-08-03 — Screenshots do README capturados contra `next build && next start`, não `next dev`

**Ação:** As 5 capturas em `docs/screenshots/` foram geradas com Playwright rodando contra o build de produção do frontend, não o servidor de desenvolvimento.

**Motivo:** Uma primeira tentativa contra `next dev` (Turbopack) capturou o indicador de devtools do Next ("N" + badge de "1 Issue") no canto da tela, e uma segunda tentativa na sequência mostrou a tabela de vagas momentaneamente sem estilo Tailwind (texto azul sublinhado, como link não estilizado) — os dois artefatos de Fast Refresh/HMR recompilando em paralelo à navegação do Playwright, não bugs reais do app (confirmado: o build de produção, sem HMR, renderizou de forma estável e sem o indicador em todas as tentativas). Para documentação pública (README), a imagem precisa ser determinística — não vale a pena investigar/mitigar instabilidade do dev server quando `next build` já resolve isso de graça.

**Trade-off:** Um passo a mais (build) antes de gerar screenshots — irrelevante para o fluxo de desenvolvimento normal (`npm run dev` continua sendo o comando do dia a dia, só as capturas para documentação usam build).

## 2026-08-03 — Badges do README limitados a stack técnica (sem CI/licença)

**Ação:** Os badges no topo do `README.md` cobrem só versões de tecnologia (Java, Spring Boot, Next.js, React, TypeScript, PostgreSQL, Docker Compose), lidas diretamente do `pom.xml`/`package.json`/`docker-compose.yml`.

**Motivo:** Um badge é uma afirmação verificável — não faz sentido mostrar um badge de build/CI (não existe pipeline, decisão explícita da v1 documentada em `docs/arquitetura.md` seção 9: "Sem CI/CD na v1") nem de licença (não existe arquivo `LICENSE` no repo). Badges que prometem algo que não existe são pior que não ter badge nenhum.

**Trade-off:** Nenhum — se um `LICENSE` ou CI forem adicionados no futuro, é só adicionar o badge correspondente.

## 2026-08-03 — Dataset de 50 vagas gerado via SQL direto, não via API

**Ação:** Para enriquecer os screenshots (e popular de vez o gráfico "tempo médio até a próxima mudança", que estava sempre zerado), gerei 50 vagas com status/plataforma aleatórios e histórico de status com gaps de 1 a 45 dias entre mudanças, via `INSERT` direto nas tabelas `vaga`/`status_historico` (script Python gerando SQL, aplicado com `psql` dentro do container), em vez de criar via `POST /vagas` + `PATCH /vagas/{id}/status`.

**Motivo:** `data_criacao` (`Vaga`) e `data_mudanca` (`StatusHistorico`) usam `@CreationTimestamp` do Hibernate — sempre o instante em que a linha é inserida, sem nenhum campo na API para sobrescrever. Criar as 50 vagas via API produziria um histórico inteiro concentrado em poucos segundos (como aconteceu nos testes anteriores, issues #12-#15), tornando qualquer média de "dias até a próxima mudança" artificialmente 0. Só dá pra simular um histórico realista (candidaturas ao longo de meses, com dias de espera entre etapas) escrevendo os timestamps diretamente no banco.

**Trade-off:** Esse dataset só existe para fins de demonstração/screenshot — não passou pelas regras de validação da API (embora respeite as mesmas constraints do schema: enums válidos, FK, not-null). Aceitável porque é dado descartável, gerado numa massa de teste, não em produção; a lógica de negócio em si (Service/Controller) continua sendo exercitada e validada normalmente pelos testes de API das issues #3-#10, que não usam esse atalho.

---

## Pós-v1 — Testes unitários do backend

## 2026-08-03 — Testes unitários cobrindo `VagaService`, `DashboardService`, `AuthService`, `JwtService` e `GlobalExceptionHandler`, com Mockito puro (sem `@SpringBootTest`)

**Ação:** Adicionei 27 testes em `backend/src/test/java`, um por classe de serviço/segurança, usando `MockitoExtension` + mocks dos repositórios/dependências (nunca subindo o contexto do Spring ou um banco real). `DashboardServiceTest` é o mais extenso: reconstrói cenários de histórico de status manualmente (com `LocalDateTime` fixos) pra conferir os números exatos de taxa de conversão e tempo médio por etapa — a lógica mais arriscada do projeto por ter mais matemática (agrupamento por vaga, `Duration.between`, arredondamento).

**Motivo:** O projeto não tinha nenhum teste automatizado até agora — toda validação até aqui (issues #1-#15) foi manual, via `curl`/Playwright contra o app rodando de ponta a ponta. Isso prova que o sistema funciona *hoje*, mas não impede uma regressão silenciosa amanhã (ex: alguém mexe no agrupamento de `DashboardService` e o "tempo médio por etapa" volta a ficar sempre zero, como aconteceu de fato até o dataset de 50 vagas ser criado — só que dessa vez sem ninguém rodando o dashboard manualmente pra notar). Escolhi testes unitários (não `@SpringBootTest`/integração com banco real) porque: (1) são ordens de magnitude mais rápidos — a suíte inteira roda em ~2s, não minutos subindo Spring+Postgres; (2) isolam a lógica de negócio da infraestrutura — um teste de `DashboardService` não deveria falhar por causa de um problema de conexão com o Postgres; (3) o *risco* está concentrado na lógica pura (cálculo de médias/percentuais), não no mapeamento JPA em si, que é simples o bastante (sem queries nativas complexas) pra confiar no Spring Data.

**Trade-off:** Não cobre a fiação real do Spring (se o `@Autowired`/`@Bean` de algum componente está quebrado, os controllers mapeiam certo, o `SecurityFilterChain` bloqueia o que deveria) — esse tipo de erro só apareceria num teste de integração (`@SpringBootTest` com `MockMvc`) ou rodando a aplicação de verdade, que é exatamente o que as issues #1-#15 já fizeram manualmente. Como o projeto não tem CI (decisão da arquitetura, seção 9), esses testes também não rodam automaticamente a cada mudança — ficam disponíveis via `./mvnw test`, mas dependem de alguém lembrar de rodar.
