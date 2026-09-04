# AGENTS.md

## 1. Project identity

This repository is **AI Context Orchestrator (ACO)**.

ACO is a declarative business-context orchestration layer for Spring AI.

Core idea:

> Application code declares which business context may be needed.  
> ACO plans, loads, resolves, budgets, assembles, and injects that context into Spring AI.

This is a framework/library project, not a normal CRUD application.

---

## 2. Read before modifying

Before making changes, inspect the repository instead of relying on assumptions.

At minimum read:

- `README.md`
- `README.zh-CN.md`
- `docs/README.md` and/or `docs/README.zh-CN.md` if present
- the relevant documents under `docs/`
- `.ai/CONTEXT.md`
- `.ai/NEXT.md`
- `.ai/SAFETY.md`
- root `pom.xml`
- the relevant module `pom.xml`
- relevant production code
- relevant tests

Also run:

```powershell
git status
git log --oneline --decorate -n 12
```

If the working tree is not clean, report the existing changes before writing anything.

Do not discard, restore, overwrite, or reformat unknown user changes.

---

## 3. Architecture map

The core runtime pipeline is:

```text
ContextRequest
    ↓
ContextPlanner
    ↓
ContextExecutor
    ↓
ContextResolver
    ↓
ContextBudgeter
    ↓
ContextAssembler
    ↓
ContextOrchestrationAdvisor
    ↓
Spring AI
```

Keep these boundaries explicit.

### Planner

Decides **what should be loaded**.

- may inspect request and source metadata
- must not perform source I/O
- must not resolve conflicting facts
- must not apply budget rules

### Executor

Executes selected `ContextSource` instances.

- handles loading mechanics
- isolates source failures
- respects execution policy
- does not decide Authority
- does not apply Budget

### Resolver

Determines which conflicting facts should be trusted.

- handles `ContextKey`
- handles Authority
- handles Freshness / expiration
- does not use Priority for factual trust

### Budgeter

Determines which already-resolved context survives budget pressure.

- Required context is protected first
- optional context is selected by Priority
- Authority and Priority must remain separate concepts

### Assembler

Builds structured model-facing context.

Keep typed values structured for as long as possible.
Render them only near the model boundary.

---

## 4. Module boundaries

### `ai-context-core`

Provider-neutral domain model and SPI.

Rules:

- must not depend on Spring AI
- must not become a persistence framework
- must not contain application-specific business logic

### `ai-context-spring-ai`

Spring AI bridge.

Responsibilities include:

- adapting Spring AI requests into ACO requests
- running orchestration through the advisor boundary
- preserving the application's existing system message
- appending structured business context

### `ai-context-spring-boot-autoconfigure`

Spring Boot default wiring.

Responsibilities include:

- discovering consumer `ContextSource` beans
- creating replaceable defaults
- wiring the orchestration advisor
- integrating with the auto-configured `ChatClient.Builder`

Do not expose ACO's private execution infrastructure as a generic application `Executor` bean.

### `ai-context-spring-boot-starter`

Consumer dependency aggregator.

It is normal for this module to contain little or no Java source.

Do not add implementation classes here merely to make the module look substantial.

### `examples`

Consumer-facing runnable examples.

Prefer examples that demonstrate how a normal application uses ACO rather than manually assembling internal framework components.

---

## 5. Core stability rule

Treat Core as **frozen by default**.

Do not add new Core abstractions merely because they appear architecturally interesting.

Examples of changes that require concrete consumer evidence:

- new strategy hierarchies
- policy engines
- context graphs
- relevance scoring frameworks
- annotation systems
- unrestricted NL2SQL abstractions
- public package restructuring

Only modify Core when a real Starter / Quickstart / consumer scenario demonstrates a missing capability or incorrect contract.

When proposing a Core change, explain:

1. the consumer problem;
2. why the current API cannot solve it cleanly;
3. the smallest compatible change;
4. affected public API;
5. migration risk.

---

## 6. Current development priority

Use `.ai/NEXT.md` as the current milestone source of truth.

Unless `.ai/NEXT.md` says otherwise, prefer this order:

1. repository readability and documentation;
2. high-value public API comments;
3. Starter consumer E2E;
4. runnable Quickstart;
5. minimal consumer configuration;
6. build/release hygiene;
7. only then reconsider deeper Core changes.

Do not silently move to the next milestone after completing the current one.

---

## 7. JavaDoc and source-comment policy

Use JavaDoc comments where they improve understanding in IDEA and source code.

Important public API documentation should be **bilingual: English + Simplified Chinese**.

Good JavaDoc explains:

- why the type exists;
- what it owns;
- what it deliberately does not own;
- its boundary with adjacent stages;
- when a consumer should implement or replace it;
- non-obvious invariants.

Example:

```java
/**
 * Resolves competing context candidates into trusted facts.
 * 将相互竞争的上下文候选项解析为最终可信事实。
 *
 * <p>
 * Authority and freshness belong to this stage; Priority intentionally does not.
 * Authority 与 Freshness 属于本阶段；Priority 有意留给后续 Budget 阶段。
 * </p>
 */
```

Do not add low-value comments such as:

```java
/** Gets the id. / 获取 id。 */
```

