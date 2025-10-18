Configuration Reference

Core Service (`app-platform/core-service`)
- `server.port` (default `18080`)
- `tp.baseUrl` (default `http://127.0.0.1:19080/api`)
- `registry.baseUrl` (default `http://127.0.0.1:8082/api`)
- Env/System properties used for plugin listing endpoint:
  - `APPDATA_DIR` (or `-DAPPDATA_DIR`) default `${user.home}/.local/share/YourApp`
  - `PF4J_PLUGINS_DIR` (or `-Dpf4j.pluginsDir`) default `${APPDATA_DIR}/plugins`

Target Platform (`target-platform/tp-server`)
- `server.port` (default `19080`)
- `tp.home` (default `${user.home}/.local/share/TargetPlatform`)
- `tp.configPath` (default `${tp.home}/config/tp-config.yml`)
- `tp.autoStart` (default `true`)
- `tp.registry.url` (default empty)
- `tp.registry.autoSyncOnStart` (default `false`)
- `tp.keys.publicKeyPath` (default empty) — X.509 PEM used to verify Registry `catalog.json` signature
- `tp.security.enabled` (default `false`)
- `tp.security.headerName` (default `X-API-Key`)
- `tp.security.readKeys` (default empty)
- `tp.security.adminKeys` (default empty)

Registry Server (`registry-server`)
- `server.port` (default `8082`)
- `registry.storage.dir` (default `${user.home}/.local/share/RegistryServer`)
- `registry.signing.privateKeyPath` (default empty) — PKCS8 PEM for signing catalog
- `registry.security.enabled` (default `false`)
- `registry.security.headerName` (default `X-API-Key`)
- `registry.security.adminKeys` (default empty)

Examples
- TP example config: `target-platform/tp-server/src/main/resources/tp-config.example.yml`
- Keys: use `scripts/generate-keys.ps1` to create compatible private/public pair

