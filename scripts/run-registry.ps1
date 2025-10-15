Param(
  [string]$StorageDir = "$env:USERPROFILE/.local/share/RegistryServer",
  [string]$PrivateKeyPath = ""
)

Push-Location "$PSScriptRoot/..\registry-server"
if ($PrivateKeyPath) {
  mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments="--registry.storage.dir=$StorageDir --registry.signing.privateKeyPath=$PrivateKeyPath"
} else {
  mvn -q -DskipTests spring-boot:run -Dspring-boot.run.arguments="--registry.storage.dir=$StorageDir"
}
Pop-Location

