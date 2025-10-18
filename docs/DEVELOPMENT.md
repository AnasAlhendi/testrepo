Development Guide

Prerequisites
- Java 17+, Maven 3.9+
- Node 18+, npm, Angular CLI for UI dev

Build (Production UI served by Core)
1) Build Angular
   - `cd app-platform/ui-angular`
   - `npm ci`
   - `npm run build`
2) Copy UI into Core static via Maven wrapper
   - `mvn -pl app-platform/ui-angular-build -Pprod package`
3) Package Core and run
   - `mvn -pl app-platform/core-service -DskipTests package`
   - `scripts/run-core-service.ps1`

Run TP and Registry (Dev)
- Registry: `scripts/run-registry.ps1 -StorageDir "$env:USERPROFILE/.local/share/RegistryServer" -PrivateKeyPath <path-to/registry-private.pem>`
- TP: `scripts/run-tp.ps1`

Angular Dev Server (Hot reload, proxied APIs)
1) Start TP (19080) and Registry (8082)
2) `cd app-platform/ui-angular`
3) `ng serve`
4) Proxy paths used by the UI:
   - `/api/tp` → `http://127.0.0.1:19080/api`
   - `/api/registry` → `http://127.0.0.1:8082/api`

Useful Notes
- Network is restricted in CI; local dev will resolve Maven/Node dependencies.
- Production serving of UI uses Spring static resources in Core.
- The TP runtime currently manages lifecycle and logs; PF4J plugin execution integration is TBD.

