# Domain Model

## Household

```json
{
  "householdId": "home-001",
  "name": "Demo Home",
  "region": "IN"
}
```

## Device

```json
{
  "deviceId": "router-001",
  "householdId": "home-001",
  "type": "ROUTER",
  "vendor": "OpenEdge",
  "model": "OES-Gateway-1",
  "firmwareVersion": "1.0.0",
  "status": "ONLINE"
}
```

## Wi-Fi telemetry

```json
{
  "deviceId": "phone-001",
  "rssi": -68,
  "snr": 25,
  "latencyMs": 42,
  "packetLossPercent": 1.5,
  "retryRatePercent": 8.2,
  "rxMbps": 120,
  "txMbps": 35,
  "timestamp": "2026-06-29T10:00:00Z"
}
```
