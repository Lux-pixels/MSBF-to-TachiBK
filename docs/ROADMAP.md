# Roadmap

Roadmap for **MSBF-to-TachiBK**.

**MSBF-to-TachiBK** converts Manga Storm `.msbf` favorites exports into Komikku / Tachiyomi-style `.tachibk` backups.

---

## Current Release

```text
v1.0.0 — Initial stable release
```

V1 provides a stable local web converter for moving Manga Storm MangaDex favorites into Komikku / Tachiyomi-style `.tachibk` backups.

---

## V1 Status

V1 is focused on safe, reliable Manga Storm favorites migration.

V1 includes:

```text
Manga Storm .msbf input
MangaDex source support
Komikku-readable .tachibk output
Local browser upload/download converter
Downloadable GitHub release ZIP
Manga restore
Category restore
Optional MangaDex metadata fetching
Duplicate reporting
Optional duplicate removal
Duplicate report download
Pre-conversion validation
Safe restore instructions
Clear troubleshooting documentation
```

---

## V1 Recommended Restore Options

When restoring the generated `.tachibk` file in Komikku, check:

```text
Manga
Categories
```

Leave these unchecked:

```text
App Settings
Extension Repos
Source Settings
Saved Searches
Feeds
```

V1 intentionally avoids restoring app, source, repo, saved search, and feed settings by default because those sections can change Komikku behavior or restore unwanted configuration data.

If MangaDex entries open as WebView / website 404 pages, manually disable delegated sources in Komikku.

---

## Development Progress

| Commit | Milestone | Status |
|---:|---|---|
| 1 | Gradle project foundation | ✅ Done |
| 2 | Manga Storm `.msbf` parser | ✅ Done |
| 3 | Initial `.tachibk` backup generator | ✅ Done |
| 4 | MangaDex metadata and reports | ✅ Done |
| 5 | Manga Storm status categories | ✅ Done |
| 6 | Metadata behavior | ✅ Done |
| 7 | Documentation and restore guide | ✅ Done |
| 8 | Better CLI options | ✅ Done |
| 9 | Pre-conversion validation | ✅ Done |
| 10 | Optional duplicate handling | ✅ Done |
| 11 | Full compatibility testing | ✅ Done |
| 12 | Shared converter service | ✅ Done |
| 13 | Local web converter UI | ✅ Done |
| 14 | Improved web status and reports | ✅ Done |
| 15A | Downloadable V1 release package | ✅ Done |
| 15B | V1 docs and release notes | ✅ Done |

---

## Completed V1 Work

### Project Foundation

Completed:

```text
Gradle project setup
Kotlin JVM setup
Gradle wrapper
GitHub Actions build
Application packaging support
Basic repository structure
Initial command-line app structure
```

Purpose:

```text
Create a buildable Kotlin project that can run locally, in GitHub Codespaces, and in GitHub Actions.
```

---

### Manga Storm Parser

Completed:

```text
Read Manga Storm .msbf files
Extract manga title
Extract Manga Storm source key
Extract MangaDex URL
Extract Manga Storm status flag
Extract timestamp when available
Confirm real Manga Storm export format is parseable
```

Purpose:

```text
Turn Manga Storm backup data into structured Kotlin objects.
```

---

### .tachibk Backup Generator

Completed:

```text
Create Komikku / Tachiyomi-style backup models
Generate gzip-compressed protobuf backup
Write .tachibk files
Map MangaDex source ID
Normalize MangaDex URLs
Confirm Komikku can read the generated backup
```

Purpose:

```text
Generate usable Komikku / Tachiyomi-style backup files.
```

---

### MangaDex Metadata and Reports

Completed:

```text
Optional MangaDex metadata fetching
Title metadata
Cover URLs
Authors
Artists
Descriptions
Genres
Manga status
Duplicate report
Missing metadata report
Failed connection report
Terminal summary output
```

Generated reports:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

Purpose:

```text
Improve restored MangaDex entries when metadata mode is enabled and provide useful debugging reports.
```

---

### Manga Storm Status Categories

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

### CLI Improvements

Completed:

