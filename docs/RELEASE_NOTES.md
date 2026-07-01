# Release Notes

Release notes for **MSBF-to-TachiBK**.

---

## v1.0.0 — Initial Stable Release

**MSBF-to-TachiBK v1.0.0** is the first stable release.

This release adds a downloadable local web converter package for GitHub Releases.

Users can download the release ZIP, unzip it, run the local converter, upload their Manga Storm `favorites.msbf` file, and download a generated Komikku / Tachiyomi-style `.tachibk` backup.

---

## Main V1 Workflow

```text
Download MSBF-to-TachiBK-v1.0.0.zip
Unzip it
Run the local web converter
Open http://localhost:8080
Upload favorites.msbf
Download favorites.tachibk
Restore in Komikku
```

Nothing is uploaded to the internet. The converter runs locally on the user’s computer.

---

## Included Launchers

The release ZIP includes:

```text
README-FIRST.txt
run-web-converter.bat
run-web-converter.sh
bin/MSBF-to-TachiBK
bin/MSBF-to-TachiBK.bat
```

Windows users can double-click:

```text
run-web-converter.bat
```

macOS and Linux users can run:

```bash
chmod +x run-web-converter.sh
./run-web-converter.sh
```

---

## Windows Smart App Control Note

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

---

## What Works

V1 supports:

```text
Manga Storm .msbf input
MangaDex favorites
Local browser upload/download conversion
Komikku-readable .tachibk output
Manga restore
Category restore
Optional MangaDex metadata fetching
Duplicate reporting
Optional duplicate removal
Duplicate report download from the web page
Pre-conversion validation
GitHub downloadable ZIP package
```

---

## Supported Source

| Source | Manga Storm Key | Komikku / Tachiyomi Source ID |
|---|---:|---:|
| MangaDex | `z13mangadex` | `2499283573021220255` |

---

## Category Mapping

| Manga Storm Flag | Komikku Category |
|---|---|
| `R` | Reading |
| `Y` | Following |
| `A` | On Hold |

Unknown values are left uncategorized.

---

## Metadata Behavior

Metadata fetching is **off by default** in V1.

This keeps large backups faster and helps avoid long waits.

Users can enable metadata fetching from the local web page or by passing:

```bash
--metadata
```

Metadata mode can add:

```text
Covers
Authors
Artists
Descriptions
Genres
Manga status
```

---

## Duplicate Behavior

Duplicates are kept by default.

The converter reports duplicate MangaDex entries and lets users download the duplicate report from the local web page when duplicates are found.

Users can remove duplicate MangaDex entries by enabling duplicate removal in the local web page or by passing:

```bash
--remove-duplicates
```

---

## Recommended Komikku Restore Options

When restoring the generated `.tachibk` file, check:

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

If MangaDex entries open as a WebView / website 404 page, manually disable delegated sources in Komikku.

---

## Known Limitations

V1 does not restore:

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

V1 also does not include:

```text
Native Windows installer
Native macOS app bundle
Native Linux package
No-Java standalone executable
Additional Manga Storm sources
```

Those can be added after V1.

---

## Build Artifact

The GitHub release workflow builds and publishes:

```text
MSBF-to-TachiBK-v1.0.0.zip
```

Local development can build the same style of package with:

```bash
./gradlew distZip
```

The output appears in:

```text
build/distributions/
```

---

## V1 Tag

```bash
git tag -a v1.0.0 -m "Initial stable MSBF-to-TachiBK release"
git push origin v1.0.0
```