Horizon Overview

Components
- App Platform (Core): Serves Angular UI on `:18080`, proxies to TP/Registry, exposes `/api/ui-config` and `/api/plugins`.
- Target Platform (TP): Manages Programs and Plugins on `:19080`, exposes control and logs APIs.
- Registry Server: Stores catalog and artifacts on `:8082`, supports signing and file serving.

Ports
- Core: 18080
- TP: 19080
- Registry: 8082

High-Level Flow
- Registry hosts a signed catalog (`/api/catalog.json` + `/api/catalog.sig`).
- TP optionally verifies the catalog (public key) and can auto-sync and install entries.
- Core UI interacts with TP and Registry through Core’s proxy (`/api/tp/**`, `/api/registry/**`).

Data & Paths
- TP home: `${user.home}/.local/share/TargetPlatform` (config, state, downloads, logs)
- Registry storage: `${user.home}/.local/share/RegistryServer` (uploaded files, catalog, signatures)
- Core served UI: `app-platform/core-service/src/main/resources/static/` (Angular build copied here)

Security (Optional)
- Registry: API key headers; private key for signing catalog.
- TP: API key headers; public key to verify Registry signatures.

