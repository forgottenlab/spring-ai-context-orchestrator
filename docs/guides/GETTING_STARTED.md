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

This consumer path is verified by `StarterConsumerE2ETest`: a fake
`ChatModel` receives the final Prompt, including the preserved system message
and appended `<business-context>`, without an API key or network call.

## ▶️ Run the Offline Quickstart

The [Quickstart](../../examples/quickstart/README.md) is an executable non-web
Spring Boot consumer. From the repository root:

```powershell
mvn -pl examples/quickstart -am clean package
java -jar .\examples\quickstart\target\ai-context-quickstart-0.1.0-SNAPSHOT.jar
```

It defines its own inventory `ContextSource`, injects the auto-configured
`ChatClient.Builder`, and uses an in-memory `ChatModel` to print the actual
enriched system message. Maven may require network access to resolve build
dependencies, but the packaged application needs no API key, provider, or
runtime network connection.
