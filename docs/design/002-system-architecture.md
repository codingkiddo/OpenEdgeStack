# System Architecture

OpenEdgeStack starts as a Spring Boot modular monolith.

```text
edge-emulator-server
  ├── system
  ├── household
  ├── device
  ├── dns
  ├── firmware
  ├── telemetry
  └── scoring
```

## Runtime view

```text
Developer / Test
      |
      v
OpenEdgeStack REST API
      |
      v
Module Services
      |
      v
In-Memory StateStore
```

## Design principle

Each emulator module should be useful independently.

- Device Registry works without DNS
- DNS Policy works with simple device metadata
- Firmware Check works without telemetry
- QoE scoring improves when telemetry exists
