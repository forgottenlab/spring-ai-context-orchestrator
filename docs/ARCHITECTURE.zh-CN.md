# 🧱 架构说明

[English](ARCHITECTURE.md) | [简体中文](ARCHITECTURE.zh-CN.md)

## ✨ 项目定位

AI Context Orchestrator 是面向 Spring AI 的声明式业务上下文编排层。

它解决的是“如何把多个业务上下文正确组织后交给模型”的问题，而不是替代：

- Spring AI；
- JDBC / MyBatis / JPA；
- Redis；
- 向量数据库；
- 业务 Service / Repository。

## 🧭 运行时流水线

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

## 🧩 模块职责

| 模块 | 职责 |
|---|---|
| `ai-context-core` | 与具体 AI 框架无关的领域模型与 SPI |
| `ai-context-spring-ai` | Spring AI Advisor 桥接 |
| `ai-context-spring-boot-autoconfigure` | Spring Boot 默认 Bean 装配 |
| `ai-context-spring-boot-starter` | 给使用者提供单一依赖入口 |
| `examples` | 可运行的消费者示例 |

## 🧠 为什么会有很多小类

这是一个框架项目，而不是普通 CRUD 应用。

把 Planner、Executor、Resolver、Budgeter、Assembler 拆开，是为了让业务方能够只替换其中一层，而不用重写整条流水线。

例如：

```text
默认 Planner
默认 Executor
自定义 Resolver
默认 Budgeter
默认 Assembler
```

这样的组合应该是合法且容易配置的。

## ⚖️ Authority 与 Priority

两个概念必须分开：

| 概念 | 解决的问题 |
|---|---|
| Authority | 多个事实冲突时，信谁 |
| Priority | 预算不足时，保留谁 |

Authority 负责“事实可信度”。

Priority 负责“上下文生存优先级”。

## 🔐 安全边界

最终业务上下文会被放入受控的：

```xml
<business-context>
...
</business-context>
```

中，并附带“上下文内容是数据而不是指令”的策略说明。

但这只能降低误解释风险，不能被当成完整的 Prompt Injection 防护。