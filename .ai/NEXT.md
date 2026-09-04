# Next

## Current stage

The foundation and consumer wiring baseline are established:

- the provider-neutral Core pipeline exists from `ContextRequest` through
  `ContextAssembler`;
- planning, concurrent execution, failure isolation, Authority/Freshness
  resolution, Priority-based budgeting, and structured assembly have focused
  tests;
- the Spring AI Advisor bridge preserves the existing system message and
  appends `<business-context>`;
- Spring Boot AutoConfiguration discovers `ContextSource` beans and creates the
  default orchestration graph;
- the dependency-only Starter module exists;
- bilingual documentation indexes and purpose-based docs directories exist.

The Starter consumer experience is not yet verified end to end. Existing
AutoConfiguration tests invoke the Advisor boundary directly; they do not yet
prove an auto-configured `ChatClient.Builder` call reaching a Fake/Stub
`ChatModel` with the final enriched Prompt.

The runnable Quickstart is also not implemented; `examples/quickstart` remains
a placeholder.

## Ordered next tasks

Complete one task at a time and stop after each task:

1. Add high-value bilingual JavaDoc to small, coherent groups of public API
   types without changing behavior.
2. Synchronize English and Chinese documentation with verified repository
   behavior and remove stale consumer-status wording.
3. Add a Starter consumer E2E using a Fake/Stub `ChatModel`, no API key, no
   external network, and an auto-configured `ChatClient.Builder`.
4. Build a runnable Quickstart only after the Starter consumer contract is
   proven.

## Starter E2E acceptance boundary

The consumer test must prove:

```text
Spring Boot consumer
  -> ai-context-spring-boot-starter
  -> ContextSource bean
  -> auto-configured ChatClient.Builder
  -> automatically registered ACO Advisor
  -> Fake/Stub ChatModel captures final Prompt
```

The final Prompt must preserve the application's original system message,
append `<business-context>`, and contain facts returned by the consumer's
`ContextSource`.

Do not manually construct the ACO pipeline or call a real model service. If the
test exposes a missing consumer contract, change only the smallest non-Core
surface needed to satisfy it.

## Deferred work

Do not start these items without a later explicit task and consumer evidence:

- YAML schema or annotation API;
- database, Redis, VectorStore, or ChatMemory adapters;
- Spring AI Alibaba compatibility;
- relevance/cost frameworks or other new Core abstractions;
- public package restructuring;
- release, publishing, or deployment work.

## Validation discipline

For code changes, run the narrowest relevant test first, then the full Maven
reactor, `git diff --check`, and `git status`. Keep build hygiene, documentation,
JavaDoc, Starter E2E, and Core changes in separate tasks.
