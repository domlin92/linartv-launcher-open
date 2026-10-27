$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$javaBin = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin' } else { '' }
$compiler = if ($javaBin) { Join-Path $javaBin 'javac.exe' } else { 'javac' }
$runtime = if ($javaBin) { Join-Path $javaBin 'java.exe' } else { 'java' }
$outputPath = Join-Path $projectRoot 'tmp\local-tests'
New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\LauncherTiming.java') (Join-Path $projectRoot 'tests\LauncherTimingCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of local checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.LauncherTimingCheck
if ($LASTEXITCODE -ne 0) { throw 'Local checks failed.' }
& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\BlurPixels.java') (Join-Path $projectRoot 'tests\BlurPixelsCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of blur checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.BlurPixelsCheck
if ($LASTEXITCODE -ne 0) { throw 'Blur checks failed.' }
& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\MatchCompetition.java') (Join-Path $projectRoot 'tests\MatchCompetitionCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of competition checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.MatchCompetitionCheck
if ($LASTEXITCODE -ne 0) { throw 'Competition checks failed.' }
& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\CalendarScope.java') (Join-Path $projectRoot 'tests\CalendarScopeCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of calendar scope checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.CalendarScopeCheck
if ($LASTEXITCODE -ne 0) { throw 'Calendar scope checks failed.' }

& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\CrestNameIndex.java') (Join-Path $projectRoot 'tests\CrestNameIndexCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of crest checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.CrestNameIndexCheck
if ($LASTEXITCODE -ne 0) { throw 'Crest checks failed.' }
& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\CrestNameMatcher.java') (Join-Path $projectRoot 'tests\CrestNameMatcherCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of crest matcher checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.CrestNameMatcherCheck
if ($LASTEXITCODE -ne 0) { throw 'Crest matcher checks failed.' }
& $compiler -encoding UTF-8 -d $outputPath (Join-Path $projectRoot 'app\src\main\java\com\linart\tvpreview\LanguageChoice.java') (Join-Path $projectRoot 'tests\LanguageChoiceCheck.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation of language checks failed.' }
& $runtime -cp $outputPath com.linart.tvpreview.LanguageChoiceCheck
if ($LASTEXITCODE -ne 0) { throw 'Language checks failed.' }
python (Join-Path $projectRoot 'scripts\check-translations.py')
if ($LASTEXITCODE -ne 0) { throw 'Translation coverage checks failed.' }
