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

## 🛡️ Validate Authoritative Ground Truth First

`ContextPriority.REQUIRED` is a budget-retention rule for context that has
already loaded and passed resolution. It does not require a `ContextSource` to
execute successfully. ACO intentionally isolates individual source errors and
timeouts so optional enrichment can continue.

When a request is unsafe without an authoritative fact, the application must
load, authorize, and validate that fact before calling `ChatClient`. A query
error, timeout, not-found result, or authorization failure must prevent the
model invocation at that application service boundary.

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
