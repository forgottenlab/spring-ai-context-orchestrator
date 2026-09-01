# 🗺️ 类职责地图

[English](CLASS_MAP.md) | [简体中文](CLASS_MAP.zh-CN.md)

## 📖 推荐阅读顺序

第一次打开项目时，不要从 `ai-context-core` 下面几十个类型逐个看。

推荐按真实请求流向阅读：

```text
1. ContextSource
2. ContextRequest
3. ContextPlanner
4. DefaultContextPlanner
5. ContextExecutor
6. DefaultContextExecutor
7. ContextResolver
8. DefaultContextResolver
9. ContextBudgeter
10. DefaultContextBudgeter
11. ContextAssembler
12. DefaultContextAssembler
13. ContextOrchestrationAdvisor
14. AiContextAutoConfiguration
```

## 🧩 核心类型职责

| 类型 | 负责什么 | 不负责什么 |
|---|---|---|
| `ContextSource` | 从业务系统提供上下文 | 不负责预算和冲突仲裁 |
| `ContextRequest` | 表示一次上下文编排请求 | 不执行 I/O |
| `ContextPlanner` | 决定哪些 Source 需要加载 | 不真正加载数据 |
| `ContextExecutor` | 执行被选中的 Source | 不判断谁更可信 |
| `ContextResolver` | 处理冲突、时效与 Authority | 不处理预算 |
| `ContextBudgeter` | 根据预算与 Priority 保留内容 | 不处理事实可信度 |
| `ContextAssembler` | 把选中内容组装成结构化块 | 不直接调用模型 |
| `ContextOrchestrationAdvisor` | 串起完整流水线并接入 Spring AI | 不替代业务 Service |
| `AiContextAutoConfiguration` | 自动创建默认 Bean 图 | 不保存业务数据 |

## 🔍 调试一次请求时怎么找

从这里开始：

```text
ContextOrchestrationAdvisor.before(...)
        ↓
SpringAiContextRequestFactory
        ↓
ContextPlanner
        ↓
ContextExecutor
        ↓
ContextResolver
        ↓
ContextBudgeter
        ↓
ContextAssembler
        ↓
ContextAssemblySystemTextRenderer
```

这条路径就是整个项目最短的“主干”。