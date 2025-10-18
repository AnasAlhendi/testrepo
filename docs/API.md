Horizon API Reference

Target Platform (TP) — `:19080/api`
- `GET /health` → `{ status: "ok" }`
- Plugins
  - `GET /plugins` → `PluginSpec[]`
  - `POST /plugins/install` body=`PluginSpec` → `PluginSpec`
  - `POST /plugins/start?id=<id>` → `PluginSpec`
  - `POST /plugins/stop?id=<id>` → `PluginSpec`
  - `POST /plugins/uninstall?id=<id>` → `{ id, status: "uninstalled" }`
  - `POST /plugins/update` body=`PluginSpec` → `PluginSpec`
- Programs
  - `GET /programs` → `ProgramSpec[]`
  - `POST /programs/install` body=`ProgramSpec` → `ProgramSpec`
  - `POST /programs/start?id=<id>` → `ProgramSpec`
  - `POST /programs/stop?id=<id>` → `ProgramSpec`
  - `POST /programs/restart?id=<id>` → `ProgramSpec`
- Logs
  - `GET /logs?target=server|program:<id>|plugin:<id>&lines=200` → `string[]`
  - `GET /logs/stream?target=...` (SSE) → live log lines
- Registry Sync
  - `POST /registry/sync` → `{ programs: ProgramSpec[], plugins: PluginSpec[] }`

ProgramSpec
- `id: string`
- `version: string`
- `source: { maven?: string, url?: string, path?: string, sha256?: string }`
- `jvmArgs?: string[]`
- `args?: string[]`
- `health?: { http: string, timeoutSec?: number }`
- `status: "stopped" | "running" | "installed"`

PluginSpec
- `id: string`
- `version: string`
- `source: { maven?: string, url?: string, path?: string, sha256?: string }`
- `verifySignature?: boolean`
- `status: "installed" | "started" | "stopped"`

Registry — `:8082/api` (aliases under `/v1` also)
- `GET /health` → `{ status: "ok" }`
- Catalog
  - `GET /catalog` → `{ plugins: any[], programs: any[] }`
  - `GET /catalog.json` → string (canonical JSON)
  - `GET /catalog.sig` → string (Base64 RSA SHA-256 of catalog.json)
  - `GET /catalog.sha256` → string (hex digest)
  - `POST /catalog/plugins` body=`object` → the added plugin entry
  - `POST /catalog/programs` body=`object` → the added program entry
- Uploads
  - `POST /plugins/upload` (multipart: `id`, `version`, `file`) → entry with `url` to artifact
  - `POST /programs/upload` (multipart: `id`, `version`, `file`) → entry with `url` to artifact
  - `POST /upload` (multipart: `type=plugin|program`, `id`, `version`, `file`, `meta?`) → convenience endpoint
- Files
  - `GET /files/**` → serves uploaded artifacts from storage root, inline

App Platform (Core) — `:18080/api`
- `GET /ui-config` → `{ tp: "/api/tp", registry: "/api/registry" }`
- Proxy to TP: `ANY /tp/**` → forwarded to TP base (`tp.baseUrl`)
- Proxy to Registry: `ANY /registry/**` → forwarded to Registry base (`registry.baseUrl`)
- Plugins (local PF4J dir listing)
  - `GET /plugins` → `[ { id, path } ]`

Notes
- Proxies preserve method, path, and query string; JSON body forwarded when present.
- For production, the Angular UI is served by Core at `/` with SPA fallback.

