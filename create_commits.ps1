
$files = Get-ChildItem -Recurse -File -Exclude .git | Select-Object -ExpandProperty FullName
$root = Get-Location
$filesRelative = $files | ForEach-Object { $_.Substring($root.Path.Length + 1) }

$counter = 0
foreach ($file in $filesRelative) {
    git add "$file"
    git commit -m "Agregando archivo del proyecto: $file"
    $counter++
}

Write-Host "Se han creado $counter commits."
git push origin main
