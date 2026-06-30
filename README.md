# MSBF-to-TachiBK

<p align="center">
  <strong>Convert Manga Storm <code>.msbf</code> favorites exports into Komikku / Tachiyomi-style <code>.tachibk</code> backups.</strong>
</p>

<p align="center">
  <img alt="Status" src="https://img.shields.io/badge/status-pre--V1-orange">
  <img alt="Version" src="https://img.shields.io/badge/current-v0.9.x-blue">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-JVM-purple">
  <img alt="Source" src="https://img.shields.io/badge/source-MangaDex-blue">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-green">
</p>

---

## What Is This?

**MSBF-to-TachiBK** is a migration tool for moving a Manga Storm library into Komikku.

It reads a Manga Storm `.msbf` favorites export, converts supported entries into Komikku-compatible backup records, enriches them with MangaDex metadata, and writes a `.tachibk` backup file that can be restored in Komikku.

The project is currently focused on:

```text
MangaDex support
Reliable Komikku restore behavior
Metadata enrichment
Category mapping
Duplicate reporting
Safe validation
Clear restore instructions
```

---

## Current Status

🚧 **Active development — pre-V1**

Current milestone:

```text
v0.9.x — Full compatibility testing
```

The converter currently works for MangaDex entries and can generate Komikku-readable `.tachibk` backups.

---

## Features

Currently supported:

- Parse Manga Storm `.msbf` files
- Convert MangaDex entries into `.tachibk` backups
- Normalize MangaDex URLs for Komikku
- Fetch MangaDex metadata by default
- Add title, author, artist, description, genres, status, and cover metadata
- Restore Manga Storm categories into Komikku
- Validate input, output, sources, and MangaDex URLs before writing a backup
- Detect duplicate MangaDex entries
- Keep duplicates by default for safety
- Optionally generate a duplicate report only
- Optionally remove duplicate copies
- Generate duplicate and metadata issue reports
- Support old and new command styles
- Create output folders automatically when possible

---

## Supported Sources

| Source | Manga Storm Key | Komikku / Tachiyomi Source ID |
|---|---:|---:|
| MangaDex | `z13mangadex` | `2499283573021220255` |

More sources may be added after V1.

---

## Category Mapping

Manga Storm status flags are converted into Komikku categories:

| Manga Storm Flag | Komikku Category |
|---|---|
| `R` | Reading |
| `Y` | Following |
| `A` | On Hold |

Unknown values are left uncategorized.

---

## Requirements

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

Build:

```bash
./gradlew clean build
```

---

## Quick Start

Show help:

```bash
./gradlew run --args="--help"
```

Show version:

```bash
./gradlew run --args="--version"
```

Recommended command:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.8/MSBF-to-TachiBK-v0.8test.tachibk"
```

Old command style is still supported:

```bash
./gradlew run --args="samples/testfavorites.msbf testdata/v0.8/MSBF-to-TachiBK-v0.8test.tachibk"
```

Quick test without metadata:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.8/MSBF-to-TachiBK-v0.8test-no-meta.tachibk --no-metadata"
```

Metadata is enabled by default.

---

## CLI Options

```text
convert                    Optional command word for the newer command style
--output, -o <file>         Output .tachibk file path
--metadata                  Fetch MangaDex metadata; default behavior
--no-metadata               Skip MangaDex metadata for quick tests
--report-duplicates-only    Write duplicate report and stop before backup creation
--remove-duplicates         Keep first MangaDex entry and remove later duplicate copies
--version, -v               Print version
--help, -h                  Show help
```

Supported command styles:

```bash
./gradlew run --args="convert <input.msbf> --output <output.tachibk>"
```

```bash
./gradlew run --args="<input.msbf> <output.tachibk>"
```

---

## Duplicate Handling

Default behavior is safe:

```text
Duplicates are reported but kept.
```

Report duplicates only:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --report-duplicates-only"
```

Remove duplicates:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.8/deduped.tachibk --remove-duplicates"
```

