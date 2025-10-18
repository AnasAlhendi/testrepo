Param(
  [string]$AppData = "$env:USERPROFILE/.local/share/YourApp"
)

$targetDir = Join-Path $PSScriptRoot '..\app-platform\core-service\target'
$jar = Join-Path $targetDir 'core-service.jar'
if (-not (Test-Path $jar)) {
  # Fallback to versioned jar produced by Spring Boot repackage
  $candidates = Get-ChildItem -Path $targetDir -Filter 'core-service-*.jar' -File -ErrorAction SilentlyContinue |
    Where-Object { $_.Name -notlike '*.original' } |
    Sort-Object LastWriteTime -Descending
  if ($candidates -and $candidates.Length -gt 0) {
    $jar = $candidates[0].FullName
    Write-Host "Using jar: $jar"
  } else {
    Write-Error "Jar not found in $targetDir. Build with: mvn -pl app-platform/core-service -DskipTests package"
    exit 1
  }
}

$env:APPDATA_DIR = $AppData
$env:PF4J_PLUGINS_DIR = Join-Path $AppData 'plugins'

# Use call operator with explicit, quoted JVM args to avoid PS parsing issues
$javaArgs = @(
  "-Xmx512m",
  "-DAPPDATA_DIR=$($env:APPDATA_DIR)",
  "-Dpf4j.pluginsDir=$($env:PF4J_PLUGINS_DIR)",
  "-jar",
  "$jar"
)
& java @javaArgs

