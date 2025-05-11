# test-api.ps1
# Устанавливаем консоль в UTF-8 для кириллицы
chcp 65001 > $null

$baseUrl    = 'http://localhost:8080'
$adminLogin = 'adminuser'; $adminPass = 'adminpass'
$userLogin  = 'testuser';  $userPass  = 'testpass'
$operation  = 'op1'

function Invoke-Check {
    param(
        [Parameter(Mandatory=$true)]
        [hashtable]$Request
    )
    try {
        $resp = Invoke-RestMethod @Request
        return @{ success = $true; response = $resp }
    }
    catch {
        $status = $_.Exception.Response.StatusCode.Value__
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $body   = $reader.ReadToEnd()
        return @{ success = $false; status = $status; body = $body }
    }
}

Write-Host "`n=== 0) CLEANUP OTP FILES ==="
Get-ChildItem .\otp-files\otp_*.txt -ErrorAction SilentlyContinue | Remove-Item -Force

Write-Host "`n=== 1) REGISTRATION ==="
$baseReq = @{
    Uri         = "$baseUrl/register"
    Method      = 'POST'
    ContentType = 'application/x-www-form-urlencoded'
}
# Админ
$req = $baseReq.Clone()
$req.Body = "login=$adminLogin&password=$adminPass&role=ADMIN"
$res = Invoke-Check -Request $req
if ($res.success) {
    Write-Host "Admin register: OK"
} else {
    Write-Host "Admin register failed: $($res.status) $($res.body)"
}

# Обычный юзер
$req = $baseReq.Clone()
$req.Body = "login=$userLogin&password=$userPass&role=USER"
$res = Invoke-Check -Request $req
if ($res.success) {
    Write-Host "User  register: OK"
} else {
    Write-Host "User  register failed: $($res.status) $($res.body)"
}

Write-Host "`n=== 2) LOGIN ==="
# Админ
$loginReq = @{
    Uri         = "$baseUrl/login"
    Method      = 'POST'
    ContentType = 'application/x-www-form-urlencoded'
    Body        = "login=$adminLogin&password=$adminPass"
}
$res = Invoke-Check -Request $loginReq
if (-not $res.success) { throw "Admin login failed: $($res.status) $($res.body)" }
$adminToken = $res.response.token
Write-Host "Admin token: $adminToken"

# Юзер
$loginReq.Body = "login=$userLogin&password=$userPass"
$res = Invoke-Check -Request $loginReq
if (-not $res.success) { throw "User login failed: $($res.status) $($res.body)" }
$userToken = $res.response.token
Write-Host "User  token: $userToken"

$hdrUser  = @{ Authorization = "Bearer $userToken" }
$hdrAdmin = @{ Authorization = "Bearer $adminToken" }

$cfgReq = @{
    Uri     = "$baseUrl/admin/otp-config"
    Method  = 'POST'
    Headers = $hdrAdmin
    Body    = 'length=6&ttl=300'
}
$res = Invoke-Check -Request $cfgReq
Write-Host "Reset TTL to 300s: $($res.response)"

Write-Host "`n=== 3) GENERATE OTP FOR ALL CHANNELS ==="
$channels = @('file','email','sms','telegram')
$codes    = @{}

foreach ($ch in $channels) {
    Write-Host "`n-- Channel: $ch"
    $genReq = @{
        Uri         = "$baseUrl/generate-otp"
        Method      = 'POST'
        ContentType = 'application/x-www-form-urlencoded'
        Headers     = $hdrUser
        Body        = "operationId=$operation&channel=$ch"
    }
    $res = Invoke-Check -Request $genReq
    if (-not $res.success) {
        Write-Host "Generate failed: $($res.status) $($res.body)"
        continue
    }
    Write-Host "Generate OK: $($res.response)"

    if ($ch -eq 'file') {
        Start-Sleep -Milliseconds 200
        $file = Get-ChildItem -Path .\otp-files -Filter "otp_${userLogin}_*.txt" |
                Sort-Object LastWriteTime | Select-Object -Last 1
        $text = Get-Content $file.FullName
        $code = ($text -split ' ')[-1]
        Write-Host "OTP from file: $code"
        $codes[$ch] = $code
    }
    else {
        $codes[$ch] = '<sent>'
    }
}

Write-Host "`n=== 4) VALIDATE ACTIVE OTP (file) ==="
$active = $codes['file']
$valReq = @{
    Uri         = "$baseUrl/validate-otp"
    Method      = 'POST'
    ContentType = 'application/x-www-form-urlencoded'
    Headers     = $hdrUser
    Body        = "operationId=$operation&otpCode=$active"
}
$res = Invoke-Check -Request $valReq
if ($res.success) {
    Write-Host "First validate: OK"
} else {
    Write-Host "First validate failed: $($res.status) $($res.body)"
}

Write-Host "`n=== 5) VALIDATE REUSE (USED) ==="
$res = Invoke-Check -Request $valReq
if ($res.success) {
    Write-Host "Second validate: Unexpected OK"
} else {
    Write-Host "Second validate (USED): $($res.status) $($res.body)"
}

Write-Host "`n=== 6) EXPIRED CODE TEST ==="
Write-Host "Updating TTL to 3 sec…"
$cfgReq.Body = 'length=6&ttl=3'
$res = Invoke-Check -Request $cfgReq
if ($res.success) { Write-Host "Config update: OK" }
else { Write-Host "Config update failed: $($res.status) $($res.body)" }

Write-Host "Generate new code for expired test…"
$genReq.Body = "operationId=${operation}2&channel=file"
$res = Invoke-Check -Request $genReq
Start-Sleep -Seconds 5
$file = Get-ChildItem -Path .\otp-files -Filter "otp_${userLogin}_*.txt" |
        Sort-Object LastWriteTime | Select-Object -Last 1
$expired = (Get-Content $file.FullName) -split ' ' | Select-Object -Last 1
Write-Host "Expired OTP: $expired"

$valReq.Body = "operationId=${operation}2&otpCode=$expired"
$res = Invoke-Check -Request $valReq
if ($res.success) {
    Write-Host "Validate expired: Unexpected OK"
} else {
    Write-Host "Validate expired: $($res.status) $($res.body)"
}

Write-Host "`n=== 7) ADMIN API TESTS ==="
Write-Host "List users:"
$listReq = @{
    Uri         = "$baseUrl/admin/users"
    Method      = 'POST'
    ContentType = 'application/x-www-form-urlencoded'
    Headers     = $hdrAdmin
}
$res = Invoke-Check -Request $listReq
if ($res.success) {
    $res.response | Format-Table
} else {
    Write-Host "List users failed: $($res.status) $($res.body)"
}

Write-Host "`nDelete testuser:"
$id = ($res.response | Where-Object login -eq $userLogin).id
$delReq = @{
    Uri         = "$baseUrl/admin/users/delete"
    Method      = 'POST'
    ContentType = 'application/x-www-form-urlencoded'
    Headers     = $hdrAdmin
    Body        = "userId=$id"
}
$res = Invoke-Check -Request $delReq
if ($res.success) {
    Write-Host "Delete response: $($res.response)"
} else {
    Write-Host "Delete failed: $($res.status) $($res.body)"
}

Write-Host "`n=== ALL TESTS COMPLETED ==="
