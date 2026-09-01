# 🧪 测试说明

[English](TESTING.md) | [简体中文](TESTING.zh-CN.md)

## ✅ 当前测试基线

| 模块 | 测试数量 | 状态 |
|---|---:|---|
| `ai-context-core` | 89 | ✅ |
| `ai-context-spring-ai` | 18 | ✅ |
| `ai-context-spring-boot-autoconfigure` | 10 | ✅ |
| 合计 | 117 | ✅ |

## ▶️ 本地测试

在项目根目录运行：

```powershell
mvn test
```

提交前建议至少执行：

```powershell
git diff --check
mvn test
git status
```

## 🧱 测试分层

| 层次 | 目标 |
|---|---|
| Core Unit Tests | 固化 Planner / Executor / Resolver / Budgeter / Assembler 规则 |
| Spring AI Bridge Tests | 验证系统消息增强与上下文桥接 |
| AutoConfiguration Tests | 验证默认 Bean 图、back-off、配置映射 |
| Starter E2E | 下一阶段：验证普通消费者依赖 Starter 后完整链路 |

## ⚠️ 已知非阻塞警告

当前 Maven 编译会出现 `compilerVersion` deprecated 警告。

它不影响现有测试结果，计划放到独立 build hygiene 提交处理中，避免和功能修改混在一起。