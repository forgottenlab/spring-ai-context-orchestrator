# 🧪 Testing

[English](TESTING.md) | [简体中文](TESTING.zh-CN.md)

## ✅ Current Baseline

| Module | Tests |
|---|---:|
| Core | 89 |
| Spring AI | 18 |
| Spring Boot AutoConfigure | 10 |
| Total | 117 |

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