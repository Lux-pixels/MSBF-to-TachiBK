# MSBF-to-TachiBK

<p align="center">
  <strong>Convert Manga Storm <code>.msbf</code> favorites exports into Komikku / Tachiyomi-style <code>.tachibk</code> backups.</strong>
</p>

<p align="center">
  <img alt="Status" src="https://img.shields.io/badge/status-V1%20stable-brightgreen">
  <img alt="Version" src="https://img.shields.io/badge/current-v1.0.0-blue">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-JVM-purple">
  <img alt="Source" src="https://img.shields.io/badge/source-MangaDex-blue">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-green">
</p>

---

## What Is This?

**MSBF-to-TachiBK** is a migration tool for moving a Manga Storm library into Komikku.

It reads a Manga Storm `.msbf` favorites export, converts supported MangaDex entries into Komikku-compatible backup records, and writes a `.tachibk` backup file that can be restored in Komikku.

The V1 release includes a local browser converter:

```text
Upload favorites.msbf
Convert locally
Download favorites.tachibk
Restore in Komikku
```

Nothing is uploaded to the internet. The converter runs on your own computer.

---

## Current Status

✅ **V1 stable release**

Current release:

```text
v1.0.0 — Initial stable local web converter
```

The converter currently supports Manga Storm exports containing MangaDex entries.

---

## Download and Run

Go to the GitHub **Releases** page and download:

```text
MSBF-to-TachiBK-v1.0.0.zip
```

Unzip the file.

### Windows

Double-click:

```text
run-web-converter.bat
```

Then open:

```text
http://localhost:8080
```

Leave the command window open while converting.

### Windows Smart App Control Note

If Windows Smart App Control blocks `run-web-converter.bat`, unblock the downloaded ZIP before extracting it:

```text
1. Delete the extracted MSBF-to-TachiBK folder.
2. Right-click MSBF-to-TachiBK-v1.0.0.zip.
3. Click Properties.
4. Check Unblock if you see it.
5. Click Apply.
6. Extract the ZIP again.
7. Double-click run-web-converter.bat again.
```

This happens because Windows may block unknown downloaded script files after extraction.

### macOS / Linux

Open a terminal in the unzipped folder and run:

```bash
chmod +x run-web-converter.sh
./run-web-converter.sh
```

Then open:

```text
http://localhost:8080
```

Leave the terminal window open while converting.

---

## Requirements

You need:

```text
Java 17 or newer
A Manga Storm .msbf favorites export
Komikku or another compatible Tachiyomi-style app
```

Check Java:

```bash
java -version
```

---

## Local Web Converter

The V1 local web converter lets you:

- Choose a Manga Storm `favorites.msbf` file
- Convert it into a `.tachibk` backup
- See conversion status directly on the page
- See Manga totals, source counts, category counts, and duplicate counts
- Download the generated `.tachibk` file
- Optionally download a duplicate report when duplicates are found

Metadata fetching is **off by default** for speed.

You can turn on MangaDex metadata fetching in the browser if you want covers, authors, descriptions, genres, and manga status. Large backups may take longer with metadata enabled.

---

## Features

Currently supported:

- Parse Manga Storm `.msbf` files
- Convert MangaDex entries into `.tachibk` backups
- Normalize MangaDex URLs for Komikku
- Run a local browser upload/download converter
- Show conversion status directly on the page
- Show Manga total counts
- Show MangaDex source counts
- Show Manga Storm category counts
- Detect duplicate MangaDex entries
- Keep duplicates by default for safety
- Optionally remove duplicate copies
- Offer duplicate report download from the web page
- Optionally fetch MangaDex metadata
- Add title, author, artist, description, genres, status, and cover metadata when metadata mode is enabled
- Restore Manga Storm categories into Komikku
- Validate input, output, sources, and MangaDex URLs before writing a backup
- Support command-line usage for developers
- Create output folders automatically when possible
- Build a downloadable GitHub release ZIP

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

## Recommended Komikku Restore Options

Before restoring:

1. Install Komikku.
2. Install the MangaDex extension.
3. Generate the `.tachibk` backup.
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

