# Safety and Maintenance Rules

- Never commit API keys, passwords, tokens, cookies, or connection secrets.
- Do not execute destructive database operations from model-generated text.
- Database context is read-only by default.
- Any future NL2SQL capability must be opt-in and guarded by schema/table/field allowlists, SELECT-only validation, row limits, timeouts, and read-only credentials.
- Fallback table reads must always have a bounded row limit.
- Do not overwrite user-defined Spring beans when a ConditionalOnMissingBean strategy can be used.
- Avoid forcing optional providers or stores into the base starter.
- Preserve compatibility with the declared Java/Spring Boot/Spring AI baseline unless a migration task explicitly changes it.
- Before dependency upgrades, verify the Spring AI / Spring Boot / Spring AI Alibaba compatibility matrix.