Do not add historical metadata tags such as:

- `@author`
- creation date
- last modified date
- version history
- custom `@tag` fields

Git already tracks authorship and history.

Do not build or configure a JavaDoc HTML site unless explicitly requested.

### JavaDoc change discipline

Do not mass-document the entire repository in one pass.

Work in small coherent groups, normally 3-6 related public types per task.

Do not change runtime behavior while performing a JavaDoc-only task.

---

## 8. Documentation policy

Follow the ForgottenLab bilingual documentation convention.

Repository entry points:

- `README.md`: English-first
- `README.zh-CN.md`: Simplified Chinese
- both should provide language-switch links

Detailed documentation belongs under `docs/`.

Prefer purpose-based documentation categories such as:

- architecture
- guides
- project/testing/roadmap/safety

Follow the repository's existing docs indexes instead of inventing duplicate structures.

Markdown rules:

- use clear emoji section headings where consistent with existing docs;
- fenced code blocks must specify an appropriate language when applicable;
- use tables for comparisons and status summaries;
- Chinese documentation should read naturally, not as mechanical translation;
- explain why important commands are run;
- document limitations and safety boundaries.

When moving documentation files, update all affected links and check for stale paths.

---

## 9. Java package organization

Do not confuse "too many files in one folder" with permission to change public Java packages.

Moving classes from:

```text
io.github.forgottenlab.aicontext.core
```

into new public subpackages is an API restructuring task.

Do not perform public package moves as part of:

- documentation cleanup;
- JavaDoc work;
- Starter work;
- formatting cleanup.

If package restructuring becomes necessary, design and review it as a separate migration.

---

## 10. Starter evolution rules

The next meaningful Starter milestone should validate the **consumer experience**, not add more abstractions.

Target consumer flow:

```text
Spring Boot consumer
    ↓
depends on ai-context-spring-boot-starter
    ↓
defines a ContextSource bean
    ↓
uses auto-configured ChatClient.Builder
    ↓
ACO advisor runs automatically
    ↓
Fake / Stub ChatModel captures final Prompt
    ↓
existing system message is preserved
    ↓
<business-context> is appended
    ↓
business facts from ContextSource are present
```

Starter E2E requirements:

- no real OpenAI API call;
- no API key;
- no external network dependency;
- use a fake/stub ChatModel;
- test from a consumer perspective;
- do not manually construct the full ACO pipeline;
- do not bypass AutoConfiguration merely to make the test pass.

If the E2E fails, first establish the exact failing consumer contract.
Then make the smallest production change necessary.

---

## 11. Test discipline

Prefer this sequence:

1. inspect existing tests;
2. run the narrowest relevant test;
3. make the smallest change;
4. rerun focused tests;
5. run the full reactor;
6. run whitespace checks.

Before declaring a code task complete, run:

```powershell
git diff --check
mvn test
git status
```

Known warnings must be reported separately from actual failures.

Do not:

- delete tests to get green;
- weaken assertions without justification;
- skip tests to claim success;
- change tests and production behavior simultaneously unless the task genuinely requires both.

For bug fixes, prefer a failing regression test before the fix when practical.

---

## 12. Change discipline

One concern per task and preferably one concern per commit.

Do not mix unrelated work such as:

- architecture refactoring;
- JavaDoc;
- docs organization;
- Starter E2E;
- build-warning cleanup;
- public package movement;
- new features.

Before modifying files, report:

1. current problem;
2. observed evidence;
3. proposed files to change;
4. why those files are sufficient;
5. risk;
6. verification plan.

After modifying files, report:

1. files changed;
2. behavior changed or unchanged;
3. focused test result;
4. full test result;
5. `git diff --check` result;
6. important diff summary;
7. known non-blocking warnings;
8. suggested Conventional Commit message;
9. one recommended next step.

Do not automatically start that next step.

---

## 13. Git safety

Unless the user explicitly asks, do not:

- `git push`
- create or push tags
- create releases
- merge to `main`
- rewrite history
- run `git reset --hard`
- run destructive cleanup commands
- discard unknown local changes

Do not commit automatically unless the user explicitly authorizes the commit.

When suggesting commit messages, use lightweight Conventional Commits, for example:

```text
docs: add bilingual public API JavaDoc
test: establish starter consumer E2E baseline
fix: preserve consumer system message
feat: establish starter consumer workflow
```

---

## 14. Security and data boundaries

Do not introduce:

- real credentials into tests;
- unrestricted database execution;
- unrestricted NL2SQL;
- logging of sensitive raw business context by default.

Treat `<business-context>` as a model-facing structure that reduces instruction confusion, not as a complete prompt-injection defense.

Provider-side access control and application authorization remain application responsibilities.

---

## 15. Working style for Codex

For a new task:

1. read the relevant repository context;
2. verify the real baseline;
3. explain the plan;
4. make the smallest coherent change;
5. test;
6. review the diff;
7. stop.

Prefer evidence from repository code and tests over assumptions.

When repository documentation conflicts with implementation or tests, report the mismatch rather than silently choosing one.

When unsure whether a change affects public API, treat it as public until proven otherwise.

The goal is steady, reviewable progress—not maximum change per run.
