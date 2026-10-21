param([string] $Catalogue = "$PSScriptRoot\data\catalogue.txt")
$ErrorActionPreference = 'Stop'
if (-not (Test-Path "$PSScriptRoot\out\classes\CinemaApp.class")) { throw 'Run .\build.ps1 first' }
java --module-path "$PSScriptRoot\lib\javafx" --add-modules javafx.controls --enable-native-access=javafx.graphics -cp "$PSScriptRoot\out\classes" CinemaApp $Catalogue
exit $LASTEXITCODE
