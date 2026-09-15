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
