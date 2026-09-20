Add-Type -AssemblyName System.Drawing

$sourcePath = Join-Path $PSScriptRoot '..\src\client\java\net\mixelpixel\mod\client\screen\EmptyRadialMenuScreen.java'
$outputPath = Join-Path $PSScriptRoot '..\src\client\resources\assets\mixelpixelmod\textures\gui\radial'
New-Item -ItemType Directory -Force -Path $outputPath | Out-Null
$source = Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8

function Read-IconLines([string]$constantName) {
    $pattern = '(?s)' + [regex]::Escape("private static final List<String> $constantName = List.of(") + '(.*?)\);'
    $match = [regex]::Match($source, $pattern)
    if (!$match.Success) { throw "Icon-Konstante nicht gefunden: $constantName" }
    return [regex]::Matches($match.Groups[1].Value, '"([^"]*)"') | ForEach-Object { $_.Groups[1].Value }
}

function Write-BrailleIcon([string]$constantName, [string]$fileName) {
    $lines = @(Read-IconLines $constantName)
    $active = [System.Collections.Generic.List[object]]::new()
    for ($row = 0; $row -lt $lines.Count; $row++) {
        # ASCII spaces were manual alignment compensations for the text preview.
        # Only Braille blank cells belong to the actual pixel grid.
        $chars = $lines[$row].Replace(' ', '').ToCharArray()
        for ($column = 0; $column -lt $chars.Count; $column++) {
            $value = [int]$chars[$column]
            if ($value -lt 0x2800 -or $value -gt 0x28FF) { continue }
            $bits = $value - 0x2800
            $dots = @(@(0,0,1), @(0,1,2), @(0,2,4), @(1,0,8), @(1,1,16), @(1,2,32), @(0,3,64), @(1,3,128))
            foreach ($dot in $dots) {
                if (($bits -band $dot[2]) -ne 0) {
                    $active.Add([pscustomobject]@{ X = $column * 2 + $dot[0]; Y = $row * 4 + $dot[1] })
                }
            }
        }
    }
    if ($active.Count -eq 0) { throw "Keine Braille-Pixel in $constantName" }

    $minX = ($active | Measure-Object X -Minimum).Minimum
    $maxX = ($active | Measure-Object X -Maximum).Maximum
    $minY = ($active | Measure-Object Y -Minimum).Minimum
    $maxY = ($active | Measure-Object Y -Maximum).Maximum
    $pixelScale = [Math]::Min(116.0 / ($maxX - $minX + 1), 116.0 / ($maxY - $minY + 1))
    $offsetX = [Math]::Floor((128 - ($maxX - $minX + 1) * $pixelScale) / 2)
    $offsetY = [Math]::Floor((128 - ($maxY - $minY + 1) * $pixelScale) / 2)

    $bitmap = [Drawing.Bitmap]::new(128, 128, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([Drawing.Color]::Transparent)
    $brush = [Drawing.SolidBrush]::new([Drawing.Color]::White)
    foreach ($point in $active) {
        $x = $offsetX + ($point.X - $minX) * $pixelScale
        $y = $offsetY + ($point.Y - $minY) * $pixelScale
        $left = [int][Math]::Round($x)
        $top = [int][Math]::Round($y)
        $right = [int][Math]::Round($x + $pixelScale)
        $bottom = [int][Math]::Round($y + $pixelScale)
        $graphics.FillRectangle($brush, $left, $top, ($right - $left), ($bottom - $top))
    }
    $target = Join-Path $outputPath $fileName
    $bitmap.Save($target, [Drawing.Imaging.ImageFormat]::Png)
    $brush.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
}

Write-BrailleIcon 'KICK_ICON' 'kick.png'

# These two symbols use explicit geometry: text-layout compensation must never
# distort their outlines. All cutouts are transparent, including the ban stripe.
foreach ($name in @('ban', 'message')) {
    $bitmap = [Drawing.Bitmap]::new(128, 128, [Drawing.Imaging.PixelFormat]::Format32bppArgb)
    $graphics = [Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([Drawing.Color]::Transparent)
    $white = [Drawing.Brushes]::White
    $pen = [Drawing.Pen]::new([Drawing.Color]::White, 7)
    if ($name -eq 'ban') {
        $graphics.DrawEllipse($pen, 10, 10, 108, 108)
        $graphics.FillPolygon($white, [Drawing.Point[]]@(
            [Drawing.Point]::new(29, 61), [Drawing.Point]::new(64, 30),
            [Drawing.Point]::new(99, 61)))
        $graphics.FillRectangle($white, 37, 60, 54, 38)
        $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
        $clearPen = [Drawing.Pen]::new([Drawing.Color]::Transparent, 16)
        $graphics.DrawLine($clearPen, 29, 29, 99, 99)
        $clearPen.Dispose()
        $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceOver
        $graphics.DrawLine($pen, 26, 26, 102, 102)
    } else {
        $graphics.DrawRectangle($pen, 48, 53, 69, 45)
        $graphics.DrawLines($pen, [Drawing.Point[]]@(
            [Drawing.Point]::new(90, 98), [Drawing.Point]::new(107, 111),
            [Drawing.Point]::new(107, 98)))
        $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
        $graphics.FillRectangle([Drawing.Brushes]::Transparent, 8, 24, 77, 55)
        $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceOver
        $graphics.FillRectangle($white, 8, 24, 72, 50)
        $graphics.FillPolygon($white, [Drawing.Point[]]@(
            [Drawing.Point]::new(17, 72), [Drawing.Point]::new(17, 90),
            [Drawing.Point]::new(37, 72)))
        $graphics.CompositingMode = [Drawing.Drawing2D.CompositingMode]::SourceCopy
        foreach ($dotX in @(23, 41, 59)) {
            $graphics.FillRectangle([Drawing.Brushes]::Transparent, $dotX, 46, 6, 6)
        }
    }
    $bitmap.Save((Join-Path $outputPath "$name.png"), [Drawing.Imaging.ImageFormat]::Png)
    $pen.Dispose(); $graphics.Dispose(); $bitmap.Dispose()
}
