# Script de compilacion de ParFMI.
#
# Uso:
#   .\tools\build.ps1                 # compila el APK debug
#   .\tools\build.ps1 -Task assembleRelease
#   .\tools\build.ps1 -Task testDebugUnitTest
#
# Requisitos: JDK 17 y Android SDK (platform 35 + build-tools 35.0.0).
# Si JAVA_HOME / ANDROID_HOME no estan definidos, el script intenta usar las
# rutas habituales de este equipo.

param(
    [string]$Task = 'assembleDebug',
    [switch]$NoDaemon
)

$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot

function Resolve-JdkHome {
    if ($env:JAVA_HOME -and (Test-Path (Join-Path $env:JAVA_HOME 'bin\javac.exe'))) {
        return $env:JAVA_HOME
    }
    $candidates = @(
        'C:\Users\Usuario\android-dev\jdk17\jdk-17.0.20.1+1',
        'C:\Users\Usuario\pardl-android-build\jdk\jdk-17.0.20.1+1'
    )
    foreach ($c in $candidates) {
        if (Test-Path (Join-Path $c 'bin\javac.exe')) { return $c }
    }
    $glob = Get-ChildItem 'C:\Users\Usuario\android-dev\jdk17' -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -like 'jdk-17*' } | Select-Object -First 1
    if ($glob) { return $glob.FullName }
    throw 'No se encontro un JDK 17. Define JAVA_HOME antes de compilar.'
}

function Resolve-SdkHome {
    if ($env:ANDROID_HOME -and (Test-Path $env:ANDROID_HOME)) { return $env:ANDROID_HOME }
    if ($env:ANDROID_SDK_ROOT -and (Test-Path $env:ANDROID_SDK_ROOT)) { return $env:ANDROID_SDK_ROOT }
    $candidates = @(
        'C:\Users\Usuario\android-dev\android-sdk',
        'C:\Users\Usuario\pardl-android-build\sdk'
    )
    foreach ($c in $candidates) {
        if (Test-Path (Join-Path $c 'platforms')) { return $c }
    }
    throw 'No se encontro el Android SDK. Define ANDROID_HOME antes de compilar.'
}

$env:JAVA_HOME = Resolve-JdkHome
$env:ANDROID_HOME = Resolve-SdkHome
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:PATH = "$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:PATH"

Write-Host "JAVA_HOME    = $env:JAVA_HOME"
Write-Host "ANDROID_HOME = $env:ANDROID_HOME"
Write-Host "TASK         = $Task"

$gradlew = Join-Path $root 'gradlew.bat'
if (-not (Test-Path $gradlew)) {
    throw "No se encontro gradlew.bat en $root"
}

$args = @($Task)
if ($NoDaemon) { $args += '--no-daemon' }
& $gradlew @args
Write-Host "GRADLE_EXIT=$LASTEXITCODE"
exit $LASTEXITCODE