```text
Add --help
Add --version
Add --output / -o
Add --metadata
Add --no-metadata
Add --report-duplicates-only
Add --remove-duplicates
Support optional convert command
Support old command style
Create output folders automatically when possible
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

### Pre-Conversion Validation

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

---

### Duplicate Handling

Completed:

```text
Keep duplicates by default
Add duplicate report
Add duplicate report download in the web UI
Add --report-duplicates-only
Add --remove-duplicates
Write duplicate report before backup creation
Stop before backup creation in duplicate-report-only mode
Keep first MangaDex entry when duplicate removal is enabled
Remove later duplicate copies only when explicitly requested
Show duplicate removal summary
Keep default behavior safe
```

Default behavior:

```text
Duplicates are reported but kept.
```

Purpose:

```text
Give users control over duplicates without silently deleting library entries.
```

---

### Local Web Converter

Completed:

```text
Start local server with serve command
Browser upload form
Local-only conversion
Metadata checkbox
Duplicate removal checkbox
Background conversion job
Status polling
On-page status summary
Manga totals
Source counts
Category counts
Duplicate counts
Backup download button
Duplicate report download button
Codespaces port forwarding support
```

Purpose:

```text
Make the converter usable without requiring users to type conversion commands.
```

---

### Downloadable GitHub Release Package

Completed:

```text
Gradle distribution ZIP
Windows launcher
macOS / Linux launcher
README-FIRST.txt
GitHub release workflow
Release ZIP upload
Smart App Control troubleshooting notes
```

Release package includes:

```text
README-FIRST.txt
run-web-converter.bat
run-web-converter.sh
bin/
lib/
```

Purpose:

```text
Let users download and run the converter from GitHub without cloning or building the project.
```

---

## V1 Scope

V1 supports:

```text
Manga Storm favorites export
MangaDex entries
Komikku / Tachiyomi-style backup output
Categories
Optional metadata enrichment
Duplicate reporting
Local web conversion
Command-line conversion
GitHub downloadable ZIP package
```

V1 does not include:

```text
Chapters
Reading progress
Reading history
Tracking data
Saved searches
Feeds
App settings
Source settings
Extension repos
Native installers
No-Java executable
Additional Manga Storm sources
```

---

# Future Roadmap After V1

Future work should stay conservative by default. Anything that changes Komikku app behavior, source behavior, extension repository behavior, saved searches, or feeds should be optional and clearly documented.

---

## v1.0.1 — First Public Patch Release

Focus:

```text
Small fixes found after first public download testing
```

Possible work:

```text
Fix README wording
Fix launcher wording
Improve Smart App Control instructions
Fix release ZIP naming if needed
Fix small UI wording issues
Fix first-run download issues
Improve README-FIRST.txt instructions
Improve troubleshooting notes
```

Purpose:

```text
Clean up the first stable release after real download testing.
```

---

## v1.1.0 — Better Windows Experience

Focus:

```text
Make Windows usage easier
```

Possible work:

```text
Add PowerShell fallback launcher
Add clearer Java install instructions
Improve Windows unblock instructions
Add Windows troubleshooting notes
Test on a normal Windows machine outside Codespaces
Improve handling when Java is missing
Improve launcher error messages
```

Possible files:

```text
run-web-converter.ps1
docs/WINDOWS.md
```

Purpose:

```text
Reduce Windows first-run friction.
```

---

## v1.2.0 — Better Release Package

Focus:

```text
Make the GitHub download cleaner and more professional
```

Possible work:

```text
Rename release folder to include version
Add checksum file
Add release testing checklist
Add GitHub release notes template
Add clearer first-run instructions
Add version to web page footer
Improve packaged README-FIRST.txt
Improve release artifact naming
```

Possible output:

```text
MSBF-to-TachiBK-v1.2.0.zip
SHA256SUMS.txt
```

Purpose:

```text
Make releases easier to trust, test, and distribute.
```

---

## v1.3.0 — Metadata Improvements

Focus:

```text
Improve optional MangaDex metadata mode
```

Possible work:

```text
Improve metadata progress text
Add better failed metadata summaries
Retry failed MangaDex requests
Improve rate-limit handling
Cache metadata locally
Avoid repeat API calls across runs
Improve missing metadata report formatting
Improve failed connection report formatting
```

Purpose:

```text
Make metadata fetching more reliable for large libraries.
```

---

## v1.4.0 — Duplicate Management Improvements

Focus:

```text
Give users better control over duplicates
```

Possible work:

```text
Improve duplicate report formatting
Show duplicate groups on the web page
Add preview before removing duplicates
Let user choose keep first or keep newest
Add duplicate cleanup summary
Export duplicate cleanup report
Add clearer duplicate explanations
```

Purpose:

```text
Make duplicate cleanup easier without silently deleting entries.
```

---

## v1.5.0 — Advanced Restore Backup Sections

Focus:

```text
Evaluate optional restore backup settings beyond Manga and Categories
```

V1 intentionally recommends restoring only:

```text
Manga
Categories
```

And leaving these unchecked:

```text
App Settings
Extension Repos
Source Settings
Saved Searches
Feeds
```

These sections may be considered after V1, but they should stay disabled by default until they are fully tested.

Possible future CLI options:

```text
--include-app-settings
--include-extension-repos
--include-source-settings
--include-saved-searches
--include-feeds
```

Possible future web options:

```text
Include App Settings
Include Extension Repos
Include Source Settings
Include Saved Searches
Include Feeds
```

Recommended default behavior:

```text
Do not include App Settings
Do not include Extension Repos
Do not include Source Settings
Do not include Saved Searches
Do not include Feeds
```

Testing needed:

```text
Test each backup section independently
Confirm Komikku restore accepts the generated section
Confirm the section does not break MangaDex entries
Confirm delegated sources behavior is not re-enabled accidentally
Confirm restore behavior in a clean Komikku profile
Confirm restore behavior in an existing Komikku profile
Confirm unsupported settings do not cause restore failures
Document any risk before exposing the option
```

Risks to document:

```text
App Settings may change Komikku-wide behavior
Extension Repos may alter extension repository configuration
Source Settings may affect MangaDex or other source behavior
Saved Searches may restore search data that is not needed for migration
Feeds may restore feed data that is not needed for migration
```

Purpose:

```text
Allow advanced users to restore more backup sections while keeping the default restore safe.
```

---

## v1.6.0 — Desktop Drag-and-Drop App Investigation

Focus:

```text
Explore a true desktop app
```

Possible work:

```text
Drag-and-drop .msbf file
Choose output folder
Convert button
Progress display
Open output folder button
Local-only UI
Better non-technical user experience
```

Possible packaging:

```text
Windows executable
macOS app
Linux AppImage
```

Purpose:

```text
Make the converter easier for non-technical users.
```

---

## v2.0.0 — Bigger Migration Features

Focus:

```text
Major expansion beyond basic MangaDex favorites conversion
```

Possible work:

```text
Support more Manga Storm sources
Import more backup data if Manga Storm provides enough information
More advanced restore options
Better migration reports
Native app packaging
No-Java standalone release
Better migration review flow
```

Purpose:

```text
Expand the project beyond the initial MangaDex favorites migration.
```

---

# Future Restore Backup Setting Considerations

Advanced restore sections need special care because they can change behavior outside the manga library itself.

## App Settings

Possible future goal:

```text
Optionally include Komikku app preferences
```

Risk:

```text
May change Komikku-wide behavior
May restore settings that are not wanted on the target device
May conflict with existing app configuration
May re-enable behavior that breaks MangaDex restore behavior
```

Recommended approach:

```text
Keep disabled by default
Add only after testing with clean and existing profiles
Warn users before enabling
```

---

## Extension Repos

Possible future goal:

```text
Optionally include extension repository configuration
```

Risk:

```text
May alter extension repo behavior
May restore outdated or unwanted repo settings
May affect extension installation or updates
```

Recommended approach:

```text
Keep disabled by default
Only include if exact restore behavior is verified
Warn users before enabling
```

---

## Source Settings

Possible future goal:

```text
Optionally include source-specific settings
```

Risk:

```text
May affect MangaDex behavior
May affect delegated source behavior
May cause restored entries to open incorrectly
May change source language or source preference behavior
```

Recommended approach:

```text
Keep disabled by default
Test MangaDex source settings independently
Do not include settings that cause WebView / 404 restore issues
```

---

## Saved Searches

Possible future goal:

```text
Optionally include saved searches
```

Risk:

```text
May restore search data that is not needed for library migration
May clutter the target Komikku profile
May depend on source IDs or source behavior
```

Recommended approach:

```text
Keep disabled by default
Treat as optional convenience data
Test separately from manga and category restore
```

---

## Feeds

Possible future goal:

```text
Optionally include feed data
```

Risk:

```text
May restore feed data that is not needed for favorites migration
May depend on source settings
May clutter the target Komikku profile
```

Recommended approach:

```text
Keep disabled by default
Treat as optional advanced data
Test separately from manga and category restore
```

---

# Known Limitations

Current limitations:

```text
MangaDex is the only supported source
Metadata depends on MangaDex API availability
Metadata is slower for large backups
Duplicates are kept by default
Chapters are not restored yet
Reading progress is not restored yet
Reading history is not restored yet
Tracking data is not restored yet
Saved searches are not restored yet
Feeds are not restored yet
App Settings are not restored yet
Source Settings are not restored yet
Extension Repos are not restored yet
Native Windows installer is not built yet
Native macOS app bundle is not built yet
Native Linux package is not built yet
No-Java standalone executable is not built yet
```

---

# Release Checklist

Before tagging a stable release:

```text
Run ./gradlew clean build
Run ./gradlew distZip
Test the unzipped release package
Test run-web-converter.bat on Windows
Test Smart App Control unblock instructions
Test run-web-converter.sh on Linux/macOS when possible
Test metadata unchecked
Test metadata checked
Test duplicate report download
Confirm README is current
Confirm README-FIRST.txt is current
Confirm release notes are current
Confirm roadmap is current
Confirm troubleshooting is current
```

---

# Version Plan

| Version | Focus |
|---|---|
| `v1.0.0` | First stable local web converter release |
| `v1.0.1` | First public patch release |
| `v1.1.0` | Better Windows experience |
| `v1.2.0` | Better release package |
| `v1.3.0` | Metadata improvements |
| `v1.4.0` | Duplicate management improvements |
| `v1.5.0` | Advanced restore backup sections |
| `v1.6.0` | Desktop drag-and-drop app investigation |
| `v2.0.0` | Bigger migration features |

---

# Recommended Next Step

The next release after V1 should be:

```text
v1.0.1 — First public patch release
```

This should only include small fixes discovered during real download testing.

Avoid starting major restore-setting work until the V1 download and restore flow is confirmed stable.