Project A – App Platform (Frontend: Angular + PrimeNG, Backend: Spring Boot, optional desktop viewer)

Project B – TargetPlatform (standalone server that can run/monitor programs & PF4J plugins, and can also run Project A)

Project C – Registry Server (independent backend to upload/manage plugin/program bundles and a catalog)

Use this as your canonical build/run document.

INSTALL_AGENT_INSTRUCTION.md
0) Scope & Outcomes

Create three independent repositories:

app-platform/ (Project A)

Spring Boot Core Service (serves API + static Angular build)

Angular + PrimeNG UI

Optional Desktop (Electron) that only opens the already running web UI

target-platform/ (Project B)

Spring Boot TP Server that downloads/installs/starts/stops:

Programs (JAR processes)

PF4J Plugins (zip bundles)

Optionally runs Project A as one of the Programs

Single YAML config (tp-config.yml) drives what to run

registry-server/ (Project C)

Spring Boot Registry API for uploading and listing plugins/programs

Stores artifacts (ZIP/JAR) and a signed catalog.json

Not wired to TargetPlatform initially; used later by either A or B

Non-goals (now): coupling Registry ↔ TargetPlatform; auto-publishing from A to C.

1) Prerequisites

Java 17+ (Temurin recommended)

Maven 3.9+

Node 18+ / npm

Angular CLI

Git

(Optional) Electron 31+

OS: Linux/macOS/Windows

2) Repository A – app-platform/
   2.1 Structure
   app-platform/
   ├─ pom.xml
   ├─ core-api/
   ├─ plugin-api/                 # optional (PF4J contracts)
   ├─ plugin-runtime/             # optional (PF4J loader/sandbox)
   ├─ core-service/               # Spring Boot + serves Angular dist
   │  └─ src/main/resources/static/   # Angular dist copied here
   ├─ ui-angular/                 # Angular + PrimeNG app
   ├─ ui-angular-build/           # Maven wrapper to build/copy Angular
   ├─ desktop-electron/           # optional desktop viewer (no background core)
   └─ docs/ scripts/ ci/

2.2 Parent POM (app-platform/pom.xml)

Packaging pom

Modules: core-api, plugin-api (opt), plugin-runtime (opt), core-service, ui-angular-build, desktop-electron (opt)

Import Spring Boot deps via dependencyManagement

Set Java 17

2.3 Core Service (Spring Boot)

Bind to 127.0.0.1:18080 (prod)

Serve UI from resources/static/

