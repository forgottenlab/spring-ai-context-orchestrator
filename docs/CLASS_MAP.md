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