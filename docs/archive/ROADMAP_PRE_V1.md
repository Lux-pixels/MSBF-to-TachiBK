# Roadmap

Development roadmap for **MSBF-to-TachiBK**.

MSBF-to-TachiBK converts **Manga Storm `.msbf` favorites exports** into **Komikku / Tachiyomi-style `.tachibk` backups**.

---

## Current Milestone

```text
v0.9.x — Full compatibility testing
```

Commit 11 is focused on testing the full converter flow before the first stable V1 release.

The goal is to confirm that the converter works with a full real Manga Storm export and restores correctly into a clean Komikku 1.13.6 profile.

---

## Project Focus

Current project goals:

```text
MangaDex support
Komikku restore reliability
Metadata enrichment
Category mapping
Duplicate reporting
Optional duplicate handling
Pre-conversion validation
Clear user documentation
Safe default behavior
```

---

# Completed Work

## Commit 1 — Project Foundation

Status:

```text
Done
```

Completed:

```text
Gradle project setup
Kotlin JVM setup
Gradle wrapper
GitHub Actions build
Basic repository structure
Initial command-line app structure
```

Purpose:

```text
Create a buildable Kotlin project that can run locally and in GitHub Actions.
```

---

## Commit 2 — MSBF Parser

Status:

```text
Done
```

Completed:

```text
Read Manga Storm .msbf files
Extract manga title
Extract Manga Storm source key
Extract MangaDex URL
Extract Manga Storm status flag
Extract timestamp when available
Confirm real Manga Storm export format is text-like and parseable
```

Purpose:

```text
Turn Manga Storm backup data into structured Kotlin objects.
```

---

## Commit 3 — Initial .tachibk Generator

Status:

```text
Done
```

Completed:

```text
Create minimal Komikku backup models
Generate gzip-compressed protobuf backup
Write .tachibk files
Map MangaDex source ID
Normalize MangaDex URLs
Confirm Komikku can read the generated backup
```

Purpose:

```text
Generate the first usable Komikku/Tachiyomi-style backup file.
```

---

## Commit 4 — MangaDex Metadata and Reports

Status:

```text
Done
```

Completed:

```text
Fetch MangaDex metadata
Add title metadata
Add cover URLs
Add authors
Add artists
Add descriptions
Add genres
Add manga status
Detect duplicate MangaDex entries
Write duplicate report
Write missing metadata report
Write failed connection report
Improve terminal summary output
```

Generated reports:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

Purpose:

```text
Make restored MangaDex entries load more reliably and provide useful debugging reports.
```

---

## Commit 5 — Manga Storm Status Categories

Status:

```text
Done
```

Completed:

```text
Map R to Reading
Map Y to Following
Map A to On Hold
Restore categories into Komikku
Confirm Komikku category order behavior
Leave unknown status values uncategorized
```

Category mapping:

| Manga Storm Flag | Komikku Category |
|---|---|
| `R` | Reading |
| `Y` | Following |
| `A` | On Hold |

Purpose:

```text
Preserve Manga Storm library organization when restoring into Komikku.
```

---

## Commit 6 — Metadata Default

Status:

```text
Done
```

Completed:

```text
Fetch metadata by default
Keep --no-metadata for quick tests
Improve conversion defaults for restored MangaDex entries
Start versioned testdata folder convention
```

Purpose:

```text
Make normal conversion behavior produce better Komikku restore results without requiring extra flags.
```

Notes:

```text
Metadata remains optional for fast testing with --no-metadata.
```

---

## Commit 7 — Documentation

Status:

```text
Done
```

Completed:

```text
Rewrite README with project progress
Add restore guide
Add troubleshooting guide
Add roadmap file
Document known limitations
Document recommended restore settings
Document BlueStacks restore workaround
Document generated reports
```

Documentation files:

```text
README.md
docs/RESTORE.md
docs/TROUBLESHOOTING.md
docs/ROADMAP.md
```

Purpose:

```text
Make the project understandable for future users and contributors.
```

---

## Commit 8 — Better CLI Options

Status:

```text
Done
```

Completed:

```text
Add --help
Add --version
Add --output / -o
Keep --metadata
Keep --no-metadata
Support optional convert command
Support old command style
Create output folders automatically when possible
Update documentation for safer restore behavior
Keep App Settings restore disabled for now
```

Supported command styles:

```bash
./gradlew run --args="convert <input.msbf> --output <output.tachibk>"
```

```bash
./gradlew run --args="<input.msbf> <output.tachibk>"
```

Purpose:

```text
Make the converter easier to run and easier to understand from the terminal.
```

---

## Commit 9 — Pre-Conversion Validation

Status:

```text
Done
```

Completed:

```text
Validate input file exists
Validate input file is readable
Validate input file is not empty
Require .msbf input extension
Require .tachibk output extension
Create output folders automatically when possible
Validate parsed entries are not empty
Detect unsupported sources before conversion
Detect invalid MangaDex URLs before conversion
Print validation summary
Stop safely before writing backup when validation fails
```