Expose REST under /api/**

If PF4J used inside A:

Load plugins from ${APPDATA_DIR}/plugins

SQLite per plugin at ${APPDATA_DIR}/plugins/<id>/data/<id>.db with WAL

application-prod.yml

server:
address: 127.0.0.1
port: 18080
servlet:
context-path: /
spring:
jackson:
serialization:
WRITE_DATES_AS_TIMESTAMPS: false

2.4 Angular + PrimeNG

Routes: /home, /programs, /store, /settings, /logs, /plugin/:id

Use lazy loading and OnPush change detection

Virtual scroll / paging in lists

Build production only

UI build via Maven wrapper (ui-angular-build/pom.xml)

frontend-maven-plugin to run npm ci + npm run build

maven-resources-plugin to copy ui-angular/dist/* → core-service/src/main/resources/static/

2.5 Optional Desktop (Electron)

Viewer only: loads http://127.0.0.1:18080/

Does not start/stop Core

Allow overriding URL via YOURAPP_URL env

2.6 Build & Run (Project A)
# Build Angular + Core
mvn -pl ui-angular-build,core-service -Pprod clean package

# Run core (server-only)
java -Xmx512m \
-DAPPDATA_DIR="$HOME/.local/share/YourApp" \
-Dpf4j.pluginsDir="$HOME/.local/share/YourApp/plugins" \
-jar core-service/target/core-service.jar

# Open web UI
# http://127.0.0.1:18080/


Electron (optional):

npm --prefix desktop-electron ci
YOURAPP_URL=http://127.0.0.1:18080 npm --prefix desktop-electron run build

3) Repository B – target-platform/ (TargetPlatform Server)

A central server that can run:

“Programs” = arbitrary JAR processes

“Plugins” = PF4J zip bundles (plugin.json + plugin.jar)

Optionally Project A (core-service.jar) as a managed program

3.1 Structure
target-platform/
├─ pom.xml
├─ tp-server/                       # Spring Boot
│  ├─ src/main/java/com/yourco/tp/{controller,service,util}
│  └─ src/main/resources/{application.yml, tp-config.example.yml}
├─ tp-ui/                           # optional admin UI
└─ orchestrator/                    # optional CLI

3.2 Runtime Home (TP_HOME)
TP_HOME/
├─ config/tp-config.yml
├─ plugins/<pluginId>/versions/<ver>/{plugin.json, plugin.jar, ...}
├─ programs/<programId>/versions/<ver>/<artifact>.jar
├─ downloads/
├─ logs/{tp-server.log, program-*.log}
└─ keys/public.pem   # optional for signature verification

3.3 Config File (tp-config.yml)
tpHome: "~/.local/share/TargetPlatform"
autoStart: true

plugins:
- id: "analytics"
  source:
  maven: "com.acme.plugins:analytics-plugin:1.2.0:zip:plugin"
  verifySignature: true

- id: "etl"
  source:
  url: "https://cdn.example.com/plugins/etl/2.0.1/etl-2.0.1.zip"

programs:
- id: "core-service"  # Project A managed by TargetPlatform
  source:
  maven: "com.yourco.app:core-service:1.0.0:jar"
  jvmArgs:
    - "-Xmx512m"
    - "-DAPPDATA_DIR=~/.local/share/YourApp"
    - "-Dpf4j.pluginsDir=~/.local/share/YourApp/plugins"
      args: []
      health:
      http: "http://127.0.0.1:18080/api/health"
      timeoutSec: 30

- id: "reporting-service"
  source:
  maven: "com.acme.services:reporting:1.4.0:jar"
  jvmArgs: ["-Xmx256m"]
  args: ["--port=19090"]
  health:
  http: "http://127.0.0.1:19090/health"
  timeoutSec: 30


Supported sources:

maven: "group:artifact:version:type[:classifier]"

url: "https://...zip|jar"

path: "/abs/path/to/file.jar|.zip"

3.4 TP REST API (essentials)

GET /api/health

GET /api/plugins

POST /api/plugins/install|start|stop|update|uninstall

GET /api/programs

POST /api/programs/install|start|stop|restart

GET /api/logs?target=server|plugin:<id>|program:<id>

3.5 Services (internals)

Downloader: resume, SHA-256, (optional) signature verify

ZipSafe: anti Zip Slip/Bomb

MavenResolver: resolve & fetch artifacts to downloads/

PluginManager: PF4J load/start/stop from TP_HOME/plugins

ProgramSupervisor: spawn JARs, capture logs, health check, restart policy

ConfigService: apply tp-config.yml at startup (autoStart)

AuditLog: INSTALL/START/STOP/UPDATE/CRASH entries

3.6 Build & Run (Project B)
mvn -pl tp-server -Pprod clean package

java -Xmx512m \
-DTP_CONFIG="$HOME/.local/share/TargetPlatform/config/tp-config.yml" \
-jar tp-server/target/tp-server.jar
# TP REST e.g. on 127.0.0.1:18085


To run Project A via TargetPlatform, publish/install core-service (Maven local/Nexus/GH Packages) and add it under programs: in tp-config.yml (as shown).

4) Repository C – registry-server/ (Upload & Catalog)

A neutral storage/control plane for uploading plugin/program bundles and maintaining a signed catalog.

4.1 Structure
registry-server/
├─ pom.xml
├─ src/main/java/com/yourco/registry/{controller,service,repo,security}
├─ src/main/resources/application.yml
└─ storage/                      # local store (or use S3/MinIO)

4.2 REST (minimal)

POST /v1/upload
Body multipart: { file: (zip/jar), type: "plugin|program", meta: json }
Response: { id, url, sha256, size, createdAt }

GET /v1/catalog
Returns catalog.json (optionally signed).
Include entries with: { id, name, type, version, url, sha256, size, createdAt }

GET /v1/artifacts/{id} (optional direct download proxy)

Keep this service independent now. Later, A or B can read catalog.json and fetch from the artifact url.

4.3 Signing (optional but recommended)

Generate Ed25519 or RSA-3072 keypair offline

Store public key in A or B (for verification)

When catalog.json is updated, produce:

catalog.json.sig (detached signature)

catalog.sha256

Provide signature URLs alongside catalog

5) PF4J Plugin Bundle Format (for both A & B use-cases)

ZIP Layout

<pluginId>-<version>.zip
├─ plugin.json
├─ plugin.jar
├─ migrations/ (optional for SQLite)
└─ assets/    (optional)


plugin.json

{
"id": "example-plugin",
"name": "Example",
"version": "1.0.0",
"entry": "com.yourco.plugins.example.Main",
"coreRange": ">=1.0.0",
"permissions": { "net": false, "fs": "sandbox" },
"db": { "type": "sqlite", "name": "example.db" }
}


Safety

Enforce schema on plugin.json

Before unpack: SHA-256 & signature (if enabled)

During unpack: ZipSafe checks (no path traversal, file count/size ratios)

6) Data Layouts & Policies
   6.1 App Platform (Project A)
   $APPDATA_DIR/
   ├─ cache/catalog.json
   ├─ downloads/
   ├─ plugins/<id>/{versions,data,backups,logs,temp}
   ├─ keys/public.pem
   ├─ logs/{core.log,http.log,audit.log}
   └─ config/settings.json


SQLite per plugin with WAL

Periodic VACUUM (weekly), ANALYZE (monthly)

Backups before migrations (keep last 3)

Disk guard: warn < 1GB free; block heavy writes < 100MB free

6.2 TargetPlatform (Project B)
TP_HOME/
├─ config/tp-config.yml
├─ downloads/
├─ plugins/<id>/...
├─ programs/<id>/versions/<ver>/*.jar
├─ logs/{tp-server.log, program-*.log}
└─ keys/public.pem


Separate Program process memory budgets (-Xmx)

Health checks with timeouts

Graceful shutdown then kill

7) Security & Headers (Project A UI)

Serve Angular with security headers:

Content-Security-Policy (hash/nonce for inline if any)

X-Content-Type-Options: nosniff

Referrer-Policy: no-referrer

Disable framework “powered-by” headers

CORS: open only in dev (proxy), closed in prod

8) CI/CD (per repo)

Common steps

Java: mvn -Pprod -DskipTests clean package

Angular: npm ci && npm run build

Artifacts:

Repo A: core-service.jar (+ optional desktop installers)

Repo B: tp-server.jar

Repo C: registry-server.jar + generated catalog.json(.sig)

Optional

Publish artifacts to Maven repo (Nexus/GH Packages)

Sign artifacts (GPG/Ed25519 provenance)

Generate SBOM (CycloneDX)

9) Run Modes Summary

A alone (Web): run core-service.jar → browse http://127.0.0.1:18080/

A Desktop (optional): start electron viewer; no background core unless you started it

B alone: run tp-server.jar with TP_CONFIG → it can run any programs/plugins

B + A: B manages core-service as a program (start/stop/health/logs)

C: provides upload endpoints & catalog for plugins/programs (standalone)

10) Operational Checklists

Performance (A UI)

Lazy routes, OnPush, trackBy, virtual scroll

Production build with AOT/optimizer

Error codes (A & B)

DL_CHECKSUM_MISMATCH, SIGNATURE_INVALID, MANIFEST_INVALID,
ARCHIVE_PATH_TRAVERSAL, ARCHIVE_ZIP_BOMB, DB_MIGRATION_FAIL, DISK_LOW

Logs

Core: download start/finish (bytes, ms), signature OK/FAIL, install OK/FAIL

TP: program spawn/exit code, health failures, restart attempts

Audit: INSTALL/UPDATE/ROLLBACK/START/STOP

Backups

Button/endpoint for “Backup All” (zip: configs + dbs + logs)

Restore with checksum/signature validation

11) How TargetPlatform “links” with Project A (explicit steps)

Build/install A locally (or publish to Maven repo):

# in app-platform/
mvn -pl ui-angular-build,core-service -Pprod clean install
# creates com.yourco.app:core-service:1.0.0 in ~/.m2


Create tp-config.yml in B:

programs:
- id: "core-service"
  source:
  maven: "com.yourco.app:core-service:1.0.0:jar"
  jvmArgs:
    - "-Xmx512m"
    - "-DAPPDATA_DIR=~/.local/share/YourApp"
    - "-Dpf4j.pluginsDir=~/.local/share/YourApp/plugins"
      health:
      http: "http://127.0.0.1:18080/api/health"
      timeoutSec: 30


Run B:

# in target-platform/
mvn -pl tp-server -Pprod clean package
java -Xmx512m -DTP_CONFIG="$HOME/.local/share/TargetPlatform/config/tp-config.yml" -jar tp-server/target/tp-server.jar


Verify:

TP logs show it resolved and spawned core-service.jar

Browse http://127.0.0.1:18080/ (Project A web UI)

Use TP REST to check GET /api/programs

12) Notes & Future Extensions

Registry Server (C) remains independent; later connect A or B to it:

A (Store page) can list from /v1/catalog and install via URLs

B can install from url or maven sources (including Registry-provided URLs)

For desktop-only users, keep electron as a viewer; do not autostart services

Consider adding RBAC + API keys on TP REST if exposed beyond localhost

Consider Nginx/Traefik TLS if exposing A web over LAN/WAN

Done.

This instruction stands alone. Follow it to scaffold, build, run, and later extend the system with more programs/plugins—without coupling TargetPlatform and Registry at the start, and with App Platform always usable by itself (web/desktop).