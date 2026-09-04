# Safety and Maintenance Rules

## Sensitive data

- Never commit API keys, passwords, access tokens, cookies, private keys,
  connection secrets, or production identifiers.
- `ContextSource` implementations must respect application authorization and
  tenant boundaries before returning model-facing context.
- Do not log raw sensitive business context by default. Prefer source IDs,
  execution outcomes, rejection reasons, and budget diagnostics.

## Data access

- ACO orchestrates context; it does not replace persistence or authorization
  frameworks.
- Database context is read-only by default.
- Do not execute destructive database operations from model-generated text.
- Any future NL2SQL capability must be opt-in and guarded by schema, table, and
  field allowlists, SELECT-only validation, row limits, timeouts, and read-only
  credentials.
- Fallback reads must always have an explicit bounded row limit.

## Model boundary

- Treat `<business-context>` as a structure that reduces instruction
  confusion, not as a complete prompt-injection defense.
- Do not send secrets or unrestricted internal data to a model merely because
  it can be represented as context.
- Starter and Quickstart validation must use a Fake/Stub `ChatModel`, require no
  API key, and perform no external model call.

## Architecture and compatibility

- Keep Core provider-neutral and independent from Spring AI.
- Keep Authority in resolution and Priority in budgeting.
- Keep typed context structured until the model-facing rendering boundary.
- Treat Core and public packages as frozen unless a verified consumer scenario
  demonstrates a concrete contract gap.
- Do not overwrite consumer-defined Spring beans when
  `ConditionalOnMissingBean` can provide a replaceable default.
- Do not expose ACO's private execution infrastructure as a generic application
  `Executor` bean.
- Avoid forcing optional model providers, databases, caches, or vector stores
  into the base Starter.
- Preserve the declared Java, Spring Boot, and Spring AI compatibility baseline
  unless a dedicated migration task changes it with version-matrix evidence.

## Change discipline

- Preserve unknown user changes; do not restore, reset, clean, or reformat them.
- Do not weaken or skip tests to obtain a green build.
- Keep architecture, JavaDoc, documentation, Starter E2E, package migration,
  build hygiene, and release work in separate tasks.
- Do not push, merge to `main`, create tags or releases, rewrite history, or
  perform remote/deployment actions without explicit authorization.
