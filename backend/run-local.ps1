param(
    [switch]$BootstrapAdmin
)

$ErrorActionPreference = 'Stop'

$locationPushed = $false
$mavenExitCode = 0
try {
    if ($BootstrapAdmin) {
        $listener = Get-NetTCPConnection -State Listen -LocalPort 8080 -ErrorAction SilentlyContinue |
            Select-Object -First 1
        if ($listener) {
            throw "Port 8080 is already in use by process $($listener.OwningProcess). Stop the existing backend before bootstrapping an admin account."
        }

        $adminName = Read-Host 'Admin full name'
        $adminEmail = Read-Host 'Admin email address'
        $secureAdminPassword = Read-Host 'Admin password (at least 12 characters; input is hidden)' -AsSecureString
        if ($secureAdminPassword.Length -lt 12 -or $secureAdminPassword.Length -gt 72) {
            $secureAdminPassword.Dispose()
            throw 'The admin password must be between 12 and 72 characters.'
        }

        $adminPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureAdminPassword)
        try {
            $env:ADMIN_BOOTSTRAP_ENABLED = 'true'
            $env:ADMIN_BOOTSTRAP_NAME = $adminName
            $env:ADMIN_BOOTSTRAP_EMAIL = $adminEmail
            $env:ADMIN_BOOTSTRAP_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPointer)
        }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPointer)
            $secureAdminPassword.Dispose()
        }
    }

    if ([string]::IsNullOrWhiteSpace($env:DB_USERNAME)) {
        $env:DB_USERNAME = 'root'
    }

    if ([string]::IsNullOrWhiteSpace($env:DB_PASSWORD)) {
        try {
            $securePassword = Read-Host "Enter the MySQL password for '$env:DB_USERNAME'" -AsSecureString
        }
        catch {
            throw "No interactive terminal is available for the MySQL password prompt. Run .\run-local.ps1 directly in a VS Code PowerShell terminal."
        }

        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
        }
        finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
            $securePassword.Dispose()
        }
    }

    if ([string]::IsNullOrWhiteSpace($env:JWT_SECRET)) {
        $secretBytes = New-Object byte[] 32
        $randomGenerator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try {
            $randomGenerator.GetBytes($secretBytes)
        }
        finally {
            $randomGenerator.Dispose()
        }
        $env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
    }

    if ([string]::IsNullOrWhiteSpace($env:SAMPLE_DATA_ENABLED)) {
        $env:SAMPLE_DATA_ENABLED = 'true'
    }

    Push-Location $PSScriptRoot
    $locationPushed = $true
    & mvn spring-boot:run
    $mavenExitCode = $LASTEXITCODE
}
finally {
    if ($BootstrapAdmin) {
        Remove-Item Env:ADMIN_BOOTSTRAP_ENABLED -ErrorAction SilentlyContinue
        Remove-Item Env:ADMIN_BOOTSTRAP_NAME -ErrorAction SilentlyContinue
        Remove-Item Env:ADMIN_BOOTSTRAP_EMAIL -ErrorAction SilentlyContinue
        Remove-Item Env:ADMIN_BOOTSTRAP_PASSWORD -ErrorAction SilentlyContinue
    }
    if ($locationPushed) {
        Pop-Location
    }
}
exit $mavenExitCode
