# 🗺️ Class Map

[English](CLASS_MAP.md) | [简体中文](CLASS_MAP.zh-CN.md)

## 📖 Recommended Reading Order

```text
ContextSource
ContextRequest
ContextPlanner
DefaultContextPlanner
ContextExecutor
DefaultContextExecutor
ContextResolver
DefaultContextResolver
ContextBudgeter
DefaultContextBudgeter
ContextAssembler
DefaultContextAssembler
ContextOrchestrationAdvisor
AiContextAutoConfiguration
```

## 📦 Package Map

| Package | Main responsibility |
|---|---|
| `io.github.forgottenlab.aicontext.core` | Shared domain contracts and the consumer-facing `ContextSource` SPI |
| `.core.planning` | Load decisions and plans |
| `.core.execution` | Source loading, concurrency, timeout, and failure isolation |
| `.core.resolution` | Conflict, Authority, and Freshness resolution |
| `.core.budget` | Required protection and Priority-based budget selection |
| `.core.assembly` | Structured model-facing context assembly and late value rendering |

## 🧩 Main Types

| Type | Responsibility |
|---|---|
| `ContextSource` | Application extension point for business context |
| `ContextRequest` | Input to orchestration |
| `ContextPlanner` | Decide which sources should load |
| `ContextExecutor` | Execute selected sources |
| `ContextResolver` | Resolve conflicting facts |
| `ContextBudgeter` | Apply context budget |
| `ContextAssembler` | Build structured model-facing context |
| `ContextOrchestrationAdvisor` | Run the whole pipeline for Spring AI |
| `AiContextAutoConfiguration` | Build the default Spring Boot graph |

Start with this path instead of reading every type under `ai-context-core`.
The package hierarchy expresses pipeline ownership without splitting Core into
multiple Maven modules.
