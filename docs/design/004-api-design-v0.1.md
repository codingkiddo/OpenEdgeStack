# API Design v0.1

## System APIs

```http
GET /_oes/health
POST /_oes/reset
```

## Household APIs

```http
POST /households
GET /households
GET /households/{householdId}
```

## Device APIs

```http
POST /devices
GET /devices/{deviceId}
GET /households/{householdId}/devices
DELETE /devices/{deviceId}
```

## DNS APIs

```http
POST /dns/query
```

## Firmware APIs

```http
POST /firmware/releases
GET /firmware/check?deviceId=router-001
```

## Telemetry APIs

```http
POST /telemetry/wifi
```

## Scoring APIs

```http
GET /devices/{deviceId}/qoe-score
GET /devices/{deviceId}/risk-score
```
