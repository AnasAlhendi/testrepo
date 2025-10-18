Target Platform (Project B)

Spring Boot server that installs, runs, and supervises Programs (JARs) and PF4J Plugins.

Build
- mvn -pl tp-server clean package -DskipTests
- Optional prod build (activates Spring profile via resources):
  - mvn -pl tp-server -Pprod clean package -DskipTests

Run
- Basic:
  - java -Xmx512m -jar tp-server/target/tp-server-1.0.0-SNAPSHOT.jar
- With explicit config:
  - java -Xmx512m -DTP_CONFIG="%USERPROFILE%/.local/share/TargetPlatform/config/tp-config.yml" -jar tp-server/target/tp-server-1.0.0-SNAPSHOT.jar
- Maven run (download deps, then run in prod):
  - mvn -pl tp-server -Prun -DskipTests spring-boot:run
  - Notes: the `run` profile executes `dependency:go-offline` to fetch all dependencies, then runs the app with `spring.profiles.active=prod`.
- API:
  - Health: http://127.0.0.1:19080/api/health
  - Programs: GET/POST http://127.0.0.1:19080/api/programs/*
  - Plugins: GET/POST http://127.0.0.1:19080/api/plugins/*
  - Logs: GET http://127.0.0.1:19080/api/logs?target=server|program:<id>|plugin:<id>
  - Registry sync: POST http://127.0.0.1:19080/api/registry/sync

Prod Profile
- Building with `-Pprod` adds `src/prod/resources/application.properties` which sets `spring.profiles.active=prod`.
- `application-prod.yml` binds the server to 127.0.0.1:19080 and provides example `tp.*` defaults.

Security (API Keys)
- Disabled by default. Enable in config or env:
  - tp.security.enabled=true
  - tp.security.headerName=X-API-Key (default)
  - tp.security.readKeys=["READ_KEY_ABC"] (comma-separated via env)
  - tp.security.adminKeys=["ADMIN_KEY_DEF"]
- Usage: send header `X-API-Key: READ_KEY_ABC` (read-only) or `X-API-Key: ADMIN_KEY_DEF` (admin)
- Access rules:
  - GET /api/health → open
  - GET /api/** → READER or ADMIN
  - POST/other /api/** → ADMIN only

Quickstart tp-config.yml
Place at `%USERPROFILE%/.local/share/TargetPlatform/config/tp-config.yml` (Windows) or `~/.local/share/TargetPlatform/config/tp-config.yml` (Linux/macOS):

tpHome: "~/.local/share/TargetPlatform"
autoStart: true

programs:
  - id: "core-service"
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

plugins:
  - id: "example-plugin"
    source:
      url: "https://example.com/plugins/example-plugin-1.0.0.zip"

Registry & Signature (optional)
- Configure registry in `application.yml` or via env:
  - `tp.registry.url` e.g. `http://127.0.0.1:8082/api`
  - `tp.registry.autoSyncOnStart` true/false
- Signature verification (if enabled):
  - `tp.keys.publicKeyPath` path to X.509 PEM public key used to verify `catalog.json.sig`.
