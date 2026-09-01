# 🔐 Safety Notes

[English](SAFETY.md) | [简体中文](SAFETY.zh-CN.md)

- Do not inject secrets, passwords, private keys, or unrestricted tokens into model context.
- `ContextSource` implementations must respect application authorization boundaries.
- Prefer read-only and explicit database access.
- Avoid unrestricted NL2SQL.
- Do not log sensitive raw context by default.
- `<business-context>` reduces instruction confusion but is not a complete prompt-injection defense.