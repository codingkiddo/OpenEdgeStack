# Testing Strategy

The first testing goal is deterministic API behavior.

Test types:

- unit tests for scoring and validation
- controller tests for HTTP APIs
- integration tests for full flows
- future Testcontainers tests

First end-to-end flow:

1. Create household
2. Register router
3. Send Wi-Fi telemetry
4. Query QoE score
5. Query DNS policy
6. Check firmware update
