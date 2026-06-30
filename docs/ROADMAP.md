# Roadmap

Development roadmap for **MSBF-to-TachiBK**.

MSBF-to-TachiBK converts **Manga Storm `.msbf` favorites exports** into **Komikku / Tachiyomi-style `.tachibk` backups**.

---

## Current Milestone

```text
v0.8.x — Optional duplicate handling
```

Commit 10 adds safer duplicate-control options while keeping the default behavior unchanged.

Default behavior:

```text
Keep duplicates
Write duplicate report when duplicates are found
Continue conversion normally
```

Optional behavior:

```text
--report-duplicates-only
--remove-duplicates
```

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

# Current Work

## Commit 10 — Optional Duplicate Handling

Status:

```text
Current
```

Completed / being finalized:

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

Commit target:

```text
v0.8.0
```

---

# Remaining Before V1

## Commit 11 — Full Compatibility Test

Status:

```text
Planned
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

---

# Current Recommended Next Steps

After Commit 10 is committed:

```text
1. Tag v0.8.0
2. Start Commit 11
3. Run full compatibility test
4. Restore into clean Komikku 1.13.6 profile
5. Confirm categories, metadata, duplicate behavior, and MangaDex opening behavior
6. Prepare V1 release cleanup
```