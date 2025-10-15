Param(
  [string]$AppData = "$env:USERPROFILE/.local/share/YourApp"
)

$jar = Join-Path $PSScriptRoot '..\app-platform\core-service\target\core-service.jar'
if (-not (Test-Path $jar)) {
  Write-Error "Jar not found: $jar. Build with: mvn -pl ui-angular-build,core-service -Pprod clean package"
  exit 1
}

$env:APPDATA_DIR = $AppData
$env:PF4J_PLUGINS_DIR = Join-Path $AppData 'plugins'

java -Xmx512m -DAPPDATA_DIR=$env:APPDATA_DIR -Dpf4j.pluginsDir=$env:PF4J_PLUGINS_DIR -jar $jar

