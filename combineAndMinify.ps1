# Define the output file path
$outputFile = "combined_code_minified.txt"

# Get all Java files recursively
Get-ChildItem -Recurse -Filter *.java | ForEach-Object {
    # Announce which file is being processed (useful for debugging)
    # Write-Host "Processing $($_.FullName)..."

    # Read the entire file content as a single string (-Raw is crucial for multi-line regex)
    $content = Get-Content $_.FullName -Raw

    # 1. Remove comments (both multi-line /* ... */ and single-line //)
    #    (?s) allows '.' to match newline characters, for multi-line comments.
    #    \* is an escaped asterisk.
    #    .*? is a non-greedy match for any character.
    $content = $content -replace '(?s)/\*.*?\*/|//.*'

    # 2. Reduce multiple consecutive blank lines down to a maximum of one blank line.
    #    (\r?\n) matches both Windows (CRLF) and Unix (LF) line endings.
    #    {3,} matches three or more consecutive newlines.
    #    We replace them with two newlines (`r`n`r`n) to create a single blank line.
    $content = $content -replace "(\r?\n){3,}", "`r`n`r`n"
    
    # 3. Trim any leading/trailing whitespace from the entire file content
    $content = $content.Trim()

    # 4. Only output the file content if it's not empty after cleaning
    if (-not [string]::IsNullOrWhiteSpace($content)) {
        "--- START FILE: $($_.FullName) ---"
        $content
        "--- END FILE: $($_.FullName) ---`n`n"
    }
} | Set-Content -Encoding UTF8 -Path $outputFile

Write-Host "Processing complete. Minified code saved to $outputFile"