# 🚀 Getting Started

[English](GETTING_STARTED.md) | [简体中文](GETTING_STARTED.zh-CN.md)

## 📦 Dependency

```xml
<dependency>
    <groupId>io.github.forgottenlab.aicontext</groupId>
    <artifactId>ai-context-spring-boot-starter</artifactId>
    <version>${ai-context.version}</version>
</dependency>
```

## 🧩 Provide a ContextSource

```java
@Component
class ProductContextSource implements ContextSource {
    // Load application business facts here.
}
```

## 🤖 Use Spring AI Normally

```java
ProductAssistant(ChatClient.Builder builder) {
    this.chatClient = builder.build();
}
```

ACO is intended to auto-register its advisor with the auto-configured builder.

The runnable fake-model E2E quickstart is the next milestone.