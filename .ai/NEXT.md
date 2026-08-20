# Next

## Phase 0 - Foundation

- [x] Create Maven multi-module skeleton.
- [x] Separate core, Spring AI integration, Boot auto-configuration, and starter.
- [x] Establish first context domain types.
- [x] Establish safe project boundaries.
- [x] Add unit tests for core invariants.
- [x] Design ContextSource loading strategies: ALWAYS / RELEVANT / ON_DEMAND / CUSTOM.
- [x] Establish ContextExecutor with timeout, concurrent loading, partial-failure isolation, and deterministic result ordering.
- [x] Establish tokenizer-neutral Context Budget policy and deterministic priority-based selection.
- [x] Establish structured Context Assembly without coupling core to Spring AI Message types.
- [x] Establish context arbitration identity and baseline authority/freshness conflict resolution.
- [ ] Extend arbitration with relevance/cost only if later stages demonstrate a real need.
- [ ] Decide YAML schema.
- [ ] Decide annotation API.
- [ ] Implement the first end-to-end quickstart.
- [ ] Add database context adapter using an existing Spring DataSource/JdbcTemplate.
- [ ] Add vector context adapter using Spring AI VectorStore.
- [ ] Add memory integration using Spring AI ChatMemory.
- [ ] Add optional Spring AI Alibaba compatibility module only after version-matrix tests exist.

## First validation scenarios

1. Product assistant:
   system instruction + memory + Chroma recall + MySQL ground truth + budget.
2. A second non-commerce scenario:
   prove the abstraction is not a renamed product-service architecture.