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
- public Markdown reflects the verified Starter consumer contract, the
  119-test reactor baseline, the stage package structure, and the runnable
  offline Quickstart;
- the external consumer fixture is GREEN against Spring AI 1.0.0 and 1.1.8,
  establishing 1.0.0 as the minimum and retaining 1.1.8 as the default;
- GitHub Actions covers reactor tests, packaging, the executable offline
  Quickstart, and both compatibility anchors;
- the authoritative ground-truth fail-closed boundary is documented as an
  application responsibility before `ChatClient` invocation, without changing
  Core failure-isolation or `REQUIRED` budget semantics.

The Starter consumer E2E is verified GREEN. A consumer obtains the
auto-configured `ChatClient.Builder` through the Starter, ACO's Advisor is
applied automatically, the original system message is preserved, and the
consumer `ContextSource` fact reaches the final Prompt inside one
`<business-context>` envelope. The test uses a Fake `ChatModel` with no API key,
external model provider, or model network call.

The runnable offline Quickstart is verified GREEN. Its only direct production
ACO dependency is the Starter; it supplies one application `ContextSource` and
an in-memory `ChatModel`, obtains the auto-configured `ChatClient.Builder`, and
prints the final enriched system message. The packaged non-web application
runs once and exits without an API key, model provider, or runtime network
access.

`mvn clean test` is GREEN with Core 89, Spring AI 18, AutoConfiguration 10,
Starter 1, and Quickstart 1 test (119 total).

The integration-readiness gates are GREEN. The standalone external-consumer
fixture adds one test per selected Spring AI anchor but remains outside the
reactor and does not change the 119-test baseline.

## Ordered next tasks

Complete one task at a time and stop after each task:

1. Dogfood the Starter in the Lingxi real application, enforcing authoritative
   Product validation before `ChatClient`, and record concrete consumer
   friction before reconsidering Core.
2. Address remaining documentation or build hygiene in separate focused tasks.

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
