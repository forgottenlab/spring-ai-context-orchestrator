# Project Context

## Current-state ownership

The tracked files in this directory have distinct responsibilities:

- `CONTEXT.md` records stable project and architecture facts.
- `NEXT.md` records the current development stage and ordered next tasks.
- `SAFETY.md` records durable safety and development boundaries.
- `.ai/history/` preserves task-specific evidence, including superseded or
  blocked outcomes; history is not current project state.

Other local `.ai/*.md` files are not authoritative current-state sources unless
they are explicitly adopted by a later repository decision.

## Project responsibility

AI Context Orchestrator (ACO) is a declarative business-context orchestration
layer for Spring AI. It owns the path between application-provided business
context sources and the Spring AI model boundary.

ACO plans which context to load, executes selected sources, resolves competing
facts, applies a context budget, assembles structured model-facing context, and
bridges the result into Spring AI.

## Module boundaries

- `ai-context-core` contains the provider-neutral domain model and extension
  SPI. It does not depend on Spring AI. Shared contracts stay in
  `io.github.forgottenlab.aicontext.core`; stage-specific types are organized
  under `planning`, `execution`, `resolution`, `budget`, and `assembly` within
  the same Maven module.
- `ai-context-spring-ai` adapts Spring AI requests, runs the orchestration
  pipeline through an Advisor, preserves existing system instructions, and
  appends rendered business context.
- `ai-context-spring-boot-autoconfigure` discovers consumer `ContextSource`
  beans and creates replaceable default runtime components.
- `ai-context-spring-boot-starter` is the consumer dependency aggregator.
- `examples` is reserved for consumer-facing runnable examples.

Dependency direction is:

```text
Starter
  -> Spring Boot AutoConfiguration
    -> Spring AI bridge
      -> Core
```

## Runtime pipeline

```text
ContextRequest
  -> ContextPlanner
  -> ContextExecutor
  -> ContextResolver
  -> ContextBudgeter
  -> ContextAssembler
  -> ContextOrchestrationAdvisor
  -> Spring AI
```

The stage boundaries are intentional:

- Planner decides what should load and performs no source I/O.
- Executor performs selected loads and isolates failures; it does not resolve
  Authority or apply Budget.
- Resolver owns `ContextKey`, Authority, and Freshness conflict handling; it
  does not use Priority.
- Budgeter owns Priority and budget pressure after resolution; it does not use
  Authority to decide factual trust.
- Assembler preserves structured provenance and renders typed values only near
  the model boundary.

Authority and Priority are separate concepts and must remain separate.

## Consumer model

Normal Spring Boot consumers should depend on the Starter and contribute
application-specific `ContextSource` beans. Consumers should not need to
manually assemble Planner, Executor, Resolver, Budgeter, or Assembler.

This contract is verified by a fake-model consumer E2E: the Starter provides
the auto-configured `ChatClient.Builder`, applies ACO's Advisor automatically,
preserves the existing system message, and appends the consumer source's facts
inside `<business-context>` without an API key or external model call.

The runnable offline Quickstart exercises the same contract as a non-web Spring
Boot consumer. Its only direct production ACO dependency is the Starter; it
contributes an application `ContextSource`, uses an in-memory `ChatModel`, and
prints the final enriched system message without provider credentials or
runtime network access.

## Compatibility baseline

- Java 17 and Spring Boot 3.5.16 are the current build baseline.
- Spring AI 1.1.8 remains the default dependency baseline.
- Spring AI 1.0.0 is the verified minimum supported version.
- The standalone `compatibility/spring-ai-consumer` fixture validates the real
  external Starter consumer path against Spring AI 1.0.0 and 1.1.8. It remains
  outside the root reactor so compatibility tests do not change the 119-test
  reactor baseline.

## Authoritative ground-truth boundary

`ContextPriority.REQUIRED` protects already-resolved context from budget
removal; it does not require a `ContextSource` to execute successfully. Core
continues to isolate per-source errors and timeouts in
`ContextExecutionReport`, and the Advisor proceeds with successful
contributions.

When an application request requires authoritative business ground truth to be
safe, the application service must load, authorize, and validate that state
before invoking `ChatClient`. Query failure, timeout, not-found, or
authorization failure must prevent model invocation at that consumer boundary.

## Integration validation

GitHub Actions runs the default reactor test and package gates, executes the
offline Quickstart, and tests the external consumer fixture against both
verified Spring AI anchors. Project and SCM metadata point to the canonical
ForgottenLab GitHub repository.

Persistence, cache, vector-store, memory, authorization, and model-provider
infrastructure remain application or provider responsibilities.

## Non-goals

- reimplement Spring AI or model SDKs;
- replace JDBC, MyBatis, JPA, Redis clients, or vector databases;
- execute unrestricted NL2SQL;
- hide all Spring AI capabilities from advanced users;
- add abstractions without a demonstrated consumer need.

## API evolution policy

Core is frozen by default. Change Core only when a real Starter, Quickstart, or
consumer scenario demonstrates a missing or incorrect contract, and then make
the smallest compatible change supported by tests.