Purpose:

```text
Catch problems before metadata fetching or backup writing begins.
```

Example successful validation:

```text
Validation Summary
==================
Input file: samples/testfavorites.msbf
Output file: testdata/v0.7/MSBF-to-TachiBK-v0.7-validation-test.tachibk
Entries parsed: 61
Supported source entries: 61
Unsupported source entries: 0
Invalid MangaDex URLs: 0
Warnings: 0
Errors: 0

Validation passed.
```

Example failed validation:

```text
Validation failed:
  - Input file does not exist: samples/missing.msbf
  - Output file must end with .tachibk: bad-output.txt

No backup was written.
```

---

## Commit 10 — Optional Duplicate Handling

Status:

```text
Done
```

Completed:

```text
Keep duplicates by default
Add --report-duplicates-only
Add --remove-duplicates
Write duplicate report before backup creation
Stop before backup creation in duplicate-report-only mode
Keep first MangaDex entry when duplicate removal is enabled
Remove later duplicate copies only when explicitly requested
Show duplicate removal summary
Keep default behavior safe
Move duplicate logic into DuplicateHandler
```

New duplicate options:

```text
--report-duplicates-only
--remove-duplicates
```

Default behavior:

```text
Duplicates are reported but kept.
```

`--report-duplicates-only` behavior:

```text
Write duplicate-manga-report.txt
Print duplicate summary
Stop before metadata fetching
Stop before backup creation
No backup is written
```

`--remove-duplicates` behavior:

```text
Keep the first copy of each MangaDex UUID
Remove later duplicate copies
Print duplicate removal summary
Continue conversion with deduped list
```

Purpose:

```text
Give users control over duplicates without silently deleting library entries.
```

---

# Current Work

## Commit 11 — Full Compatibility Test

Status:

```text
Current
```

Planned:

```text
Run full real Manga Storm export
Fetch metadata
Generate final pre-V1 backup
Restore into clean Komikku 1.13.6 profile
Confirm MangaDex entries open correctly
Confirm categories restore correctly
Confirm metadata appears where supported
Confirm duplicate report is accurate
Confirm duplicate removal works when requested
Confirm duplicate-report-only mode stops safely
Confirm missing metadata report is accurate
Confirm failed connection report behavior
Confirm delegated sources are manually disabled
Confirm App Settings remain unchecked during restore
```

Recommended restore options:

```text
Check:
- Manga
- Categories

Uncheck:
- App Settings
- Saved Searches
- Source Settings
- Extension Repos
```

Purpose:

```text
Verify the converter is stable enough for a V1 release.
```

Commit target:

```text
v0.9.0
```

---

# Remaining Before V1

## Commit 12 — V1 Release Cleanup

Status:

```text
Planned
```

Planned:

```text
Update version to 1.0.0
Finalize README
Finalize restore guide
Finalize troubleshooting guide
Finalize roadmap
Add known issues section
Add release notes
Create GitHub release
Tag v1.0.0
```

Target tag:

```bash
git tag -a v1.0.0 -m "Initial stable MSBF to TachiBK converter"
git push origin v1.0.0
```

Purpose:

```text
Create the first stable release of MSBF-to-TachiBK.
```

---

# Post-V1 Roadmap

## Commit 13 — Optional Restore Selections

Status:

```text
Planned after V1
```

Planned:

```text
Add optional App Settings support
Add optional Extension Repos support
Add optional Source Settings support
Make restore sections selectable
Keep optional restore sections disabled by default
Avoid changing Komikku settings unless the user explicitly selects them
Document restore risks clearly
Test App Settings behavior before recommending it
```

Possible CLI options:

```text
--include-app-settings
--include-extension-repos
--include-source-settings
```

Recommended default:

```text
Do not include App Settings
Do not include Extension Repos
Do not include Source Settings
```

Purpose:

```text
Allow advanced users to include additional Komikku backup sections while keeping default conversion safe.
```

Notes:

```text
App Settings, Extension Repos, and Source Settings should remain optional because they can affect restore behavior and app configuration.
```

Commit target:

```text
v1.1.0
```

---

## Commit 14 — Windows 10 Compatibility

Status:

```text
Planned after V1
```

Planned:

```text
Create Windows 10-compatible release package
Add Windows run script
Test converter on Windows 10
Document Windows usage
Confirm Java/runtime requirements
Confirm path handling works on Windows
Confirm output folder creation works on Windows
```

Possible outputs:

```text
Windows zip package
Windows .bat launcher
Future Windows desktop app or .exe
```

Purpose:

```text
Make the converter easy to run on Windows 10.
```

Commit target:

```text
v1.2.0
```

---

## Commit 15 — macOS Apple Silicon Compatibility

Status:

```text
Planned after V1
```

Planned:

