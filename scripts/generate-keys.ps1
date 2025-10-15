Param(
  [string]$OutDir = "$env:USERPROFILE/.local/share/RegistryKeys"
)

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$rsa = [System.Security.Cryptography.RSA]::Create(2048)

# Export private key (PKCS8) PEM
$privDer = $rsa.ExportPkcs8PrivateKey()
$privB64 = [System.Convert]::ToBase64String($privDer)
$privPem = "-----BEGIN PRIVATE KEY-----`n" + ($privB64 -split '(.{1,64})' | ? { $_ -ne '' } -join "`n") + "`n-----END PRIVATE KEY-----`n"
Set-Content -Path (Join-Path $OutDir 'registry-private.pem') -Value $privPem -NoNewline

# Export public key (X.509 SubjectPublicKeyInfo) PEM
$pubDer = $rsa.ExportSubjectPublicKeyInfo()
$pubB64 = [System.Convert]::ToBase64String($pubDer)
$pubPem = "-----BEGIN PUBLIC KEY-----`n" + ($pubB64 -split '(.{1,64})' | ? { $_ -ne '' } -join "`n") + "`n-----END PUBLIC KEY-----`n"
Set-Content -Path (Join-Path $OutDir 'tp-public.pem') -Value $pubPem -NoNewline

Write-Output "Generated keys in $OutDir"

