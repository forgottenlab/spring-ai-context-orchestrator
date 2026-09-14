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
- bilingual documentation indexes and purpose-based docs directories exist;
- the first three P1 JavaDoc batches document six Core pipeline SPI boundaries,
  six central domain concepts, and six pipeline data/result lifecycles in
  English and Simplified Chinese;
- Core remains one Maven module, with shared contracts in the root package and
  stage-specific types organized under `planning`, `execution`, `resolution`,
  `budget`, and `assembly`;
- public Markdown now reflects the verified Starter consumer contract, the
  118-test reactor baseline, the stage package structure, and the fact that the
  Quickstart remains a placeholder.

The Starter consumer E2E is verified GREEN. A consumer obtains the
auto-configured `ChatClient.Builder` through the Starter, ACO's Advisor is
applied automatically, the original system message is preserved, and the
consumer `ContextSource` fact reaches the final Prompt inside one
`<business-context>` envelope. The test uses a Fake `ChatModel` with no API key,
external model provider, or model network call.

The runnable Quickstart is not implemented; `examples/quickstart` remains
a placeholder.

After the package migration, `mvn clean test` is GREEN with Core 89, Spring AI
18, AutoConfiguration 10, and Starter 1 test.

## Ordered next tasks

Complete one task at a time and stop after each task:

1. Build a runnable Quickstart from the verified Starter consumer contract,
   without introducing a real API key or model network dependency into tests.
2. Dogfood the Starter in a real application and record concrete consumer
   friction before reconsidering Core.
3. Address remaining documentation or build hygiene in separate focused tasks.

## Verified Starter E2E boundary

The consumer test proves:

```text
Spring Boot consumer
  -> ai-context-spring-boot-starter
  -> ContextSource bean
  -> auto-configured ChatClient.Builder
  -> automatically registered ACO Advisor
  -> Fake/Stub ChatModel captures final Prompt
```

The final Prompt preserves the application's original system message, appends
`<business-context>`, and contains facts returned by the consumer's
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
- release, publishing, or deployment work.

## Validation discipline

For code changes, run the narrowest relevant test first, then the full Maven
reactor, `git diff --check`, and `git status`. Keep build hygiene, documentation,
JavaDoc, Starter E2E, and Core changes in separate tasks.
