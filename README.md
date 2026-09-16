# 🧠 AI Context Orchestrator

[English](README.md) | [简体中文](README.zh-CN.md)

> A declarative business-context orchestration layer for Spring AI.
>
> Let applications declare only the business context AI needs, while the framework handles planning, loading, conflict resolution, budget selection, and final injection.

---

## ✨ Vision

In real-world applications, AI responses often depend on more than conversation history. They may also need:

- current user profiles;
- database facts such as products, orders, and inventory;
- session state stored in Redis;
- vector retrieval results;
- business rules and authorization context;
- data provided by other internal services.

If every AI endpoint manually queries and concatenates this context, the code quickly becomes repetitive, difficult to test, and difficult to extend.

AI Context Orchestrator (ACO) extracts that responsibility into a dedicated context orchestration layer:

```text
Application declares ContextSource
        ↓
ACO plans which context should load
        ↓
ContextSource instances execute concurrently
        ↓
Authority / Freshness resolve conflicts
        ↓
Priority / Budget select retained context
        ↓
Structured ContextAssembly is built
        ↓
Spring AI Advisor injects it into the model request
```

---

## 🚀 Features

| Capability | Status | Notes |
|---|---|---|
| Provider-neutral Core | ✅ Complete | Core does not depend on Spring AI |
| ContextSource SPI | ✅ Complete | Application extension point for business context |
| Planning | ✅ Complete | Decides LOAD / DEFER / SKIP |
| Execution | ✅ Complete | Concurrent loading, timeout, and failure isolation |
| Resolution | ✅ Complete | Authority / Freshness conflict handling |
| Budget | ✅ Complete | Required protection + Priority-based selection |
| Structured Assembly | ✅ Complete | Keeps context structured until the model boundary |
| Spring AI Advisor Bridge | ✅ Complete | Appends business context automatically |
| Spring Boot AutoConfiguration | ✅ Complete | Builds the default runtime graph |
| Spring Boot Starter | ✅ Consumer path verified | Fake ChatModel E2E covers auto-configuration and final Prompt |
| Runnable Quickstart | ✅ Complete | Offline executable example covers the real Starter consumer path |
| Spring AI compatibility | ✅ Verified | Minimum 1.0.0; current default 1.1.8 |

---

## 🧱 Architecture

Core pipeline:

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

`ai-context-core` remains a single Maven module. Cross-stage contracts stay in
`io.github.forgottenlab.aicontext.core`, while stage-specific types live in the
`planning`, `execution`, `resolution`, `budget`, and `assembly` packages.

Detailed documentation:

- [Architecture](docs/architecture/ARCHITECTURE.md)
- [Class Map](docs/architecture/CLASS_MAP.md)

---

## 📦 Usage

Current target consumer experience:

```xml
<dependency>
    <groupId>io.github.forgottenlab.aicontext</groupId>
    <artifactId>ai-context-spring-boot-starter</artifactId>
    <version>${ai-context.version}</version>
</dependency>
```

Applications only need to provide their own `ContextSource`:

```java
@Component
class ProductContextSource implements ContextSource {

    @Override
    public String id() {
        return "product";
    }

    @Override
    public CompletableFuture<ContextContribution> load(ContextRequest request) {
        // Load facts from an application Service / Repository and return ContextItem values.
        ...
    }
}
```

Then continue using Spring AI normally:

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

ACO does not require application code to manually invoke Planner, Executor, Resolver, Budgeter, or Assembler.

---

## ⚙️ Configuration

Current public budget configuration:

```yaml
forgottenlab:
  ai:
    context:
      enabled: true
      budget:
        max-context-tokens: 12000
```

Note:

`max-context-tokens` is the current public configuration name, while the default
`ContextCostEstimator` is still a deterministic, tokenizer-neutral estimator.

Current default semantics:

```text
1 rendered Unicode code point = 1 cost unit
```

Applications that need exact model token accounting can replace `ContextCostEstimator`.

---

## 🧪 Testing

Current baseline:

| Module | Tests | Status |
|---|---:|---|
| Core | 89 | ✅ |
| Spring AI | 18 | ✅ |
| Spring Boot AutoConfigure | 10 | ✅ |
| Spring Boot Starter | 1 | ✅ |
| Quickstart | 1 | ✅ |
| **Total** | **119** | **✅** |

Run:

```powershell
mvn test
```

The standalone external-consumer fixture verifies Spring AI `1.0.0` and
`1.1.8`. It is intentionally outside the reactor, so its test is not included
in the 119-test total. GitHub Actions runs the reactor, packages and executes
the offline Quickstart, and checks both compatibility anchors.

Detailed documentation:

- [Testing](docs/project/TESTING.md)

---

## 📖 Documentation

| Document | English | 简体中文 |
|---|---|---|
| Architecture | [ARCHITECTURE.md](docs/architecture/ARCHITECTURE.md) | [ARCHITECTURE.zh-CN.md](docs/architecture/ARCHITECTURE.zh-CN.md) |
| Class Map | [CLASS_MAP.md](docs/architecture/CLASS_MAP.md) | [CLASS_MAP.zh-CN.md](docs/architecture/CLASS_MAP.zh-CN.md) |
| Getting Started | [GETTING_STARTED.md](docs/guides/GETTING_STARTED.md) | [GETTING_STARTED.zh-CN.md](docs/guides/GETTING_STARTED.zh-CN.md) |
| Testing | [TESTING.md](docs/project/TESTING.md) | [TESTING.zh-CN.md](docs/project/TESTING.zh-CN.md) |
| Roadmap | [ROADMAP.md](docs/project/ROADMAP.md) | [ROADMAP.zh-CN.md](docs/project/ROADMAP.zh-CN.md) |
| Safety Notes | [SAFETY.md](docs/project/SAFETY.md) | [SAFETY.zh-CN.md](docs/project/SAFETY.zh-CN.md) |

---

## 🔐 Safety Notes

ACO handles business context, so applications should pay particular attention to:

- do not inject passwords, tokens, private keys, or other sensitive values into models without an explicit need;
- `ContextSource` implementations must respect the application's existing authorization boundaries;
- `ContextPriority.REQUIRED` protects resolved context from budget removal; it does not require a source load to succeed;
- validate business-critical authoritative ground truth before invoking `ChatClient` when the request must fail closed;
- prefer explicit read-only database queries over unrestricted NL2SQL;
- the `<business-context>` envelope reduces accidental instruction confusion but is not a complete prompt-injection defense;
- do not log raw sensitive business context by default.

Detailed documentation:

- [Safety Notes](docs/project/SAFETY.md)

---

## 🗺️ Roadmap

The real Starter consumer experience has already been verified by a Fake ChatModel E2E:

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
Final Prompt contains <business-context>
```

The [Runnable Offline Quickstart](examples/quickstart/README.md) implements the
same path as a non-web Spring Boot application. It uses a local `ChatModel` to
capture the final Prompt and requires no API key, model provider, or runtime
network access.

The next stage is real-application dogfooding. Core remains frozen unless real
consumer usage exposes a concrete contract gap.

Detailed roadmap:

- [Roadmap](docs/project/ROADMAP.md)

---

## 📜 License

This project is licensed under the [Apache License 2.0](LICENSE).
