# Runnable Offline Quickstart

[English](#english) | [简体中文](#简体中文)

## English

### 🎯 What this example proves

This runnable Spring Boot application exercises ACO exactly as a normal
consumer would:

```text
ai-context-spring-boot-starter
  -> application ContextSource bean
  -> auto-configured ChatClient.Builder
  -> automatically registered ACO Advisor
  -> offline ChatModel captures the final Prompt
```

The original system message is preserved, one `<business-context>` envelope is
appended, and the inventory fact `stock=7` reaches the model boundary.

### ✅ Requirements

- Java 17 or later
- Maven 3.9 or later

Maven may need network access the first time it resolves build dependencies.
Running the packaged application itself requires no network access, model
provider, API key, database, or Docker service.

### 🔨 Build and run

From the repository root:

```powershell
mvn -pl examples/quickstart -am clean package
java -jar .\examples\quickstart\target\ai-context-quickstart-0.1.0-SNAPSHOT.jar
```

The application is non-web, performs one request, prints the actual system
message received by the offline model, closes its Spring context, and exits.

### 🖥️ Expected output

The Spring Boot banner and startup log are followed by output containing:

```text
=== AI Context Orchestrator Quickstart ===

Assistant response:
offline-response

Final system message:
You are the inventory assistant.

<business-context>
...
<context source="inventory" ...>stock=7</context>
</business-context>

Context source loads:
1
```

### 🧭 Code walkthrough

- `QuickstartApplication` starts a non-web Spring Boot application and closes
  the context after startup runners finish.
- `InventoryContextSource` is consumer code. It contributes one
  `AUTHORITATIVE`, `REQUIRED` inventory fact with a stable `ContextKey`.
- `QuickstartDemoRunner` injects `ChatClient.Builder`, builds a normal
  `ChatClient`, and sends the system and user messages.
- `OfflineChatModel` is a local deterministic model stub. It captures the final
  `Prompt` and returns `offline-response`.
- `QuickstartApplicationTest` verifies the same application path, including
  automatic ACO enrichment and the exact source/model call counts.

The consumer writes the application, `ContextSource`, model bean, and business
call. The Starter supplies Spring AI's `ChatClient.Builder` auto-configuration
and ACO's orchestration components and Advisor registration. No Planner,
Executor, Resolver, Budgeter, Assembler, or Advisor is manually constructed.

### 🔐 Why no API key is needed

`OfflineChatModel` implements Spring AI's `ChatModel` contract entirely in
memory. It does not connect to any provider and exists only to make the complete
consumer integration observable and repeatable offline.

### ➡️ Next steps

Use this module as the smallest consumer reference. Replace the sample
`InventoryContextSource` with application-specific sources and replace
`OfflineChatModel` with the Spring AI model integration selected by the
consumer application. Provider credentials and access controls remain the
consumer's responsibility.

## 简体中文

### 🎯 这个示例验证什么

这是一个可以直接运行的 Spring Boot 应用，并且严格按照普通消费者的方式
使用 ACO：

```text
ai-context-spring-boot-starter
  -> 应用提供 ContextSource Bean
  -> 自动配置 ChatClient.Builder
  -> 自动注册 ACO Advisor
  -> 离线 ChatModel 捕获最终 Prompt
```

最终 Prompt 会保留原 system message，追加且只追加一个
`<business-context>`，并把库存事实 `stock=7` 送到模型边界。

### ✅ 环境要求

- Java 17 或更高版本
- Maven 3.9 或更高版本

Maven 首次解析构建依赖时可能需要网络；打包后的应用运行时不需要网络、
模型供应商、API Key、数据库或 Docker 服务。

### 🔨 构建与运行

在仓库根目录执行：

```powershell
mvn -pl examples/quickstart -am clean package
java -jar .\examples\quickstart\target\ai-context-quickstart-0.1.0-SNAPSHOT.jar
```

应用以非 Web 模式启动，只发起一次请求，打印离线模型实际收到的最终
system message，然后关闭 Spring context 并自然退出。

### 🖥️ 预期输出

Spring Boot banner 与启动日志之后会出现以下关键内容：

```text
=== AI Context Orchestrator Quickstart ===

Assistant response:
offline-response

Final system message:
You are the inventory assistant.

<business-context>
...
<context source="inventory" ...>stock=7</context>
</business-context>

Context source loads:
1
```

### 🧭 代码说明

- `QuickstartApplication` 启动非 Web Spring Boot 应用，并在启动 Runner
  执行完成后关闭 context。
- `InventoryContextSource` 属于消费者代码，提供一个带稳定 `ContextKey`
  的 `AUTHORITATIVE`、`REQUIRED` 库存事实。
- `QuickstartDemoRunner` 注入 `ChatClient.Builder`，构建普通
  `ChatClient`，然后发送 system 与 user message。
- `OfflineChatModel` 是本地确定性的模型 Stub，负责捕获最终 `Prompt` 并
  返回 `offline-response`。
- `QuickstartApplicationTest` 验证同一条应用链路，包括 ACO 自动增强以及
  Source、Model 的精确调用次数。

消费者只编写应用、`ContextSource`、模型 Bean 和业务调用。Starter 提供
Spring AI `ChatClient.Builder` 自动配置、ACO 编排组件以及 Advisor 注册；
示例没有手工构造 Planner、Executor、Resolver、Budgeter、Assembler 或
Advisor。

### 🔐 为什么不需要 API Key

`OfflineChatModel` 完全在内存中实现 Spring AI 的 `ChatModel` 契约，不连接
任何模型供应商。它只用于离线、可重复地观察完整消费者集成链路。

### ➡️ 下一步

可以把本模块作为最小消费者参考：将 `InventoryContextSource` 替换为应用
自己的上下文源，并将 `OfflineChatModel` 替换为消费者选择的 Spring AI
模型集成。模型凭证与访问控制仍由消费者应用负责。
