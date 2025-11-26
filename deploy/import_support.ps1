<#
PowerShell helper to import support schema and seed data into the local DB container used by the compose stack.
Usage (from repository root):
    cd C:\SmarthFashion
    .\deploy\import_support.ps1 -RootPass 'rodo2006'

It will import `support_schema.sql` then `seed_support.sql` into the `smarthfashion` database and then recreate the `admin` service.
#>
param(
    [string]$RootPass = "rodo2006"
)

Push-Location (Split-Path -Path $MyInvocation.MyCommand.Path -Parent) | Out-Null
Pop-Location

Write-Host "Using root password from parameter (do not hardcode in public places)."

# Get db container id from compose
$composeCmd = "docker compose -f ..\docker-compose.yml"
$psCmd = "$composeCmd ps -q db"
$cid = Invoke-Expression $psCmd
if (-not $cid) {
    Write-Error "Could not determine DB container id. Ensure you're in the repo and compose stack is up."
    exit 1
}

Write-Host "DB container id: $cid"

# Import schema
if (-Not (Test-Path "..\support_schema.sql")) {
    Write-Error "support_schema.sql not found at repo root."
    exit 1
}

Write-Host "Importing support_schema.sql ..."
# Use docker exec with input redirection. In PowerShell the < operator works when running docker exec directly.
$importCmd = "docker exec -i $cid mysql -u root -p$RootPass smarthfashion < ..\\support_schema.sql"
Invoke-Expression $importCmd
if ($LASTEXITCODE -ne 0) { Write-Error "Failed importing schema."; exit 1 }

Write-Host "Importing seed_support.sql ..."
$seedCmd = "docker exec -i $cid mysql -u root -p$RootPass smarthfashion < ..\\seed_support.sql"
Invoke-Expression $seedCmd
if ($LASTEXITCODE -ne 0) { Write-Error "Failed importing seeds."; exit 1 }

Write-Host "Recreating admin service to pick up any env changes..."
$recreate = "docker compose -f ..\docker-compose.yml up -d --no-deps --force-recreate admin"
Invoke-Expression $recreate

Write-Host "Tailing admin logs (press Ctrl+C to stop)"
Invoke-Expression "docker compose -f ..\docker-compose.yml logs --tail 200 -f admin"