`--remove-duplicates` keeps the first copy of each MangaDex UUID and removes later duplicate copies.

---

## Restore in Komikku

Before restoring:

1. Install Komikku.
2. Install the MangaDex extension.
3. Generate the backup with metadata enabled.
4. Copy the `.tachibk` file to your Android device.
5. Restore from inside Komikku.

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

### Important MangaDex Note

For now, do **not** restore App Settings.

Manually disable delegated sources in Komikku:

```text
Komikku settings → Advanced / Developer tools → Disable Enable delegated sources
```

This helps prevent restored MangaDex entries from opening as a WebView / website 404 page.

More details:

- [Restore Guide](docs/RESTORE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)

---

## Output Reports

The converter may generate local report files:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

These help identify:

- Duplicate MangaDex entries
- Removed or invalid MangaDex links
- MangaDex connection failures

These files are for debugging and should not be committed.

---

## Test Data Convention

Local test output should use versioned folders:

```text
testdata/v0.1/
testdata/v0.2/
testdata/v0.3/
testdata/v0.4/
testdata/v0.5/
testdata/v0.6/
testdata/v0.7/
testdata/v0.8/
testdata/v0.9/
testdata/v1.0/
```

Example:

```text
testdata/v0.8/MSBF-to-TachiBK-v0.8test.tachibk
```

Generated backup files should not be committed.

To keep a test folder in GitHub:

```bash
mkdir -p testdata/v0.9
touch testdata/v0.9/.gitkeep
```

---

## Documentation

More project documentation:

- [Restore Guide](docs/RESTORE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Roadmap](docs/ROADMAP.md)

---

## Development Progress

| Commit | Milestone | Status |
|---:|---|---|
| 1 | Gradle project foundation | ✅ Done |
| 2 | Manga Storm `.msbf` parser | ✅ Done |
| 3 | Initial `.tachibk` backup generator | ✅ Done |
| 4 | MangaDex metadata and reports | ✅ Done |
| 5 | Manga Storm status categories | ✅ Done |
| 6 | Metadata fetched by default | ✅ Done |
| 7 | Documentation and restore guide | ✅ Done |
| 8 | Better CLI options | ✅ Done |
| 9 | Pre-conversion validation | ✅ Done |
| 10 | Optional duplicate handling | ✅ Done |
| 11 | Full compatibility test | 🚧 Current |
| 12 | V1 release cleanup | Planned |
| 13 | Optional restore selections | Planned after V1 |
| 14 | Windows 10 compatibility | Planned after V1 |
| 15 | macOS Apple Silicon compatibility | Planned after V1 |
| 16 | macOS Intel compatibility | Planned after V1 |
| 17 | Linux x64 compatibility | Planned after V1 |

See the full [Roadmap](docs/ROADMAP.md).

---

## V1 Goal

The V1 release should provide a stable command-line converter for:

```text
Manga Storm .msbf input
MangaDex entries
Komikku-readable .tachibk output
Manga restore
Category restore
Metadata fetching
Duplicate reporting
Optional duplicate removal
Pre-conversion validation
Clear restore instructions
Safe default behavior
```

---

## Known Limitations

Current limitations:

- MangaDex is the only supported source
- Metadata depends on MangaDex API availability
- Duplicates are kept by default
- Chapters are not restored yet
- Reading progress is not restored yet
- Reading history is not restored yet
- Tracking data is not restored yet
- App Settings are not restored yet
- Source Settings are not restored yet
- Extension Repos are not restored yet
- Desktop app is not built yet
- Native platform packages are not built yet

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

## Planned After V1

Post-V1 development will focus on:

- Optional restore selections
- App Settings selection
- Extension Repos selection
- Source Settings selection
- Windows 10 compatibility
- macOS Apple Silicon compatibility
- macOS Intel compatibility
- Linux x64 compatibility
- Desktop drag-and-drop app
- More Manga Storm sources
- More backup data if Manga Storm provides enough information

---

## License

This project is licensed under the MIT License.