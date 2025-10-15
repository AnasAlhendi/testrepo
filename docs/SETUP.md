Horizon Setup Guide

Prerequisites
- Java 17+ (Temurin recommended)
- Maven 3.9+
- Node 18+ and npm (for Angular UI)
- Angular CLI (for local dev): npm i -g @angular/cli
- Windows PowerShell (scripts provided)

Directory Layout
- App Platform (Project A)
  - Spring Boot core: app-platform/core-service
  - Angular UI: app-platform/ui-angular
  - UI copy module: app-platform/ui-angular-build
  - Electron viewer (optional): app-platform/desktop-electron
- Target Platform (Project B): target-platform/tp-server
- Registry Server (Project C): registry-server

1) Generate Keys (Registry signing / TP verification)
- Run: scripts/generate-keys.ps1
- Outputs:
  - registry-private.pem (PKCS8 RSA, for Registry)
  - tp-public.pem (X.509 RSA, for TP)

2) Run Registry Server (port 8082)
- Command:
  - scripts/run-registry.ps1 -StorageDir "$env:USERPROFILE/.local/share/RegistryServer" -PrivateKeyPath "$env:USERPROFILE/.local/share/RegistryKeys/registry-private.pem"
- Endpoints:
  - GET /api/catalog.json — canonical catalog
  - GET /api/catalog.sig — Base64 signature of catalog.json (if key configured)
  - POST /api/programs/upload — multipart: id, version, file (JAR)
  - POST /api/plugins/upload — multipart: id, version, file (ZIP)
  - GET /api/files/** — serves uploaded files from storage dir

3) Prepare Target Platform (TP) Home
- Command:
  - scripts/setup-tp-home.ps1 -TpHome "$env:USERPROFILE/.local/share/TargetPlatform" -RegistryUrl "http://127.0.0.1:8082/api" -PublicKeyPath "$env:USERPROFILE/.local/share/RegistryKeys/tp-public.pem" -AutoSyncOnStart
- Creates directory structure and seeds config file: ${TP_HOME}/config/tp-config.yml

4) Run Target Platform (port 19080)
- Command:
  - scripts/run-tp.ps1
- Key endpoints:
  - GET /api/health
  - Programs: GET /api/programs, POST /api/programs/install|start|stop|restart
  - Plugins: GET /api/plugins, POST /api/plugins/install|start|stop|uninstall
  - Logs: GET /api/logs?target=server|program:<id>|plugin:<id>&lines=200
  - Logs stream (SSE): GET /api/logs/stream?target=...
  - Registry sync: POST /api/registry/sync
- Persistence:
  - ${TP_HOME}/state/{programs.json, plugins.json}
- Hot reload:
  - Watches ${TP_HOME}/config/tp-config.yml; applies changes and auto-starts if enabled

5) Build and Run App Platform (UI + Core)
- Option A: Production build and serve via core-service
  1. Build Angular UI (requires network):
     - cd app-platform/ui-angular
     - npm ci
     - npm run build
  2. Copy UI into core-service static:
     - mvn -pl ui-angular-build -Pprod package
  3. Run core-service:
     - scripts/run-core-service.ps1
  4. Open http://127.0.0.1:18080/

- Option B: Angular dev server (proxy to TP/Registry)
  1. Start TP (19080) and Registry (8082)
  2. cd app-platform/ui-angular
  3. ng serve
  4. Dev proxy mapping:
     - /api/tp → http://127.0.0.1:19080/api
     - /api/registry → http://127.0.0.1:8082/api

6) Upload and Install a Program (example)
- Upload to Registry:
  - POST /api/programs/upload (id, version, file=your.jar)
- In App UI → Store page:
  - Click "Install" next to the program to install into TP
  - Or click "Sync from Registry" to pull signed catalog and install

Notes & Limitations
- Angular build requires network to fetch dependencies. A placeholder dist is provided so Maven copy works before the first real build.
- Maven artifact resolution in TP is stubbed for offline; prefer using source.url (from Registry) or source.path.
- PF4J runtime in App Platform is not yet integrated; plugins are tracked but not executed.

Troubleshooting
- Ports in use: adjust ports in application.yml files if needed
- Signature errors: verify Registry private key and TP public key match
- File access: ensure storage and TP_HOME directories exist and are writable

