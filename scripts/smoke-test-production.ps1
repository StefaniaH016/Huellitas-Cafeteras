[CmdletBinding()]
param(
    [string]$FrontendUrl = "https://huellitas-cafeteras.vercel.app",
    [string]$BackendUrl = "https://huellitas-cafeteras-prod.azurewebsites.net",
    [ValidateRange(1, 60)]
    [int]$TimeoutSeconds = 15,
    [ValidateRange(1, 60)]
    [int]$MaxResponseSeconds = 5
)

$ErrorActionPreference = "Stop"
$checksFailed = 0

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Uri,
        [scriptblock]$ValidateResponse
    )

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

    try {
        $response = Invoke-WebRequest -Uri $Uri -Method Get -TimeoutSec $TimeoutSeconds -UseBasicParsing
        $stopwatch.Stop()
        $elapsedSeconds = $stopwatch.Elapsed.TotalSeconds

        if ($response.StatusCode -lt 200 -or $response.StatusCode -ge 300) {
            throw "HTTP $($response.StatusCode)"
        }

        if ($ValidateResponse) {
            & $ValidateResponse $response
        }

        if ($elapsedSeconds -ge $MaxResponseSeconds) {
            throw ("Tiempo de respuesta {0:N2}s; debe ser menor a {1}s" -f $elapsedSeconds, $MaxResponseSeconds)
        }

        Write-Host ("PASS {0}: HTTP {1}, {2:N2}s" -f $Name, $response.StatusCode, $elapsedSeconds) -ForegroundColor Green
        return $true
    }
    catch {
        $stopwatch.Stop()
        $elapsedSeconds = $stopwatch.Elapsed.TotalSeconds
        Write-Host ("FAIL {0}: {1} ({2:N2}s)" -f $Name, $_.Exception.Message, $elapsedSeconds) -ForegroundColor Red
        return $false
    }
}

Write-Host "Smoke test de Huellitas Cafeteras" -ForegroundColor Cyan
Write-Host "Frontend: $FrontendUrl"
Write-Host "Backend:  $BackendUrl"
Write-Host "Limite de respuesta: menos de $MaxResponseSeconds segundos`n"

if (-not (Test-Endpoint -Name "Frontend carga" -Uri $FrontendUrl)) {
    $checksFailed++
}

$healthValidator = {
    param($response)
    $content = $response.Content
    if ($content -is [byte[]]) {
        $content = [System.Text.Encoding]::UTF8.GetString($content)
    }
    $health = $content | ConvertFrom-Json
    if ($health.status -ne "UP") {
        throw "Actuator reporto estado '$($health.status)'"
    }
}

if (-not (Test-Endpoint -Name "Backend health" -Uri "$($BackendUrl.TrimEnd('/'))/actuator/health" -ValidateResponse $healthValidator)) {
    $checksFailed++
}

if (-not (Test-Endpoint -Name "Consulta de departamentos" -Uri "$($BackendUrl.TrimEnd('/'))/v1/ubicaciones/departamentos")) {
    $checksFailed++
}

if (-not (Test-Endpoint -Name "Consulta de casos" -Uri "$($BackendUrl.TrimEnd('/'))/v1/casos")) {
    $checksFailed++
}

if ($checksFailed -gt 0) {
    Write-Host "`nSmoke test fallido: $checksFailed comprobacion(es) no pasaron." -ForegroundColor Red
    exit 1
}

Write-Host "`nSmoke test exitoso: todas las comprobaciones pasaron." -ForegroundColor Green
