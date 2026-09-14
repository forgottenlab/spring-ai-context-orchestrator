# 🚀 快速开始

[English](GETTING_STARTED.md) | [简体中文](GETTING_STARTED.zh-CN.md)

## 📦 引入 Starter

目标使用方式：

```xml
<dependency>
    <groupId>io.github.forgottenlab.aicontext</groupId>
    <artifactId>ai-context-spring-boot-starter</artifactId>
    <version>${ai-context.version}</version>
</dependency>
```

## 🧩 提供业务 ContextSource

业务项目通常只需要定义自己的 `ContextSource`：

```java
@Component
class ProductContextSource implements ContextSource {

    @Override
    public String id() {
        return "product";
    }

    @Override
    public CompletableFuture<ContextContribution> load(ContextRequest request) {
        // 调用业务 Service / Repository 获取事实
        ...
    }
}
```

## 🤖 正常使用 Spring AI

```java
@Service
class ProductAssistant {

    private final ChatClient chatClient;

    ProductAssistant(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
}
```

业务方不应该手工调用：

```text
Planner
Executor
Resolver
Budgeter
Assembler
```

这些属于 ACO 内部流水线。

## ✅ 当前状态

AutoConfiguration 与 Starter 消费者链路已经完成验证。

`StarterConsumerE2ETest` 使用不需要 API Key、不会访问网络的 Fake
`ChatModel`，已经证明：

```text
Starter
  ↓
ContextSource
  ↓
AutoConfiguration
  ↓
Advisor
  ↓
最终 Prompt
```

整条链路可工作，并验证原 system message 得以保留、
`<business-context>` 与 `ContextSource` 事实进入最终 Prompt。

`examples/quickstart` 当前仍是占位目录；下一阶段会把已验证契约整理为
真正可运行的示例。
