# OpenEdgeStack

OpenEdgeStack is a local emulator for router, IoT, ISP, cybersecurity, firmware, and device-intelligence platforms.

> LocalStack for edge-device intelligence platforms.

## What it emulates

The first version focuses on generic, vendor-neutral workflows:

- Household registry
- Device registry
- DNS security policy simulation
- Firmware update checks
- Wi-Fi telemetry ingestion
- Basic QoE scoring
- Basic risk scoring

## Run locally

```bash
cd edge-emulator-server
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/_oes/health
```

## Example flow

```bash
curl -X POST http://localhost:8080/households \
  -H "Content-Type: application/json" \
  -d '{"householdId":"home-001","name":"Demo Home","region":"IN"}'

curl -X POST http://localhost:8080/devices \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"router-001","householdId":"home-001","type":"ROUTER","vendor":"OpenEdge","model":"OES-Gateway-1","firmwareVersion":"1.0.0"}'

curl -X POST http://localhost:8080/telemetry/wifi \
  -H "Content-Type: application/json" \
  -d '{"deviceId":"router-001","rssi":-68,"snr":25,"latencyMs":42,"packetLossPercent":1.5,"retryRatePercent":8.2,"rxMbps":120,"txMbps":35,"timestamp":"2026-06-29T10:00:00Z"}'

curl http://localhost:8080/devices/router-001/qoe-score
```

## Open-source safety

This project intentionally uses fake schemas, fake devices, fake thresholds, and generic behavior. Do not add company-specific, customer-specific, or proprietary logic.

## License

Apache-2.0
