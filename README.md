# MSBF-to-TachiBK

Convert **Manga Storm `.msbf` favorites exports** into **Komikku / Tachiyomi-style `.tachibk` backups**.

This project started as a personal migration tool for moving a Manga Storm library into Komikku, but it is being built as a reusable open-source converter for anyone with a Manga Storm backup.

---

## Current Status

🚧 **In active development**

The converter can currently:

- Parse Manga Storm `.msbf` files
- Convert MangaDex entries into `.tachibk` backup records
- Generate a Komikku-readable backup
- Normalize MangaDex URLs
- Fetch MangaDex metadata by default
- Add cover URLs, authors, artists, descriptions, genres, and manga status
- Detect duplicate manga entries
- Write duplicate, missing metadata, and failed connection reports
- Map Manga Storm status flags into Komikku categories
- Disable Komikku delegated sources in the generated backup setting to avoid MangaDex WebView / 404 issues

---

## Supported Source

Currently supported:

- **MangaDex**

Manga Storm source key:

```text
z13mangadex
```

Komikku / Tachiyomi source ID:

```text
2499283573021220255
```

More sources may be added later.

---

## Manga Storm Status Mapping

Manga Storm uses single-letter status flags.

The converter currently maps them as:

| Manga Storm Flag | Komikku Category |
|---|---|
| `R` | Reading |
| `Y` | Following |
| `A` | On Hold |

Unknown values are left uncategorized.

---

## Metadata Behavior

Metadata is fetched from MangaDex **by default** because restored entries load more reliably when metadata is included.

The converter attempts to add:

- Cover image URL
- Author
- Artist
- Description
- Genres
- Manga status:
  - Ongoing
  - Completed
  - Hiatus
  - Cancelled

To skip metadata for fast testing, use:

```bash
--no-metadata
```

---

## Build Requirements

Use:

- Java 17
- Gradle Wrapper included in the repository

Check Java:

```bash
java -version
```

Expected:

```text
openjdk version "17.x"
```

Build:

```bash
./gradlew clean build
```

---

## Running the Converter

Example test command:

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

Local test files should be stored in versioned folders:

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

Generated `.tachibk` files and personal `.msbf` files should not be committed.

---

## Restore Instructions for Komikku

Before restoring:

1. Install the **MangaDex** extension in Komikku.
2. Make sure the backup was generated with metadata unless doing a quick test.
3. Copy the generated `.tachibk` file to the Android device.
4. If using BlueStacks, move the backup into Android’s **Downloads** folder before restoring.

In Komikku, restore the backup and check:

- ✅ Manga
- ✅ Categories
- ✅ App Settings

Uncheck:

- ☐ Saved Searches
- ☐ Source Settings
- ☐ Extension Repos

The App Settings option is currently used so the backup can disable delegated sources, which helps prevent MangaDex WebView / 404 issues after restore.

---

## Current Output Reports

The converter may generate these local text reports:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

### Duplicate Report

Generated when the `.msbf` file contains duplicate MangaDex UUIDs.

Example:

```text
duplicate-manga-report.txt
```

### Missing Metadata Report

Generated when a MangaDex title exists in the `.msbf` file but metadata cannot be found.

Example:

```text
missing-metadata-links.txt
```

### Failed Connection Report

Generated when the converter cannot connect to MangaDex while fetching metadata.

Example:

```text
failed-connection-links.txt
```

These reports are for debugging and should not be committed.

---

## Development Progress

### Commit 1 — Project Foundation

Completed:

- Gradle project setup
- Kotlin JVM setup
- Gradle wrapper
- GitHub Actions build
- Basic repository structure

---

### Commit 2 — MSBF Parser

Completed:

- Reads Manga Storm `.msbf` files
- Extracts title, source key, URL, status, and timestamp
- Verified against a real Manga Storm favorites export

---

### Commit 3 — Initial `.tachibk` Generator

Completed:

- Created minimal Komikku backup models
- Generated gzip-compressed `.tachibk` backups
- Added MangaDex source ID mapping
- Confirmed Komikku can read the generated backup

---

### Commit 4 — MangaDex Metadata and Reports

Completed:

- Fetches MangaDex metadata
- Adds covers, authors, artists, descriptions, genres, and manga status
- Detects duplicate MangaDex entries
- Writes duplicate report
- Writes missing metadata report
- Writes failed connection report
- Improved terminal summary output

---

### Commit 5 — Manga Storm Status Categories

Completed:

- Maps Manga Storm status flags:
  - `R` → Reading
  - `Y` → Following
  - `A` → On Hold
- Restores categories into Komikku
- Confirmed category order handling needed to match Komikku restore behavior

---

### Commit 6 — Metadata Default and Delegated Sources Fix

Completed / in progress:

- Metadata fetch is enabled by default
- `--no-metadata` remains available for fast testing
- Backup includes the setting to disable delegated sources
- This helps prevent MangaDex WebView / 404 issues after restore

---

## Roadmap to V1

### Commit 7 — README and Restore Documentation

Planned:

- Finalize restore instructions
- Document required restore checkboxes
- Document BlueStacks file location issue
- Document metadata behavior
- Document duplicate and metadata reports
- Document current limitations

---

### Commit 8 — Better CLI Options

Planned:

- Cleaner command-line arguments
- Possible future flags:

```text
--output <file>
--metadata
--no-metadata
--dedupe-report-only
--version
--help
```

Current command works, but the CLI can be made easier to use.

---

### Commit 9 — Pre-Conversion Validation

Planned:

- Verify input file exists
- Verify input file is readable
- Create output folders automatically
- Detect unsupported sources clearly
- Detect invalid MangaDex URLs clearly
- Block empty backups
- Show validation summary before writing backup

---

### Commit 10 — Optional Duplicate Handling

Currently, duplicates are reported but not removed.

Possible future options:

```text
--keep-duplicates
--remove-duplicates
--report-duplicates-only
```

Default behavior should remain safe and avoid deleting entries automatically.

---

### Commit 11 — Full Compatibility Test

Planned before V1:

- Test full Manga Storm export
- Fetch metadata
- Restore into clean Komikku 1.13.6 profile
- Confirm MangaDex pages open correctly
- Confirm categories restore correctly
- Confirm metadata appears where Komikku supports it
- Confirm duplicate report is accurate

---

### Commit 12 — V1 Release Cleanup

Planned:

- Final README
- Version update to `1.0.0`
- Release notes
- Known issues section
- GitHub release tag

Target tag:

```bash
git tag -a v1.0.0 -m "Initial stable MSBF to TachiBK converter"
git push origin v1.0.0
```

---

## Planned After V1

Post-V1 ideas:

- Desktop drag-and-drop app
- Windows executable
- macOS app
- Linux AppImage
- Progress bar for metadata fetching
- Real popup dialogs
- Support for additional Manga Storm sources
- Optional duplicate removal
- More app settings
- Better source mapping
- Chapter/history import if Manga Storm data supports it

---

## Known Limitations

Current limitations:

- MangaDex is the only supported source
- Metadata depends on MangaDex API availability
- Categories are limited to Reading, Following, and On Hold
- Duplicates are reported but not automatically removed
- Chapter progress is not restored yet
- Reading history is not restored yet
- Tracker data is not restored yet
- Desktop app is not built yet

---

## Repository Safety

Do not commit personal backup files.

Ignored file types should include:

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