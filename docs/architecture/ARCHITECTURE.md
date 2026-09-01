# 🧱 Architecture

[English](ARCHITECTURE.md) | [简体中文](ARCHITECTURE.zh-CN.md)

## ✨ Positioning

AI Context Orchestrator is a declarative business-context orchestration layer for Spring AI.

It orchestrates context; it does not replace Spring AI, persistence frameworks, Redis, or vector databases.

## 🧭 Runtime Pipeline

```text
ContextRequest
   ↓
ContextPlanner
   ↓
ContextPlan
   ↓
ContextExecutor
   ↓
ContextExecutionReport
   ↓
ContextResolver
   ↓
ContextResolution
   ↓
ContextBudgeter
   ↓
ContextBudgetResult
   ↓
ContextAssembler
   ↓
ContextAssembly
   ↓
ContextOrchestrationAdvisor
   ↓
Spring AI ChatClient
```

## 🧩 Module Responsibilities

| Module | Responsibility |
|---|---|
| `ai-context-core` | Provider-neutral domain and SPI |
| `ai-context-spring-ai` | Spring AI Advisor bridge |
| `ai-context-spring-boot-autoconfigure` | Spring Boot default wiring |
| `ai-context-spring-boot-starter` | Consumer dependency aggregation |
| `examples` | Runnable consumer examples |

## ⚖️ Authority vs Priority

- **Authority** decides which conflicting fact is trusted.
- **Priority** decides which already-resolved fact survives budget pressure.

They are intentionally separate concepts.

## 🔐 Safety Boundary

Business context is rendered inside a controlled `<business-context>` envelope.

This reduces accidental instruction confusion but is not a complete prompt-injection security boundary.