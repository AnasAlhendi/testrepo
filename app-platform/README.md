App Platform (Project A)

This repository contains the Spring Boot core service intended to serve an Angular UI and expose REST APIs.

Modules
- core-service: Spring Boot service exposing /api/** and serving static UI from resources/static/
- ui-angular-build: placeholder module for building/copying Angular dist into core-service
- ui-angular: Angular + PrimeNG app (scaffolded). Build output goes to `dist/ui-angular`.

Build (server only)
- mvn -pl ui-angular-build,core-service -Pprod clean package

Run
- java -Xmx512m -DAPPDATA_DIR="%USERPROFILE%/.local/share/YourApp" -Dpf4j.pluginsDir="%USERPROFILE%/.local/share/YourApp/plugins" -jar core-service/target/core-service.jar

UI
- When built, UI assets should reside in core-service/src/main/resources/static/

Angular + PrimeNG
- Location: `app-platform/ui-angular`
- Routes: /home, /programs, /store, /settings, /logs, /plugin/:id
- Change detection: OnPush; lazy-loaded standalone components
- Build locally (requires network):
  - npm ci
  - npm run build
  - Then re-run Maven copy: `mvn -pl ui-angular-build -Pprod package`
