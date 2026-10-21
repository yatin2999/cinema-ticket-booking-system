$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
[Net.ServicePointManager]::SecurityProtocol = [Net.ServicePointManager]::SecurityProtocol -bor [Net.SecurityProtocolType]::Tls12
$root = $PSScriptRoot

New-Item -ItemType Directory -Force "$root\lib\javafx" | Out-Null
foreach ($module in 'base', 'graphics', 'controls') {
    $jar = "$root\lib\javafx\javafx-$module-25.0.4-win.jar"
    if (-not (Test-Path $jar)) {
        Write-Host "Downloading javafx-$module 25.0.4"
        Invoke-WebRequest "https://repo1.maven.org/maven2/org/openjfx/javafx-$module/25.0.4/javafx-$module-25.0.4-win.jar" -OutFile "$jar.part" -UseBasicParsing
        Move-Item "$jar.part" $jar
    }
}

if (Test-Path "$root\out\classes") { Remove-Item "$root\out\classes" -Recurse -Force }
$sources = Get-ChildItem "$root\src" -Filter *.java | ForEach-Object { $_.FullName }
javac --release 25 -encoding UTF-8 --module-path "$root\lib\javafx" --add-modules javafx.controls -d "$root\out\classes" $sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
Copy-Item "$root\resources\*" "$root\out\classes"
Write-Host "Build succeeded: $root\out\classes"
