# Project Context

## Problem

Spring AI already provides model clients, advisors, memory, tool calling, vector stores, and multimodal primitives. Application developers still spend significant time deciding how business context should be retrieved and assembled for each model call.

## Project responsibility

This project owns the layer between business data sources and Spring AI:

- declare context sources;
- load context when needed;
- preserve source identity and metadata;
- apply field/security boundaries;
- compare authority and freshness;
- enforce context budgets;
- resolve conflicts;
- assemble context into Spring AI requests/advisors.

## Non-goals

- reimplement model SDKs;
- reimplement Spring AI;
- reimplement JDBC/MyBatis/JPA;
- reimplement vector databases;
- create unrestricted NL2SQL;
- hide every Spring AI feature from advanced users.

## API philosophy

Complexity should be progressively disclosed:

1. configuration-only quick start;
2. annotations;
3. Java API;
4. custom SPI.

All entry points should converge on one internal context model.