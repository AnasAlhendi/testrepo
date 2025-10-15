Param(
  [string]$TpHome = "$env:USERPROFILE/.local/share/TargetPlatform",
  [string]$RegistryUrl = "",
  [string]$PublicKeyPath = "",
  [switch]$AutoSyncOnStart
)

New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'config') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'plugins') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'programs') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'downloads') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'logs') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'keys') | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $TpHome 'state') | Out-Null

$cfgPath = Join-Path $TpHome 'config/tp-config.yml'
if (-not (Test-Path $cfgPath)) {
@"
tpHome: "$TpHome"
autoStart: true
plugins: []
programs: []
"@ | Set-Content -Path $cfgPath -NoNewline
}

$appYml = Join-Path "$PSScriptRoot/.." 'target-platform/tp-server/src/main/resources/application.yml'
if (Test-Path $appYml) {
  $yml = Get-Content $appYml -Raw
  if ($RegistryUrl) { $yml = $yml -replace 'url: \"\"', ('url: "' + $RegistryUrl + '"') }
  if ($PublicKeyPath) { $yml = $yml -replace 'publicKeyPath: \"\"', ('publicKeyPath: "' + $PublicKeyPath + '"') }
  if ($AutoSyncOnStart.IsPresent) { $yml = $yml -replace 'autoSyncOnStart: false', 'autoSyncOnStart: true' }
  Set-Content -Path $appYml -Value $yml -NoNewline
}

Write-Output "TP_HOME prepared at $TpHome"

