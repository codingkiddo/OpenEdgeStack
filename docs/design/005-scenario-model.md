# Scenario Model

Scenarios are future YAML files used to simulate common home network conditions.

Example future scenario:

```yaml
name: poor-wifi-bedroom
household:
  householdId: home-001
  name: Demo Home
  region: IN
devices:
  - deviceId: router-001
    type: ROUTER
    vendor: OpenEdge
    model: OES-Gateway-1
    firmwareVersion: 1.0.0
telemetry:
  wifi:
    - deviceId: router-001
      rssi: -78
      snr: 14
      latencyMs: 120
      packetLossPercent: 3.5
      retryRatePercent: 22.0
      rxMbps: 18
      txMbps: 4
```

Scenario runner is not included in v0.1.
