$ports = 8761, 8080, 8081, 8082, 8083, 8084, 8085, 8086, 8087

foreach ($port in $ports) {
  $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
  if (-not $listeners) {
    continue
  }

  $listeners |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object {
      try {
        Stop-Process -Id $_ -Force -ErrorAction Stop
        Write-Host "Stopped process $_ on port $port"
      } catch {
        Write-Warning "Could not stop process $_ on port ${port}: $($_.Exception.Message)"
      }
    }
}
