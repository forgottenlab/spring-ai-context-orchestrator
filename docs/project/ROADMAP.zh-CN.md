# 🗺️ 路线图

[English](ROADMAP.md) | [简体中文](ROADMAP.zh-CN.md)

## 📌 当前阶段

| 里程碑 | 状态 | 说明 |
|---|---|---|
| Provider-neutral Core | ✅ 完成 | Planner → Assembler 基线 |
| Spring AI Advisor Bridge | ✅ 完成 | 已能注入业务上下文 |
| Spring Boot AutoConfiguration | ✅ 完成 | 默认消费者运行图 |
| Starter Fake ChatModel E2E | 🟡 下一步 | 验证真正的一依赖使用体验 |
| Runnable Quickstart | 🟡 计划中 | 给用户可直接运行的示例 |
| Minimal YAML | 🟡 计划中 | 只保留真正需要配置的项目 |
| Build / README Hygiene | 🟡 计划中 | 清理警告、完善发布说明 |
| First Alpha | ⏳ 待定 | 消费者验证完成后再考虑 |

## 🎯 下一阶段原则

下一步不继续为了“看起来高级”而增加 Core 抽象。

优先证明：

```text
普通 Spring Boot 应用
        ↓
只依赖 Starter
        ↓
提供 ContextSource
        ↓
注入 ChatClient.Builder
        ↓
业务上下文自动进入最终 Prompt
```

只有真实消费者案例暴露缺口时，才继续调整 Core API。