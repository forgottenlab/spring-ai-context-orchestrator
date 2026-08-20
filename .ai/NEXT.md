# Next

## Phase 0 - Foundation

- [x] Create Maven multi-module skeleton.
- [x] Separate core, Spring AI integration, Boot auto-configuration, and starter.
- [x] Establish first context domain types.
- [x] Establish safe project boundaries.
- [x] Add unit tests for core invariants.
- [ ] Design ContextSource loading strategies: ALWAYS / RELEVANT / ON_DEMAND / CUSTOM.
- [ ] Design Context Budget policy without coupling core to a tokenizer implementation.
- [ ] Design context arbitration rules for authority, freshness, relevance, and cost.
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