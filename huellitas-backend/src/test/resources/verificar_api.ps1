# Script de Verificación End-to-End en Vivo para Huellitas Cafeteras
$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Verificando Huellitas Cafeteras Backend" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Health Actuator
$health = Invoke-RestMethod -Uri "http://localhost:8080/api/actuator/health" -Method Get -UseBasicParsing
Write-Host "[1/10] Actuator Health: $($health.status)" -ForegroundColor Green

# 2. Departamentos
$deps = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/ubicaciones/departamentos" -Method Get -UseBasicParsing
Write-Host "[2/10] Ubicaciones Departamentos: $($deps.Count) encontrados ($($deps[0].nombre), $($deps[1].nombre), $($deps[2].nombre))" -ForegroundColor Green

# 3. Login Admin
$adminLoginBody = @{ correo = "admin@huellitas.co"; contrasena = "Admin12345!" } | ConvertTo-Json
$adminLoginBytes = [System.Text.Encoding]::UTF8.GetBytes($adminLoginBody)
$adminAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -Body $adminLoginBytes -ContentType "application/json; charset=utf-8" -UseBasicParsing
$adminToken = $adminAuth.token
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
Write-Host "[3/10] Login SuperAdmin: OK (Rol: $($adminAuth.usuario.rol), Token JWT generado)" -ForegroundColor Green

# 4. Auth Me
$me = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" -Method Get -Headers $adminHeaders -UseBasicParsing
Write-Host "[4/10] GET /v1/auth/me: OK ($($me.nombre) - $($me.correo))" -ForegroundColor Green

# 5. Registro de nuevo ciudadano voluntario
$emailVoluntario = "voluntario_$(Get-Random)@huellitas.co"
$regBody = @{
    nombre = "Juan Perez"
    correo = $emailVoluntario
    contrasena = "Voluntario12345!"
    telefono = "3105551234"
    rol = "CIUDADANO_VOLUNTARIO"
    barrioId = 1
} | ConvertTo-Json
$regBytes = [System.Text.Encoding]::UTF8.GetBytes($regBody)
$regRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/registro" -Method Post -Body $regBytes -ContentType "application/json; charset=utf-8" -UseBasicParsing
Write-Host "[5/10] Registro Ciudadano: OK (Usuario ID: $($regRes.id), Rol: $($regRes.rol))" -ForegroundColor Green

# Login con nuevo ciudadano
$volLoginBody = @{ correo = $emailVoluntario; contrasena = "Voluntario12345!" } | ConvertTo-Json
$volLoginBytes = [System.Text.Encoding]::UTF8.GetBytes($volLoginBody)
$volAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -Body $volLoginBytes -ContentType "application/json; charset=utf-8" -UseBasicParsing
$volToken = $volAuth.token
$volHeaders = @{ Authorization = "Bearer $volToken" }

# 6. Crear un Caso de Animal Herido
$casoBody = @{
    tipoCaso = "HERIDO"
    urgencia = "ALTA"
    descripcion = "Perro herido rescatado en el centro de Armenia"
    direccionReferencia = "Plaza de Bolivar"
    barrioId = 1
    animal = @{
        nombre = "Tobby"
        especie = "PERRO"
        raza = "Labrador Mix"
        color = "Dorado"
        sexo = "MACHO"
        edadAproximada = "3 anios"
        senasParticulares = "Collar rojo"
    }
    fotografias = @("https://res.cloudinary.com/huellitas_local/image/upload/tobby.jpg")
} | ConvertTo-Json -Depth 5
$casoBytes = [System.Text.Encoding]::UTF8.GetBytes($casoBody)
$casoRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos" -Method Post -Body $casoBytes -Headers $volHeaders -ContentType "application/json; charset=utf-8" -UseBasicParsing
$casoId = $casoRes.id
Write-Host "[6/10] Creación de Caso: OK (Caso ID: $casoId, Estado: $($casoRes.estado), Urgencia: $($casoRes.urgencia))" -ForegroundColor Green

