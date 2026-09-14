# AI Context Orchestrator

[English](README.md) | [简体中文](README.zh-CN.md)

> Working name. The public project name and artifact coordinates can be renamed before the first release.

AI Context Orchestrator is a thin, declarative business-context orchestration layer for Spring AI applications.

## Goal

Let application developers describe **what the AI is allowed and expected to know**, while the framework handles how that context is loaded, prioritized, governed, budgeted, and assembled.

The project deliberately does **not** replace Spring AI, Spring Data, MyBatis, Redis clients, vector-store implementations, or model SDKs.

## Design principles

1. Declarative first.
2. Convention over configuration.
3. Safe defaults.
4. Progressive override: YAML -> annotations -> Java API -> SPI.
5. Preserve context identity; do not flatten everything into one string too early.
6. Business ground truth should be able to outrank stale or lower-authority context.
7. Context budget is a first-class concern.
8. Provider-neutral core; Spring AI is the primary integration layer.
9. Spring AI Alibaba is an optional enhancement, not a hard dependency of the core.
10. No automatic write-capable SQL from LLM output.

## Modules

- `ai-context-core`: provider-neutral context model and extension SPI.
- `ai-context-spring-ai`: Spring AI bridge.
- `ai-context-spring-boot-autoconfigure`: Boot auto-configuration and properties.
- `ai-context-spring-boot-starter`: user-facing dependency aggregator with the
  verified Spring AI `ChatClient.Builder` consumer path.
- `examples/quickstart`: placeholder for the planned runnable minimal example.
- `examples/advanced-context`: placeholder for the planned extensible example.

The Core module remains one Maven module, with shared contracts in
`io.github.forgottenlab.aicontext.core` and pipeline-specific types organized
under `planning`, `execution`, `resolution`, `budget`, and `assembly`.

## ✅ Current status

The Starter consumer contract is covered by a fake-model Spring Boot E2E test.
A normal consumer can depend on the Starter, contribute a `ContextSource`,
inject the auto-configured `ChatClient.Builder`, and receive ACO business
context in the final Prompt without an API key or external model call.

The runnable Quickstart has not been implemented yet.

## Baseline

- Java 17
- Spring Boot 3.5.16
- Spring AI 1.1.8
- Maven

Spring AI Alibaba support will be introduced behind a dedicated adapter/compatibility layer rather than forcing its Spring AI version onto the core dependency graph.

## 🧪 Testing

| Module | Tests |
|---|---:|
| Core | 89 |
| Spring AI | 18 |
| Spring Boot AutoConfigure | 10 |
| Spring Boot Starter | 1 |
| Total | 118 |

Run the complete local reactor with:

```powershell
mvn test
```

The next consumer-facing milestone is the runnable Quickstart, followed by
real-application dogfooding. These roadmap items are not claims of current
implementation.

## 📖 Documentation

- [Architecture](docs/architecture/ARCHITECTURE.md) | [架构说明](docs/architecture/ARCHITECTURE.zh-CN.md)
- [Class Map](docs/architecture/CLASS_MAP.md) | [类职责地图](docs/architecture/CLASS_MAP.zh-CN.md)
- [Getting Started](docs/guides/GETTING_STARTED.md) | [快速开始](docs/guides/GETTING_STARTED.zh-CN.md)
- [Testing](docs/project/TESTING.md) | [测试说明](docs/project/TESTING.zh-CN.md)
- [Roadmap](docs/project/ROADMAP.md) | [路线图](docs/project/ROADMAP.zh-CN.md)
- [Safety Notes](docs/project/SAFETY.md) | [安全说明](docs/project/SAFETY.zh-CN.md)

## License

This project is licensed under the [Apache License 2.0](LICENSE).
