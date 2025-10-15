Param(
  [string]$TpHome = "$env:USERPROFILE/.local/share/TargetPlatform"
)

Push-Location "$PSScriptRoot/..\target-platform\tp-server"
mvn -q -DskipTests spring-boot:run -Dspring-boot.run.jvmArguments="-Dtp.home=$TpHome"
Pop-Location

