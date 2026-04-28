$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$logDir = Join-Path $root ".run-logs"

$services = @(
  @{ Name = "flowboard-server"; Port = 8761; DelaySeconds = 8 },
  @{ Name = "auth-service"; Port = 8081; DelaySeconds = 6 },
  @{ Name = "notification_service"; Port = 8082; DelaySeconds = 4 },
  @{ Name = "comment-service"; Port = 8083; DelaySeconds = 4 },
  @{ Name = "workspace-service"; Port = 8084; DelaySeconds = 4 },
  @{ Name = "board-service"; Port = 8085; DelaySeconds = 4 },
  @{ Name = "list-service"; Port = 8086; DelaySeconds = 4 },
  @{ Name = "card-service"; Port = 8087; DelaySeconds = 4 },
  @{ Name = "flowboard-api-gateway"; Port = 8080; DelaySeconds = 0 }
)

$sharedEnv = @{
  "db_password" = ""
  "jwt-secret-key" = ""
  "google_client_id" = ""
  "google_client_secret" = ""
  "brevo-api-key" = ""
  "brevo-sender-mail" = ""
  "admin-verification-mail" = ""
  "cloudinary-cloud-name" = ""
  "cloudinary-api-key" = ""
  "cloudinary-api-secret" = ""
  "rate_limit_enabled" = "false"
  "MANAGEMENT_HEALTH_REDIS_ENABLED" = "false"
}

$launchConfigDir = Join-Path $root ".metadata\.plugins\org.eclipse.debug.core\.launches"

function Get-LaunchEnvironment {
  param([string]$ServiceName)

  $environment = @{}
  if (-not (Test-Path $launchConfigDir)) {
    return $environment
  }

  $launchFile = Get-ChildItem -Path $launchConfigDir -Filter "$ServiceName*.launch" -ErrorAction SilentlyContinue |
    Select-Object -First 1

  if (-not $launchFile) {
    return $environment
  }

  [xml]$launchXml = Get-Content -LiteralPath $launchFile.FullName
  $launchXml.launchConfiguration.mapAttribute |
    Where-Object { $_.key -eq "org.eclipse.debug.core.environmentVariables" } |
    ForEach-Object {
      foreach ($entry in $_.mapEntry) {
        $environment[$entry.key] = $entry.value
      }
    }

  return $environment
}

function Get-MavenCommand {
  $mvnCmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
  if ($mvnCmd -and $mvnCmd.Source) {
    return $mvnCmd.Source
  }

  $mvn = Get-Command mvn -ErrorAction SilentlyContinue
  if ($mvn -and $mvn.Source) {
    return $mvn.Source
  }

  $directCandidates = @(
    "C:\Program Files\JetBrains\IntelliJ IDEA 2025.2\plugins\maven\lib\maven3\bin\mvn.cmd",
    "C:\Program Files\Apache\maven\bin\mvn.cmd"
  )

  foreach ($candidate in $directCandidates) {
    if (Test-Path -LiteralPath $candidate) {
      return $candidate
    }
  }

  $stsCandidates = Get-ChildItem "C:\Program Files\STS" -Recurse -Filter mvn.cmd -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -like "*apache-maven*" } |
    Select-Object -First 1
  if ($stsCandidates) {
    return $stsCandidates.FullName
  }

  throw "Could not find Maven. Add mvn to PATH or install Maven/STS with embedded Maven."
}

function Stop-ProcessOnPort {
  param([int]$Port)

  $listeners = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
  if (-not $listeners) {
    return
  }

  $listeners |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object {
      try {
        Stop-Process -Id $_ -Force -ErrorAction Stop
        Write-Host "Stopped process $_ on port $Port"
      } catch {
        Write-Warning "Could not stop process $_ on port ${Port}: $($_.Exception.Message)"
      }
    }
}

function Wait-ForPort {
  param(
    [int]$Port,
    [int]$TimeoutSeconds = 90
  )

  $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
  while ((Get-Date) -lt $deadline) {
    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    if ($listener) {
      return $true
    }
    Start-Sleep -Seconds 2
  }

  return $false
}

New-Item -ItemType Directory -Force -Path $logDir | Out-Null

$mavenCommand = Get-MavenCommand
Write-Host "Using Maven: $mavenCommand"

foreach ($service in $services) {
  Stop-ProcessOnPort -Port $service.Port
}

foreach ($service in $services) {
  $serviceDir = Join-Path $root $service.Name
  $stdoutLog = Join-Path $logDir "$($service.Name).out.log"
  $stderrLog = Join-Path $logDir "$($service.Name).err.log"

  if (-not (Test-Path $serviceDir)) {
    Write-Warning "Skipping missing service folder: $serviceDir"
    continue
  }

  Write-Host "Starting $($service.Name) on port $($service.Port)..."

  $serviceEnv = @{}
  foreach ($entry in $sharedEnv.GetEnumerator()) {
    $serviceEnv[$entry.Key] = $entry.Value
  }
  foreach ($entry in (Get-LaunchEnvironment -ServiceName $service.Name).GetEnumerator()) {
    $serviceEnv[$entry.Key] = $entry.Value
  }

  $envAssignments = ($serviceEnv.GetEnumerator() | ForEach-Object {
    'set "{0}={1}"' -f $_.Key, $_.Value
  }) -join " && "

  $commandLine = "$envAssignments && `"$mavenCommand`" spring-boot:run"

  Start-Process `
    -FilePath "cmd.exe" `
    -ArgumentList "/c", $commandLine `
    -WorkingDirectory $serviceDir `
    -RedirectStandardOutput $stdoutLog `
    -RedirectStandardError $stderrLog `
    -WindowStyle Hidden

  if (-not (Wait-ForPort -Port $service.Port)) {
    throw "Service $($service.Name) did not start on port $($service.Port). Check $stdoutLog and $stderrLog"
  }

  if ($service.DelaySeconds -gt 0) {
    Start-Sleep -Seconds $service.DelaySeconds
  }
}

Write-Host ""
Write-Host "All FlowBoard services were started."
Write-Host "Logs folder: $logDir"
