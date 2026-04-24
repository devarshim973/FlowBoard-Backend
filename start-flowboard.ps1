$ErrorActionPreference = "Stop"

$backendRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$frontendRoot = "C:\Users\devar\Desktop\FlowBoard-Frontend\flowboard-frontend"
$mavenWrapper = Join-Path $backendRoot "Eureka-server\mvnw.cmd"
$envFile = Join-Path $backendRoot "auth-service\.env"
$envAssignments = @()

$services = @(
  @{ Name = "Eureka"; Path = "Eureka-server\pom.xml"; Delay = 12 },
  @{ Name = "Auth"; Path = "auth-service\pom.xml"; Delay = 8 },
  @{ Name = "Notification"; Path = "notification_service\pom.xml"; Delay = 5 },
  @{ Name = "Workspace"; Path = "workspace-service\pom.xml"; Delay = 5 },
  @{ Name = "Board"; Path = "board-service\pom.xml"; Delay = 5 },
  @{ Name = "List"; Path = "list-service\pom.xml"; Delay = 5 },
  @{ Name = "Comment"; Path = "comment-service\pom.xml"; Delay = 5 },
  @{ Name = "Card"; Path = "card-service\pom.xml"; Delay = 5 },
  @{ Name = "Gateway"; Path = "api-gateway\pom.xml"; Delay = 0 }
)

if (-not (Test-Path $mavenWrapper)) {
  throw "Maven wrapper not found at $mavenWrapper"
}

if (-not (Get-Command npm -ErrorAction SilentlyContinue)) {
  throw "npm is required to start the frontend."
}

if (Test-Path $envFile) {
  Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*#' -or $_ -notmatch '=') {
      return
    }

    $name, $value = $_ -split '=', 2
    if (-not [string]::IsNullOrWhiteSpace($name)) {
      $trimmedName = $name.Trim()
      $trimmedValue = $value.Trim()
      [Environment]::SetEnvironmentVariable($trimmedName, $trimmedValue, "Process")
      $escapedValue = $trimmedValue.Replace("'", "''")
      $envAssignments += "`$env:$trimmedName = '$escapedValue'"
    }
  }
}

$mysqlService = Get-Service -Name "MySQL80" -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -ne "Running") {
  Start-Service -Name "MySQL80"
}

foreach ($service in $services) {
  $pomPath = Join-Path $backendRoot $service.Path
  $title = "FlowBoard $($service.Name)"
  $envCommand = if ($envAssignments.Count) { ($envAssignments -join "; ") + "; " } else { "" }
  $command = "$envCommand& '$mavenWrapper' -f '$pomPath' spring-boot:run"

  Start-Process powershell -ArgumentList @(
    "-NoExit",
    "-Command",
    "`$Host.UI.RawUI.WindowTitle = '$title'; Set-Location '$backendRoot'; $command"
  ) | Out-Null

  if ($service.Delay -gt 0) {
    Start-Sleep -Seconds $service.Delay
  }
}

Start-Process powershell -ArgumentList @(
  "-NoExit",
  "-Command",
  "`$Host.UI.RawUI.WindowTitle = 'FlowBoard Frontend'; Set-Location '$frontendRoot'; npm run dev"
) | Out-Null

Write-Host "FlowBoard services are starting in separate windows."
Write-Host "Frontend: http://localhost:5173"
Write-Host "Gateway:  http://localhost:8080"
