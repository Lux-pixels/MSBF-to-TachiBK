# MSBF-to-TachiBK

Convert **Manga Storm `.msbf` favorites exports** into **Komikku / Tachiyomi-style `.tachibk` backups**.

This project is being built as a migration tool for moving a Manga Storm library into Komikku. The current focus is **MangaDex support**, reliable Komikku restore behavior, category mapping, metadata enrichment, duplicate reporting, and clear restore documentation.

---

## Project Status

🚧 **Active development — pre-V1**

Current milestone:

```text
v0.5.x documentation / restore guidance phase
```

The converter currently works for MangaDex entries and can generate Komikku-readable `.tachibk` backups.

---

## Documentation

More detailed documentation:

- [Restore Guide](docs/RESTORE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Roadmap](docs/ROADMAP.md)

---

## What Works Now

The converter can currently:

- Parse Manga Storm `.msbf` files
- Detect MangaDex entries
- Convert MangaDex entries into Komikku/Tachiyomi backup records
- Generate `.tachibk` files
- Normalize MangaDex URLs
- Fetch MangaDex metadata by default
- Add title, author, artist, description, genre, status, and cover metadata
- Detect duplicate MangaDex entries
- Write duplicate reports
- Write missing metadata reports
- Write failed connection reports
- Map Manga Storm status flags into Komikku categories
- Disable delegated sources in the generated backup setting to help avoid MangaDex WebView / 404 restore issues

---

## Supported Sources

Currently supported:

| Source | Manga Storm Key | Komikku / Tachiyomi Source ID |
|---|---:|---:|
| MangaDex | `z13mangadex` | `2499283573021220255` |

More sources may be added after V1.

---

## Manga Storm Category Mapping

Manga Storm uses single-letter status flags. These are currently mapped into Komikku categories:

| Manga Storm Flag | Komikku Category |
|---|---|
| `R` | Reading |
| `Y` | Following |
| `A` | On Hold |

Unknown or unsupported flags are left uncategorized.

---

## Metadata Behavior

Metadata is fetched from MangaDex **by default** because restored entries load more reliably when metadata is included.

The converter attempts to add:

```text
Title
Author
Artist
Description
Genres
Cover URL
Manga status
```

To skip metadata for a quick test backup, use:

```bash
--no-metadata
```

---

## Build Requirements

Use:

```text
Java 17
Gradle Wrapper
Kotlin JVM
```

Check Java:

```bash
java -version
```

Expected:

```text
openjdk version "17.x"
```

Build the project:

```bash
./gradlew clean build
```

---

## Running the Converter

Example command:

```bash
./gradlew clean build
./gradlew run --args="samples/testfavorites.msbf testdata/v0.4/MSBF-to-TachiBK-v0.4test.tachibk"
```

Quick test without metadata:

```bash
./gradlew run --args="samples/testfavorites.msbf testdata/v0.4/MSBF-to-TachiBK-v0.4test-no-meta.tachibk --no-metadata"
```

Metadata is enabled automatically unless `--no-metadata` is provided.

---

## Test Data Folder Convention

Local test output should use versioned folders:

```text
testdata/v0.1/
testdata/v0.2/
testdata/v0.3/
testdata/v0.4/
testdata/v1.0/
```

Example:

```text
testdata/v0.4/MSBF-to-TachiBK-v0.4test.tachibk
```

Generated backup files should not be committed.

Use `.gitkeep` files if a versioned folder should exist in GitHub:

```bash
mkdir -p testdata/v0.4
touch testdata/v0.4/.gitkeep
```

---

## Restore Instructions for Komikku

Before restoring:

1. Install Komikku.
2. Install the MangaDex extension.
3. Generate the backup with metadata enabled.
4. Copy the `.tachibk` file onto the Android device.
5. Restore the backup inside Komikku.

Recommended restore options:

```text
Check:
- Manga
- Categories
- App Settings

Uncheck:
- Saved Searches
- Source Settings
- Extension Repos
```

The generated backup currently includes one app setting:

```text
enable_delegated_sources = false
```

This is included because delegated MangaDex behavior can cause restored entries to open in a WebView / website 404 page instead of opening correctly through the MangaDex source.

For more details, see:

```text
docs/RESTORE.md
```

---

## BlueStacks Restore Note

When using BlueStacks, Komikku may fail to restore directly from the shared host folder.

Recommended workaround:

```text
Copy the .tachibk file into Android Downloads first.
Restore from Downloads inside Komikku.
```

More details are in:

```text
docs/TROUBLESHOOTING.md
```

