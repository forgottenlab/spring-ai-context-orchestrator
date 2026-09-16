# 🔐 Safety Notes

[English](SAFETY.md) | [简体中文](SAFETY.zh-CN.md)

- Do not inject secrets, passwords, private keys, or unrestricted tokens into model context.
- `ContextSource` implementations must respect application authorization boundaries.
- Prefer read-only and explicit database access.
- Avoid unrestricted NL2SQL.
- Do not log sensitive raw context by default.
- `<business-context>` reduces instruction confusion but is not a complete prompt-injection defense.

## 🛑 Authoritative Ground Truth and Fail-Closed Requests

`ContextPriority.REQUIRED` means that an already-resolved candidate cannot be
silently removed by budget selection. It does not mean that its
`ContextSource` must execute successfully. `ContextExecutor` intentionally
isolates source errors and timeouts in `ContextExecutionReport`, and the
Advisor continues with successful contributions.

For business-critical authoritative ground truth, fail-closed behavior belongs
at the application service boundary. Load, authorize, and validate the required
business entity before invoking `ChatClient`. If that query fails or times out,
the entity is not found, or authorization fails, the model must not be invoked.

```text
Authoritative business validation succeeds -> ChatClient may be invoked
Authoritative business validation fails    -> model invocation is forbidden
```

ACO retains generic failure isolation for optional and enrichment context; it
does not make every `ContextSource` failure globally fail closed.
