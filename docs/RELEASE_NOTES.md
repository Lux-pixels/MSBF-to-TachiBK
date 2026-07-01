# Release Notes

Release notes for **MSBF-to-TachiBK**.

---

## v1.0.0 — Initial Stable Release

**MSBF-to-TachiBK v1.0.0** is the first stable command-line release.

This release focuses on converting Manga Storm `.msbf` favorites exports containing MangaDex entries into Komikku / Tachiyomi-style `.tachibk` backups.

---

## What Works

v1.0.0 supports:

```text
Manga Storm .msbf input
MangaDex entries
Komikku-readable .tachibk output
Manga restore
Category restore
MangaDex metadata fetching
Duplicate reporting
Optional duplicate removal
Duplicate report-only mode
Missing metadata reporting
Failed connection reporting
Pre-conversion validation
Clear restore instructions
Safe default behavior
```

---

## Supported Source

Currently supported:

| Source | Manga Storm Key | Komikku / Tachiyomi Source ID |
|---|---:|---:|
| MangaDex | `z13mangadex` | `2499283573021220255` |

More sources may be added after V1.

---

## Category Mapping

Manga Storm status flags are mapped as:

| Manga Storm Flag | Komikku Category |
|---|---|
| `R` | Reading |
| `Y` | Following |
| `A` | On Hold |

Unknown values are left uncategorized.

---

## CLI Examples

Recommended conversion:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/MSBF-to-TachiBK-v1.0.tachibk"
```

Quick test without metadata:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/quick-test.tachibk --no-metadata"
```

Duplicate report only:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --report-duplicates-only"
```

Remove duplicates:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v1.0/deduped.tachibk --remove-duplicates"
```

---

## Recommended Komikku Restore Options

When restoring the generated backup, check:

```text
Manga
Categories
```

Uncheck:

```text
App Settings
Saved Searches
Source Settings
Extension Repos
```

Manually disable delegated sources in Komikku if MangaDex entries open as a WebView / website 404 page.

---

## Known Limitations

v1.0.0 does not restore:

```text
Chapters
Reading progress
Reading history
Tracking data
Saved searches
App settings
Source settings
Extension repos
```

v1.0.0 also does not include:

```text
Desktop app
Native Windows package
Native macOS package
Native Linux package
Additional Manga Storm sources
```

These are planned for future releases.

---

## Safety Notes

The converter keeps duplicates by default.

Users must explicitly choose duplicate removal with:

```bash
--remove-duplicates
```

Personal backup files should not be committed:

```text
*.msbf
*.tachibk
*.proto.gz
```

Generated reports should also not be committed:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

---

## Tag

```bash
git tag -a v1.0.0 -m "Initial stable MSBF to TachiBK converter"
git push origin v1.0.0
```