Get-ChildItem -Recurse -Filter *.java | ForEach-Object {
    "--- START FILE: $($_.FullName) ---"
    Get-Content $_.FullName
    "--- END FILE: $($_.FullName) ---`n`n"
} | Set-Content -Encoding UTF8 combined_code.txt