# Calender28 Automated Benchmark Runner (PowerShell)
param(
    [string]$Scenario = "All",
    [int]$Iterations = 3
)

$adbCmd = "adb"
$sdkAdb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
if (-not (Get-Command adb -ErrorAction SilentlyContinue) -and (Test-Path $sdkAdb)) {
    $adbCmd = $sdkAdb
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host " Calender28 Performance Benchmark Runner" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if ($Scenario -eq "All" -or $Scenario -eq "JVM") {
    Write-Host "`n[1/2] Running JVM Microbenchmarks..." -ForegroundColor Yellow
    ./gradlew testDebugUnitTest --tests "com.l1khith.calender28.benchmark.*"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[-] JVM Microbenchmarks Failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "[+] JVM Microbenchmarks Passed!" -ForegroundColor Green
}

if ($Scenario -eq "All" -or $Scenario -eq "Startup") {
    Write-Host "`n[2/2] Checking for connected Android devices for Startup Timing..." -ForegroundColor Yellow
    $devices = & $adbCmd devices | Select-String -Pattern "\bdevice$"
    if ($devices.Count -gt 0) {
        Write-Host "Connected device found. Running $Iterations startup benchmark runs..." -ForegroundColor Cyan
        for ($i = 1; $i -le $Iterations; $i++) {
            & $adbCmd shell am force-stop com.l1khith.calender28
            Start-Sleep -Milliseconds 500
            Write-Host "Iteration $i of ${Iterations}:" -ForegroundColor Gray
            & $adbCmd shell am start-activity -W -n com.l1khith.calender28/.MainActivity -c android.intent.category.LAUNCHER -a android.intent.action.MAIN | Select-String -Pattern "ThisTime|TotalTime|WaitTime"
        }
    } else {
        Write-Host "[*] No physical/emulator Android device connected. Skipping on-device benchmarks." -ForegroundColor Gray
    }
}

Write-Host "`n[SUCCESS] Benchmark run finished successfully!" -ForegroundColor Green