```text
Create macOS Apple Silicon-compatible release package
Test on Apple Silicon Mac
Document macOS Apple Silicon usage
Confirm Java/runtime requirements
Confirm file permissions
Confirm command-line launcher works
```

Possible outputs:

```text
macOS Apple Silicon zip package
macOS shell launcher
Future native app package
```

Purpose:

```text
Make the converter easy to run on Apple Silicon Macs.
```

Commit target:

```text
v1.3.0
```

---

## Commit 16 — macOS Intel Compatibility

Status:

```text
Planned after V1
```

Planned:

```text
Create macOS Intel-compatible release package
Test on Intel Mac
Document macOS Intel usage
Confirm Java/runtime requirements
Confirm file permissions
Confirm command-line launcher works
```

Possible outputs:

```text
macOS Intel zip package
macOS shell launcher
Future native app package
```

Purpose:

```text
Make the converter easy to run on Intel-based Macs.
```

Commit target:

```text
v1.4.0
```

---

## Commit 17 — Linux x64 Compatibility

Status:

```text
Planned after V1
```

Planned:

```text
Create Linux x64-compatible release package
Test on Linux x64
Document Linux usage
Confirm Java/runtime requirements
Confirm shell launcher works
Confirm output folder creation works
```

Possible outputs:

```text
Linux x64 tar.gz package
Linux shell launcher
Future AppImage
```

Purpose:

```text
Make the converter easy to run on Linux x64.
```

Commit target:

```text
v1.5.0
```

---

# V1 Goals

The V1 release should support:

```text
Manga Storm .msbf input
MangaDex entries
Komikku-readable .tachibk output
Manga restore
Category restore
Metadata fetching
Duplicate reporting
Optional duplicate removal
Duplicate report-only mode
Missing metadata reporting
Failed connection reporting
Pre-conversion validation
Clear restore instructions
Clear troubleshooting instructions
Safe default behavior
```

V1 should not require:

```text
Desktop app
Automatic duplicate removal by default
Chapter import
Reading history import
Tracking import
More source support
App Settings restore
Source Settings restore
Extension Repos restore
Native platform packages
```

Those can come after V1.

---

# Known Limitations Before V1

Current limitations:

```text
MangaDex is the only supported source
Metadata depends on MangaDex API availability
Duplicates are kept by default
Chapters are not restored yet
Reading progress is not restored yet
Reading history is not restored yet
Tracking data is not restored yet
Saved searches are not restored yet
App Settings are not restored yet
Source Settings are not restored yet
Extension Repos are not restored yet
Desktop app is not built yet
Native Windows/macOS/Linux packages are not built yet
```

---

# Planned After V1

## Desktop App

Possible features:

```text
Drag-and-drop .msbf file
Choose output folder
Convert button
Progress display
Metadata progress bar
Error summary screen
Open output folder button
```

Possible packaging:

```text
Windows executable
macOS app
Linux AppImage
```

---

## More Source Support

Possible future source work:

```text
Identify additional Manga Storm source keys
Map those sources to Komikku/Tachiyomi source IDs
Normalize source-specific URLs
Add source-specific validation
Add source-specific metadata support when available
```

---

## More Backup Data

Possible future imports:

```text
Chapters
Reading progress
Reading history
Tracking data
Custom categories beyond current status mapping
Source settings
App settings after exact backup behavior is verified
Extension repos after restore behavior is verified
```

---

## Duplicate Management

Possible future options:

```text
Interactive duplicate review
Keep newest duplicate instead of first duplicate
Export duplicate cleanup report
Preview duplicate removal before writing backup
Duplicate comparison report
```

---

## Better Metadata Handling

Possible future improvements:

```text
Retry failed MangaDex requests
Resume metadata fetching
Cache fetched metadata
Reduce repeat API calls across runs
Write metadata cache to local file
Improve rate-limit handling
```

---

# Version Plan

| Version | Focus |
|---|---|
| `v0.1.0` | Initial project foundation |
| `v0.2.0` | Metadata and reports |
| `v0.3.0` | Manga Storm category mapping |
| `v0.4.0` | Metadata default behavior |
| `v0.5.0` | Documentation and restore guidance |
| `v0.6.0` | Better CLI options |
| `v0.7.0` | Pre-conversion validation |
| `v0.8.0` | Optional duplicate handling |
| `v0.9.0` | Full compatibility testing |
| `v1.0.0` | First stable release |
| `v1.1.0` | Optional restore selections |
| `v1.2.0` | Windows 10 compatibility |
| `v1.3.0` | macOS Apple Silicon compatibility |
| `v1.4.0` | macOS Intel compatibility |
| `v1.5.0` | Linux x64 compatibility |

---

# Current Recommended Next Steps

```text
1. Start Commit 11
2. Create a full compatibility testing checklist
3. Run full conversion with metadata
4. Restore into clean Komikku 1.13.6 profile
5. Confirm categories, metadata, duplicate behavior, and MangaDex opening behavior
6. Prepare V1 release cleanup
```