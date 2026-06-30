# Testing Guide

Compatibility testing checklist for **MSBF-to-TachiBK**.

This document is used for the Commit 11 / `v0.9.0` compatibility test before the first stable V1 release.

---

## Test Goal

Confirm that MSBF-to-TachiBK can convert a real Manga Storm `.msbf` export into a Komikku-readable `.tachibk` backup and restore it successfully.

The test should verify:

```text
Full .msbf export parses correctly
Metadata fetch works
Duplicate handling works
Backup file is created
Backup restores into Komikku
MangaDex entries open correctly
Categories restore correctly
Reports are generated correctly
App Settings remain unchecked during restore
Delegated sources are manually disabled in Komikku
```

---

## Test Environment

Recommended test environment:

```text
Komikku version: 1.13.6
Source extension: MangaDex
Input: Real Manga Storm .msbf export
Output: .tachibk backup
Restore profile: Clean Komikku profile when possible
```

---

## Pre-Test Checklist

Before running the converter:

```text
Commit 10 is complete
Working tree is clean
Java 17 is active
MangaDex extension is installed in Komikku
Delegated sources are manually disabled in Komikku
testdata/v0.9 folder exists
Personal .msbf and .tachibk files are ignored by Git
```

Check Java:

```bash
java -version
```

Build:

```bash
./gradlew clean build
```

Version:

```bash
./gradlew run --args="--version"
```

Expected:

```text
MSBF-to-TachiBK 0.9.0
```

---

## Test Data Folder

Create the v0.9 test folder:

```bash
mkdir -p testdata/v0.9
touch testdata/v0.9/.gitkeep
```

Generated `.tachibk` files should stay untracked.

---

## Test 1 — Help Command

Run:

```bash
./gradlew run --args="--help"
```

Pass criteria:

```text
Help text prints successfully
CLI options are listed
Duplicate options are listed
No backup is written
```

---

## Test 2 — Version Command

Run:

```bash
./gradlew run --args="--version"
```

Pass criteria:

```text
MSBF-to-TachiBK 0.9.0
```

---

## Test 3 — Validation Failure

Run an intentional failure:

```bash
./gradlew run --args="convert samples/missing.msbf --output testdata/v0.9/bad-output.txt"
```

Pass criteria:

```text
Validation Summary is printed
Missing input file is reported
Invalid output extension is reported
No backup was written
```

---

## Test 4 — Quick Conversion Without Metadata

Run:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.9/MSBF-to-TachiBK-v0.9-quick.tachibk --no-metadata"
```

Pass criteria:

```text
Validation passes
Backup summary prints
Duplicate check runs
Metadata fetch is skipped
Backup is written
```

---

## Test 5 — Duplicate Report Only

Run:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --report-duplicates-only"
```

Pass criteria:

```text
Validation passes
Duplicate check runs
duplicate-manga-report.txt is written if duplicates exist
No metadata is fetched
No backup is written
```

---

## Test 6 — Remove Duplicates

Run:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.9/MSBF-to-TachiBK-v0.9-deduped.tachibk --remove-duplicates --no-metadata"
```

Pass criteria:

```text
Validation passes
Duplicate check runs
Duplicate removal summary prints
Backup uses deduped entry count
Backup is written
```

---

## Test 7 — Full Metadata Conversion

Run:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.9/MSBF-to-TachiBK-v0.9-full.tachibk"
```

Pass criteria:

```text
Validation passes
Metadata fetch starts
Metadata progress prints
Metadata summary prints
Backup is written
Reports are generated only when needed
```

Expected reports may include:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

---

## Test 8 — Full Real Export Conversion

Run the converter with the real Manga Storm export.

Example:

```bash
./gradlew run --args="convert samples/favorites.msbf --output testdata/v0.9/MSBF-to-TachiBK-v0.9-real-export.tachibk"
```

Pass criteria:

```text
Full export parses
Validation passes
Metadata fetch completes
Backup is written
No unexpected crash occurs
Reports look reasonable
```

---

## Restore Test in Komikku

Before restoring:

```text
Install MangaDex extension
Use a clean Komikku profile if possible
Copy .tachibk file to Android Downloads
Manually disable delegated sources in Komikku
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

---

## Komikku Restore Pass Criteria

After restoring:

```text
Manga list appears
Categories appear
Reading category has expected entries
Following category has expected entries
On Hold category has expected entries
MangaDex entries open through the MangaDex source
Entries do not open as WebView / website 404 pages
Metadata appears where Komikku supports it
No restore crash occurs
```

---

## Duplicate Behavior Pass Criteria

Default conversion:

```text
Duplicates are reported
Duplicates are kept
Backup is still written
```

`--report-duplicates-only`:

```text
Duplicate report is written
No backup is written
```

`--remove-duplicates`:

```text
First copy is kept
Later duplicate copies are removed
Backup is written with fewer entries
```

---

## Reports Checklist

Check these files after test runs:

```text
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

Pass criteria:

```text
Reports are created only when needed
Reports are readable
Reports include useful URLs/reasons
Reports are not committed to Git
```

---

## Git Safety Checklist

Before committing:

```bash
git status
```

Make sure these are not committed:

```text
*.msbf
*.tachibk
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
```

Files expected in Commit 11:

```text
src/main/kotlin/config/Constants.kt
docs/TESTING.md
README.md
docs/ROADMAP.md
testdata/v0.9/.gitkeep
```

---

## Commit 11 Pass Criteria

Commit 11 is complete when:

```text
v0.9.0 version prints correctly
All CLI test modes run
Full metadata conversion works
Real export conversion works
Komikku restore succeeds
Categories restore correctly
MangaDex entries open correctly
No personal backup files are staged
Testing guide is committed
```