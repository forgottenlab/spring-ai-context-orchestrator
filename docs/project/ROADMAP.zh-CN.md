# 🗺️ 路线图

[English](ROADMAP.md) | [简体中文](ROADMAP.zh-CN.md)

## 📌 当前阶段

| 里程碑 | 状态 | 说明 |
|---|---|---|
| Provider-neutral Core | ✅ 完成 | Planner → Assembler 基线 |
| Spring AI Advisor Bridge | ✅ 完成 | 已能注入业务上下文 |
| Spring Boot AutoConfiguration | ✅ 完成 | 默认消费者运行图 |
| Starter Fake ChatModel E2E | ✅ 完成 | 已验证真正的单一 Starter 依赖使用体验 |
| Core Package Organization | ✅ 完成 | 单一 Core module 按 pipeline stage 分类 |
| Runnable Offline Quickstart | ✅ 完成 | 离线可执行示例覆盖真实 Starter 消费者链路 |
| Integration Readiness Gates | ✅ 完成 | 兼容性锚点、fail-closed 边界、CI 与仓库 metadata 已建立 |
| 真实应用 Dogfooding | 🟡 下一步 | 在实际消费者中验证使用体验 |
| Minimal YAML | 🟡 计划中 | 只保留真正需要配置的项目 |
| Build / README Hygiene | 🟡 计划中 | 清理警告、完善发布说明 |
| First Alpha | ⏳ 待定 | 消费者验证完成后再考虑 |

## 🎯 下一阶段原则

兼容性锚点、fail-closed 职责边界、CI 和仓库 metadata 已经建立。下一步不
继续为了“看起来高级”而增加 Core 抽象，而是在真实应用中 dogfood Starter，
记录具体消费者摩擦后再判断是否需要调整。

当前测试已经证明：

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

Roadmap 只表达方向，不构成编码授权。
