Horizon Monorepo Scaffold

This workspace contains initial scaffolding for three independent projects described in `test.md`:

- app-platform/ (Project A): Spring Boot `core-service` with placeholder Angular build module and optional Electron desktop viewer.
- target-platform/ (Project B): Spring Boot `tp-server` with stub APIs and example config.
- registry-server/ (Project C): Spring Boot registry API with basic endpoints.

Notes
- Network is restricted, so Maven/Node builds are not executed here.
- Angular app is a placeholder; build should copy its `dist/` into `app-platform/core-service/src/main/resources/static/`.
- Health endpoints are implemented at `/api/health` for each service.

Setup & Usage
- See docs/SETUP.md for end-to-end setup, running, and usage instructions.

Docs Index
- docs/OVERVIEW.md — components and flow
- docs/API.md — REST endpoints
- docs/CONFIG.md — configuration reference
- docs/DEVELOPMENT.md — build and dev workflows
- docs/SCRIPTS.md — helper scripts

Next Steps
- Populate Angular UI and wire `ui-angular-build` to build+copy assets.
- Flesh out TargetPlatform services for plugin/program management.
- Implement storage and catalog management in Registry Server.


TargetPlatform Config & Persistence
- Default home: `${user.home}/.local/share/TargetPlatform`
- Default config path: `${tp.home}/config/tp-config.yml` (overridable via `tp.configPath`)
- Example config: `target-platform/tp-server/src/main/resources/tp-config.example.yml`
- On startup, TP will:
  - Load saved state from `${tp.home}/state/{programs.json,plugins.json}`
  - Merge and install from YAML config if present
  - If `autoStart: true`, start programs and plugins

Registry Signing & Sync
- Registry server (Project C) supports signed catalog endpoints:
  - JSON: `/api/catalog.json`
  - Signature (Base64 RSA SHA-256): `/api/catalog.sig`
- Configure Registry signing with PKCS8 PEM private key: `registry.signing.privateKeyPath` in `registry-server`.
- Configure TP verification and sync:
  - `tp.keys.publicKeyPath` → X.509 PEM public key matching registry private key
  - `tp.registry.url` → e.g., `http://127.0.0.1:8082/api`
  - `tp.registry.autoSyncOnStart` → `true` to pull on startup
- Manual sync endpoint in TP: `POST /api/registry/sync`
