param(
    [string]$BaseUrl = "http://localhost:8080/api",
    [string]$Username = "user",
    [string]$Password = "123456"
)

$ErrorActionPreference = "Stop"
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

function Invoke-MallApi {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null
    )

    $request = @{
        Method      = $Method
        Uri         = "$BaseUrl$Path"
        WebSession  = $session
        ContentType = "application/json"
    }

    if ($null -ne $Body) {
        $request.Body = $Body | ConvertTo-Json -Depth 8
    }

    $response = Invoke-WebRequest @request
    $result = $response.Content | ConvertFrom-Json
    if ($result.code -ne 200) {
        throw "$Method $Path failed: $($result.message)"
    }
    return $result.data
}

$user = Invoke-MallApi -Method "POST" -Path "/auth/login" -Body @{
    username = $Username
    password = $Password
}
$me = Invoke-MallApi -Method "GET" -Path "/auth/me"
$categories = Invoke-MallApi -Method "GET" -Path "/categories/tree"
$products = Invoke-MallApi -Method "GET" -Path "/products?page=1&size=4&sort=newest"
$cart = Invoke-MallApi -Method "GET" -Path "/cart"

Write-Host "Smoke passed"
Write-Host "User: $($user.username) / $($me.role)"
Write-Host "Categories: $($categories.Count)"
Write-Host "Products: $($products.records.Count)"
Write-Host "Cart items: $($cart.Count)"
