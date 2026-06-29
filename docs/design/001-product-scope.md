# Product Scope

## Problem

Developers building edge, router, ISP, IoT, cybersecurity, and device-intelligence systems often need to test workflows involving device onboarding, Wi-Fi telemetry, DNS policy decisions, firmware update checks, telemetry ingestion, QoE scoring, and device risk scoring.

These workflows are hard to reproduce locally because they often require real routers, lab devices, staging environments, production-like telemetry, ISP backend systems, custom mocks, and manual setup.

## Solution

OpenEdgeStack provides a local emulator that exposes simple APIs and scenario files for testing these workflows.

## MVP scope

Version 0.1 includes:

- household registry
- device registry
- DNS policy simulation
- firmware update check simulation
- Wi-Fi telemetry ingestion
- basic QoE scoring
- basic risk scoring
- health endpoint
- in-memory storage
