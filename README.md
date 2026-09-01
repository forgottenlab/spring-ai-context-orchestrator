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

## Initial modules

- `ai-context-core`: provider-neutral context model and extension SPI.
- `ai-context-spring-ai`: Spring AI bridge.
- `ai-context-spring-boot-autoconfigure`: Boot auto-configuration and properties.
- `ai-context-spring-boot-starter`: user-facing dependency aggregator.
- `examples/quickstart`: reserved for the minimal example.
- `examples/advanced-context`: reserved for the extensible example.

## Baseline

- Java 17
- Spring Boot 3.5.16
- Spring AI 1.1.8
- Maven

Spring AI Alibaba support will be introduced behind a dedicated adapter/compatibility layer rather than forcing its Spring AI version onto the core dependency graph.

## 📖 Documentation

- [Architecture](docs/architecture/ARCHITECTURE.md) | [架构说明](docs/architecture/ARCHITECTURE.zh-CN.md)
- [Class Map](docs/architecture/CLASS_MAP.md) | [类职责地图](docs/architecture/CLASS_MAP.zh-CN.md)
- [Getting Started](docs/guides/GETTING_STARTED.md) | [快速开始](docs/guides/GETTING_STARTED.zh-CN.md)
- [Testing](docs/project/TESTING.md) | [测试说明](docs/project/TESTING.zh-CN.md)
- [Roadmap](docs/project/ROADMAP.md) | [路线图](docs/project/ROADMAP.zh-CN.md)
- [Safety Notes](docs/project/SAFETY.md) | [安全说明](docs/project/SAFETY.zh-CN.md)
