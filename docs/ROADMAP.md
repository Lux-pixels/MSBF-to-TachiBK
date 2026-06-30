# Roadmap

Development roadmap for **MSBF-to-TachiBK**.

---

## Completed

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

### Commit 6 — Metadata Default

Completed:

```text
Fetch metadata by default
Keep --no-metadata for quick tests
Improve conversion defaults for restored MangaDex entries
Start versioned testdata folder convention
```

---

### Commit 7 — Documentation

Completed:

```text
README progress summary
Restore guide
Troubleshooting guide
Roadmap file
Known limitations
Recommended restore settings
```

---

### Commit 8 — Better CLI Options

Current:

```text
Add --help
Add --version
Add --output / -o
Keep --metadata and --no-metadata
Support optional convert command
Support old command style
Create output folders automatically
Update documentation for safer restore behavior
Keep App Settings restore disabled for now
```

---

## Remaining Before V1

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
Confirm delegated sources are manually disabled
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

## After V1

Possible future features:

```text
Desktop drag-and-drop app
Windows executable
macOS app
Linux AppImage
Progress bar for metadata fetching
Popup dialogs
Support for more Manga Storm sources
Optional duplicate removal
More app settings after exact backup behavior is verified
Better source mapping
Chapter import if available
Reading history import if available
Tracking import if available
```