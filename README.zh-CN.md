# 🧠 AI Context Orchestrator

[English](README.md) | [简体中文](README.zh-CN.md)

> 面向 Spring AI 的声明式业务上下文编排层。  
> 让应用只声明“AI 需要哪些业务上下文”，由框架负责规划、加载、冲突处理、预算裁剪与最终注入。

---

## ✨ 项目愿景

在真实业务中，AI 的回答通常不只依赖聊天记录，还可能依赖：

- 当前用户资料；
- 商品、订单、库存等数据库事实；
- Redis 中的会话状态；
- 向量检索结果；
- 业务规则与权限上下文；
- 其他应用内部服务提供的数据。

如果每个 AI 接口都手工拼接这些上下文，代码很快会变成重复、难测、难扩展的 Prompt 组装逻辑。

AI Context Orchestrator（ACO）的目标是把这部分抽象成独立的上下文编排层：

```text
业务代码声明 ContextSource
        ↓
ACO 规划需要加载的上下文
        ↓
并发执行 ContextSource
        ↓
按 Authority / Freshness 解决冲突
        ↓
按 Priority / Budget 选择保留内容
        ↓
组装结构化 ContextAssembly
        ↓
Spring AI Advisor 自动注入模型请求
```

---

## 🚀 当前功能

| 能力 | 状态 | 说明 |
|---|---|---|
| Provider-neutral Core | ✅ 已完成 | Core 不依赖 Spring AI |
| ContextSource SPI | ✅ 已完成 | 业务方提供上下文入口 |
| Planning | ✅ 已完成 | 决定 LOAD / DEFER / SKIP |
| Execution | ✅ 已完成 | 并发执行、超时与失败隔离 |
| Resolution | ✅ 已完成 | Authority / Freshness 冲突处理 |
| Budget | ✅ 已完成 | Required + Priority 预算选择 |
| Structured Assembly | ✅ 已完成 | 保持结构化到模型边界 |
| Spring AI Advisor Bridge | ✅ 已完成 | 自动追加业务上下文 |
| Spring Boot AutoConfiguration | ✅ 已完成 | 自动构建默认运行图 |
| Spring Boot Starter | 🟡 基线已建立 | 下一步完成真正的消费者 E2E |
| Runnable Quickstart | 🟡 进行中 | 使用 Fake ChatModel 验证完整链路 |

---

## 🧱 架构说明

核心流水线：

```text
ContextRequest
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
ContextOrchestrationAdvisor
   ↓
Spring AI ChatClient
```

详细说明：

- [架构说明](docs/ARCHITECTURE.zh-CN.md)
- [类职责地图](docs/CLASS_MAP.zh-CN.md)

---

## 📦 使用方式

当前目标使用体验：

```xml
<dependency>
    <groupId>io.github.forgottenlab.aicontext</groupId>
    <artifactId>ai-context-spring-boot-starter</artifactId>
    <version>${ai-context.version}</version>
</dependency>
```

业务方只需要提供自己的 `ContextSource`：

```java
@Component
class ProductContextSource implements ContextSource {

    @Override
    public String id() {
        return "product";
    }

    @Override
    public CompletableFuture<ContextContribution> load(ContextRequest request) {
        // 从业务 Service / Repository 获取事实并返回 ContextItem
        ...
    }
}
```

然后继续按 Spring AI 的方式使用：

```java
@Service
class ProductAssistant {

    private final ChatClient chatClient;

    ProductAssistant(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }
}
```

ACO 不要求业务代码手工调用 Planner、Executor、Resolver、Budgeter 或 Assembler。

---

## ⚙️ 配置

当前公开预算配置：

```yaml
forgottenlab:
  ai:
    context:
      enabled: true
      budget:
        max-context-tokens: 12000
```

注意：

`max-context-tokens` 是当前公开配置名称，但默认 `ContextCostEstimator` 仍是 tokenizer-neutral 的确定性估算器。

当前默认语义：

```text
1 个渲染后的 Unicode code point = 1 个 cost unit
```

如果业务需要精确模型 Token 统计，可自行替换 `ContextCostEstimator`。

---

## 🧪 测试

当前基线：

| 模块 | 测试数 | 状态 |
|---|---:|---|
| Core | 89 | ✅ |
| Spring AI | 18 | ✅ |
| Spring Boot AutoConfigure | 10 | ✅ |
| 合计 | 117 | ✅ |

运行：

```powershell
mvn test
```

详细说明：

- [测试说明](docs/TESTING.zh-CN.md)

---

## 📖 相关文档

| 文档 | 中文 | English |
|---|---|---|
| 架构说明 | [ARCHITECTURE.zh-CN.md](docs/ARCHITECTURE.zh-CN.md) | [ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| 类职责地图 | [CLASS_MAP.zh-CN.md](docs/CLASS_MAP.zh-CN.md) | [CLASS_MAP.md](docs/CLASS_MAP.md) |
| 快速开始 | [GETTING_STARTED.zh-CN.md](docs/GETTING_STARTED.zh-CN.md) | [GETTING_STARTED.md](docs/GETTING_STARTED.md) |
| 测试说明 | [TESTING.zh-CN.md](docs/TESTING.zh-CN.md) | [TESTING.md](docs/TESTING.md) |
| 路线图 | [ROADMAP.zh-CN.md](docs/ROADMAP.zh-CN.md) | [ROADMAP.md](docs/ROADMAP.md) |
| 安全说明 | [SAFETY.zh-CN.md](docs/SAFETY.zh-CN.md) | [SAFETY.md](docs/SAFETY.md) |

---

## 🔐 安全提醒

ACO 会接触业务上下文，因此使用时应特别注意：

- 不要把密码、Token、私钥等敏感数据无条件注入模型；
- `ContextSource` 应遵守应用自身的权限边界；
- 数据库访问优先只读，并尽量使用明确查询而不是开放式 NL2SQL；
- Prompt 中的 `<business-context>` 只能降低误把数据当指令的风险，不能视为完整 Prompt Injection 防护；
- 日志中不要记录原始敏感上下文。

详细说明见：

- [安全说明](docs/SAFETY.zh-CN.md)

---

## 🗺️ 路线图

下一阶段重点不是继续扩张 Core，而是验证 Starter 的真实消费者体验：

```text
Starter dependency
    ↓
ContextSource Bean
    ↓
AutoConfiguration
    ↓
ChatClient.Builder
    ↓
Fake ChatModel
    ↓
最终 Prompt 中出现 <business-context>
```

详细路线：

- [路线图](docs/ROADMAP.zh-CN.md)

---

## 📜 许可证

许可证信息以仓库中的 `LICENSE` 文件为准。