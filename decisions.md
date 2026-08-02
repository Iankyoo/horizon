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
