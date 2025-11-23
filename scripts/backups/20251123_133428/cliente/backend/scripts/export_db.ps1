param(
    [string]$DbHost = "127.0.0.1",
    [int]$Port = 3306,
    [string]$User = "root",
    [string]$Database = "smarthfashion",
    [string]$OutFile = "./db_export.sql",
    [switch]$SchemaOnly,
    [switch]$DataOnly,
    [string]$DockerContainer = "",
    [string]$Password = ""
)

function Get-MySqlDumpPath {
    $cmd = "mysqldump"
    $which = (Get-Command $cmd -ErrorAction SilentlyContinue)
    if ($which) { return $which.Source }
    return $null
}

if (-not $Password) {
    $secure = Read-Host -AsSecureString "MySQL password for $User@$DbHost"
    $bstr = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        $plain = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr)
        $Password = $plain
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) | Out-Null
    }
}

$mysqldump = Get-MySqlDumpPath
if (-not $mysqldump -and -not $DockerContainer) {
    Write-Error "`nNo `mysqldump` found in PATH. If MySQL is running in Docker, pass -DockerContainer <name>.\nInstall MySQL client tools or run this from a machine with mysqldump available."
    exit 1
}

$options = "--routines --triggers --events --single-transaction --set-gtid-purged=OFF --default-character-set=utf8mb4"
if ($SchemaOnly) { $options += " --no-data" }
if ($DataOnly) { $options += " --no-create-info" }

if ($DockerContainer) {
    # Use docker exec and pipe the output to a file on the host
    $dockerCmd = "docker exec -i $DockerContainer mysqldump -h $DbHost -P $Port -u $User -p$Password $options $Database"
    Write-Output "Running (docker): $dockerCmd > $OutFile"
    iex "$dockerCmd | Out-File -Encoding utf8 -FilePath $OutFile"
} else {
    $cmd = "$mysqldump -h $DbHost -P $Port -u $User -p$Password $options $Database"
    Write-Output "Running: $cmd > $OutFile"
    iex "$cmd | Out-File -Encoding utf8 -FilePath $OutFile"
}

Write-Output "Export finished: $OutFile"