## Command-Line Usage

Developers can still run the converter from source.

Show help:

```bash
./gradlew run --args="--help"
```

Show version:

```bash
./gradlew run --args="--version"
```

Start the local web converter:

```bash
./gradlew run --args="serve"
```

Convert directly from the command line:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/MSBF-to-TachiBK-v1.0.tachibk"
```

Old command style is still supported:

```bash
./gradlew run --args="samples/testfavorites.msbf testdata/v1.0/MSBF-to-TachiBK-v1.0.tachibk"
```

Convert with MangaDex metadata:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/MSBF-to-TachiBK-v1.0-metadata.tachibk --metadata"
```

Remove duplicate MangaDex entries:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/MSBF-to-TachiBK-v1.0-deduped.tachibk --remove-duplicates"
```

---

## CLI Options

```text
serve                      Start local browser upload/download converter
convert                    Optional command word for the newer command style
--output, -o <file>         Output .tachibk file path
--metadata                  Fetch MangaDex metadata
--no-metadata               Skip MangaDex metadata; default behavior
--report-duplicates-only    Write duplicate report and stop before backup creation
--remove-duplicates         Keep first MangaDex entry and remove later duplicate copies
--version, -v               Print version
--help, -h                  Show help
```

Metadata is skipped by default.

Duplicates are kept by default.

---

## Duplicate Handling

Default behavior is safe:

```text
Duplicates are reported but kept.
```

The local web converter will offer a duplicate report download when duplicate MangaDex entries are found.

Report duplicates only from the command line:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --report-duplicates-only"
```

Remove duplicates:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/deduped.tachibk --remove-duplicates"
```

`--remove-duplicates` keeps the first copy of each MangaDex UUID and removes later duplicate copies.

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
testdata/v0.10/
testdata/v0.11/
testdata/v1.0/
```

Example:

```text
testdata/v1.0/MSBF-to-TachiBK-v1.0test.tachibk
```

Generated backup files should not be committed.

To keep a test folder in GitHub:

```bash
mkdir -p testdata/v1.0
touch testdata/v1.0/.gitkeep
```

---

## Documentation

More project documentation:

- [Restore Guide](docs/RESTORE.md)
- [Troubleshooting](docs/TROUBLESHOOTING.md)
- [Testing Guide](docs/TESTING.md)
- [Roadmap](docs/ROADMAP.md)
- [Release Notes](docs/RELEASE_NOTES.md)

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
| 15B | V1 docs and release notes | 🚧 Current |

See the full [Roadmap](docs/ROADMAP.md).

---

## V1 Goal

The V1 release provides a stable local web converter for:

```text
Manga Storm .msbf input
MangaDex entries
Komikku-readable .tachibk output
Manga restore
Category restore
Optional metadata fetching
Duplicate reporting
Optional duplicate removal
Duplicate report download
Pre-conversion validation
Clear restore instructions
GitHub downloadable release ZIP
Safe default behavior
```

---

## Known Limitations

Current limitations:

- MangaDex is the only supported source
- Metadata depends on MangaDex API availability
- Metadata is slower for large backups
- Duplicates are kept by default
- Chapters are not restored yet
- Reading progress is not restored yet
- Reading history is not restored yet
- Tracking data is not restored yet
- App Settings are not restored yet
- Source Settings are not restored yet
- Extension Repos are not restored yet
- Native Windows installer is not built yet
- Native macOS app bundle is not built yet
- Native Linux package is not built yet
- No-Java standalone executable is not built yet

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

## Build From Source

Build:

```bash
./gradlew clean build
```

Build the downloadable distribution ZIP:

```bash
./gradlew distZip
```

The ZIP will be created in:

```text
build/distributions/
```

---

## Planned After V1

Post-V1 development may focus on:

- Native Windows installer
- Native macOS app bundle
- Native Linux package
- No-Java standalone executable
- Desktop drag-and-drop app
- Optional restore selections
- App Settings selection
- Extension Repos selection
- Source Settings selection
- More Manga Storm sources
- More backup data if Manga Storm provides enough information

---

## License

This project is licensed under the MIT License.