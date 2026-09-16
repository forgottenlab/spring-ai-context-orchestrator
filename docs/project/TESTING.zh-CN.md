# 🧪 测试说明

[English](TESTING.md) | [简体中文](TESTING.zh-CN.md)

## ✅ 当前测试基线

| 模块 | 测试数量 | 状态 |
|---|---:|---|
| `ai-context-core` | 89 | ✅ |
| `ai-context-spring-ai` | 18 | ✅ |
| `ai-context-spring-boot-autoconfigure` | 10 | ✅ |
| `ai-context-spring-boot-starter` | 1 | ✅ |
| `examples/quickstart` | 1 | ✅ |
| 合计 | 119 | ✅ |

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
| Starter E2E | 使用 Fake `ChatModel` 验证普通消费者依赖 Starter 后的完整链路 |

Starter E2E 会检查自动配置的 `ChatClient.Builder`、自动注册的 ACO
Advisor、原 system message，以及追加到最终 Prompt 的业务上下文。测试不需要
API Key，也不会访问模型网络服务。

Quickstart 测试会启动真正可运行的消费者应用，验证注入的
`ChatClient.Builder` 触发一次 Source 加载和一次离线模型调用，并检查捕获的
Prompt 保留 system、user message 且只包含一个增强后的业务上下文信封。

## 🔁 Spring AI 兼容性

独立的 `compatibility/spring-ai-consumer` fixture 按外部应用方式运行：依赖
已安装的 ACO Starter，提供一个通用 `ContextSource`，获得自动配置的
`ChatClient.Builder`，并用内存 `ChatModel` 检查最终 Prompt。

已验证锚点：

| Spring AI | 状态 |
|---|---|
| 1.0.0 | ✅ PASS |
| 1.1.8 | ✅ PASS（当前默认） |

最低支持的 Spring AI 版本是 `1.0.0`，默认依赖基线仍为 `1.1.8`。Fixture
位于根 reactor 之外，因此其中 1 项测试不计入 reactor 的 119 项测试总数。

```powershell
mvn -DskipTests install
mvn -f .\compatibility\spring-ai-consumer\pom.xml "-Dspring-ai.version=1.0.0" clean test
mvn -f .\compatibility\spring-ai-consumer\pom.xml "-Dspring-ai.version=1.1.8" clean test
```

## 🤖 持续集成

GitHub Actions 会运行默认 reactor 测试、打包仓库、执行已打包的离线
Quickstart，并针对两个已验证 Spring AI 锚点运行外部消费者 fixture。CI 使用
Java 17，不需要模型 Provider 或 secret。

## ⚠️ 已知非阻塞警告

当前 Maven 编译会出现 `compilerVersion` deprecated 警告。

它不影响现有测试结果，计划放到独立 build hygiene 提交处理中，避免和功能修改混在一起。