---

## Generated Reports

The converter may generate these local report files:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

### Duplicate Report

Created when duplicate MangaDex UUIDs are found.

```text
duplicate-manga-report.txt
```

### Missing Metadata Report

Created when a MangaDex entry cannot be found or parsed.

```text
missing-metadata-links.txt
```

### Failed Connection Report

Created when the converter cannot connect to MangaDex while fetching metadata.

```text
failed-connection-links.txt
```

These reports are for debugging and should not be committed.

---

## Development Progress

### Commit 1 — Project Foundation

Completed:

```text
Gradle project setup
Kotlin JVM setup
Gradle wrapper
GitHub Actions build
Basic repository structure
```

---

### Commit 2 — MSBF Parser

Completed:

```text
Read Manga Storm .msbf files
Extract manga title
Extract Manga Storm source key
Extract MangaDex URL
Extract Manga Storm status flag
Extract timestamp when available
```

---

### Commit 3 — Initial .tachibk Generator

Completed:

```text
Create minimal Komikku backup models
Generate gzip-compressed .tachibk backup
Map MangaDex source ID
Normalize MangaDex URLs
Confirm Komikku can read generated backup
```

---

### Commit 4 — MangaDex Metadata and Reports

Completed:

```text
Fetch MangaDex metadata
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

---

### Commit 5 — Manga Storm Status Categories

Completed:

```text
Map R to Reading
Map Y to Following
Map A to On Hold
Restore categories into Komikku
Confirm Komikku category order behavior
```

---

### Commit 6 — Metadata Default and Delegated Sources Fix

Completed:

```text
Fetch metadata by default
Keep --no-metadata for quick tests
Add delegated sources app setting
Disable delegated sources in generated backup
Start versioned testdata folder convention
```

---

### Commit 7 — Documentation

Current documentation phase:

```text
README progress summary
Restore guide
Troubleshooting guide
Roadmap file
Known limitations
Recommended restore settings
```

---

## Remaining Before V1

### Commit 8 — Better CLI Options

Planned:

```text
Add --help
Add --version
Add --output <file>
Improve argument parsing
Make command usage clearer
Create output folders automatically
```

Possible future command format:

```bash
./gradlew run --args="convert samples/favorites.msbf --output testdata/v1.0/favorites.tachibk"
```

---

### Commit 9 — Pre-Conversion Validation

Planned:

```text
Verify input file exists
Verify input file is readable
Verify output folder exists
Create output folder when missing
Detect unsupported sources before conversion
Detect invalid MangaDex URLs before conversion
Warn about empty backups
Print validation summary
```

---

### Commit 10 — Optional Duplicate Handling

Planned:

```text
Keep duplicates by default
Add duplicate report-only mode
Possibly add --remove-duplicates
Clearly show which entries would be removed
Avoid deleting anything without explicit user choice
```

---

### Commit 11 — Full Compatibility Test

Planned:

```text
Run full real Manga Storm export
Fetch metadata
Generate final pre-V1 backup
Restore into clean Komikku 1.13.6 profile
Confirm MangaDex entries open correctly
Confirm categories restore correctly
Confirm metadata appears where supported
Confirm reports are accurate
```

---

### Commit 12 — V1 Release Cleanup

Planned:

```text
Update version to 1.0.0
Finalize README
Finalize restore instructions
Add known issues section
Add release notes
Create GitHub release
Tag v1.0.0
```

Target V1 tag:

```bash
git tag -a v1.0.0 -m "Initial stable MSBF to TachiBK converter"
git push origin v1.0.0
```

---

## Planned After V1

Possible post-V1 features:

```text
Desktop drag-and-drop app
Windows executable
macOS app
Linux AppImage
Progress bar for metadata fetching
Popup dialogs
Support for more Manga Storm sources
Optional duplicate removal
More app settings
Better source mapping
Chapter import if available
Reading history import if available
Tracking import if available
```

---

## Known Limitations

Current limitations:

```text
MangaDex is the only supported source
Metadata depends on MangaDex API availability
Duplicates are reported but not automatically removed
Chapters are not restored yet
Reading progress is not restored yet
Reading history is not restored yet
Tracker data is not restored yet
Saved searches are not restored yet
Source settings are not restored yet
Extension repos are not restored yet
Desktop app is not built yet
```

---

## Repository Safety

Do not commit personal backup files.

Recommended `.gitignore` entries:

```gitignore
*.msbf
*.tachibk
*.proto.gz

duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

---

## License

This project is licensed under the MIT License.