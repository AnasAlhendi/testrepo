Helper Scripts

All scripts are PowerShell and live under `scripts/`.

generate-keys.ps1
- Usage: `scripts/generate-keys.ps1 -OutDir <dir>`
- Generates `registry-private.pem` (PKCS8) and `tp-public.pem` (X.509)

run-registry.ps1
- Params:
  - `-StorageDir` (default `${USERPROFILE}/.local/share/RegistryServer`)
  - `-PrivateKeyPath` (optional path to PKCS8 PEM)
- Starts `registry-server` on `:8082` with provided storage/signing.

setup-tp-home.ps1
- Params:
  - `-TpHome` (default `${USERPROFILE}/.local/share/TargetPlatform`)
  - `-RegistryUrl` (e.g., `http://127.0.0.1:8082/api`)
  - `-PublicKeyPath` (path to `tp-public.pem`)
  - `-AutoSyncOnStart` (switch)
- Prepares directory structure and updates TP application.yml accordingly.

run-tp.ps1
- Params:
  - `-TpHome` (default `${USERPROFILE}/.local/share/TargetPlatform`)
- Starts TP on `:19080` with `-Dtp.home` JVM arg.

run-core-service.ps1
- Params:
  - `-AppData` (default `${USERPROFILE}/.local/share/YourApp`)
- Runs packaged Core JAR; ensure UI dist has been copied via `ui-angular-build`.