# 7. Feed de Casos y Detalle
$feed = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos?especie=PERRO" -Method Get -UseBasicParsing
$detalle = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId" -Method Get -UseBasicParsing
Write-Host "[7/10] Feed y Detalle: OK (Total en Feed: $($feed.totalElements), Animal Detalle: $($detalle.animal.nombre), Municipio: $($detalle.ubicacion.municipio))" -ForegroundColor Green

# 8. Aporte Comunitario
$aporteBody = @{
    descripcion = "Se le suministró agua y alimento mientras llega la patrulla de rescate"
    ubicacionTexto = "Esquina sur del parque"
    barrioId = 1
    fotografias = @("https://res.cloudinary.com/huellitas_local/image/upload/aporte_tobby.jpg")
} | ConvertTo-Json
$aporteBytes = [System.Text.Encoding]::UTF8.GetBytes($aporteBody)
$aporteRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId/aportes" -Method Post -Body $aporteBytes -Headers $volHeaders -ContentType "application/json; charset=utf-8" -UseBasicParsing
$aportesList = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId/aportes" -Method Get -UseBasicParsing
Write-Host "[8/10] Aporte Comunitario: OK (Aporte ID: $($aporteRes.id), Total aportes en caso: $($aportesList.Count))" -ForegroundColor Green

# 9. Bitácora Médica, Evolución y Alta
$bitBody = @{ observacionInicial = "Ingreso para revision veterinaria y estabilizacion" } | ConvertTo-Json
$bitBytes = [System.Text.Encoding]::UTF8.GetBytes($bitBody)
$bitRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId/bitacora" -Method Post -Body $bitBytes -Headers $adminHeaders -ContentType "application/json; charset=utf-8" -UseBasicParsing

$evoBody = @{
    diagnostico = "Deshidratacion moderada y contusion leve"
    procedimientos = "Sueroterapia y limpieza"
    tratamiento = "Reposo e hidratacion"
    medicamentos = "Complejo B y antiinflamatorio"
    observaciones = "Paciente responde muy favorablemente"
} | ConvertTo-Json
$evoBytes = [System.Text.Encoding]::UTF8.GetBytes($evoBody)
$evoRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId/bitacora/evoluciones" -Method Post -Body $evoBytes -Headers $adminHeaders -ContentType "application/json; charset=utf-8"

$altaBody = @{ condicionAlta = "Completamente recuperado y alerta"; indicaciones = "Apto para adopcion o reintegro" } | ConvertTo-Json
$altaBytes = [System.Text.Encoding]::UTF8.GetBytes($altaBody)
$altaRes = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId/bitacora/alta" -Method Patch -Body $altaBytes -Headers $adminHeaders -ContentType "application/json; charset=utf-8" -UseBasicParsing
Write-Host "[9/10] Bitácora Clínica y Alta: OK (Estado Bitácora: $($altaRes.estadoBitacora), Evolución ID: $($evoRes.id))" -ForegroundColor Green

# 10. Descargar Resumen Clínico PDF y Validar Formato
$tempPdf = "$env:TEMP\resumen_clinico_$casoId.pdf"
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/casos/$casoId/bitacora/pdf" -Method Get -OutFile $tempPdf -UseBasicParsing
$pdfBytes = [System.IO.File]::ReadAllBytes($tempPdf)
$magicHeader = [System.Text.Encoding]::ASCII.GetString($pdfBytes[0..3])
if ($magicHeader -eq "%PDF") {
    Write-Host "[10/10] Generación y Descarga de PDF Clínico: OK (Encabezado: $magicHeader, Tamaño: $($pdfBytes.Length) bytes)" -ForegroundColor Green
} else {
    throw "El archivo descargado no es un PDF válido"
}
Remove-Item -Path $tempPdf -Force -ErrorAction SilentlyContinue

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "TODAS LAS PRUEBAS EN VIVO PASARON CON ÉXITO (10/10)" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan
