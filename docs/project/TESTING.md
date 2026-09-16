# 🧪 Testing

[English](TESTING.md) | [简体中文](TESTING.zh-CN.md)

## ✅ Current Baseline

| Module | Tests |
|---|---:|
| Core | 89 |
| Spring AI | 18 |
| Spring Boot AutoConfigure | 10 |
| Spring Boot Starter | 1 |
| Quickstart | 1 |
| Total | 119 |

The Starter test is a Spring Boot consumer E2E using a fake `ChatModel`. It
verifies the auto-configured `ChatClient.Builder`, automatically registered ACO
Advisor, preserved system message, and appended business context without an API
key or network call.

The Quickstart test starts the runnable consumer application and verifies that
its injected `ChatClient.Builder` drives one source load and one offline model
call. It asserts that the captured Prompt preserves the system and user
messages and contains exactly one enriched business-context envelope.

## 🔁 Spring AI Compatibility

The independent `compatibility/spring-ai-consumer` fixture behaves like an
external application: it depends on the installed ACO Starter, contributes one
generic `ContextSource`, obtains the auto-configured `ChatClient.Builder`, and
uses an in-memory `ChatModel` to inspect the final Prompt.

Verified anchors:

| Spring AI | Status |
|---|---|
| 1.0.0 | ✅ PASS |
| 1.1.8 | ✅ PASS (current default) |

The minimum supported Spring AI version is `1.0.0`; the default dependency
baseline remains `1.1.8`. The fixture is outside the root reactor, so its one
test is not part of the 119-test reactor count.

```powershell
mvn -DskipTests install
mvn -f .\compatibility\spring-ai-consumer\pom.xml "-Dspring-ai.version=1.0.0" clean test
mvn -f .\compatibility\spring-ai-consumer\pom.xml "-Dspring-ai.version=1.1.8" clean test
```

## 🤖 Continuous Integration

GitHub Actions runs the default reactor tests, packages the repository, executes
the packaged offline Quickstart, and tests the external consumer fixture against
both verified Spring AI anchors. CI uses Java 17 and requires no model provider
or secret.

Run:

```powershell
mvn test
```

Before commit:

```powershell
git diff --check
mvn test
git status
```

The Maven compiler deprecation warning is currently known and intentionally kept separate from functional commits.
