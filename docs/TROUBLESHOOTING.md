# Troubleshooting

Troubleshooting guide for **MSBF-to-TachiBK**.

---

## Windows Smart App Control Blocks the App

Windows may block `run-web-converter.bat` after downloading the release ZIP.

If you see a message like:

```text
Smart App Control has blocked an app with a dangerous file extension
```

Use this fix:

```text
1. Delete the extracted MSBF-to-TachiBK folder.
2. Right-click MSBF-to-TachiBK-v1.0.0.zip.
3. Click Properties.
4. Check Unblock if you see it.
5. Click Apply.
6. Extract the ZIP again.
7. Double-click run-web-converter.bat again.
```

This happens because Windows may mark files from the internet as blocked. Unblocking the ZIP before extraction allows the launcher file to run normally.

---

## The Web Page Does Not Open

The local web converter runs at:

```text
http://localhost:8080
```

Make sure:

```text
The command window is still open
The converter says it started successfully
You are opening http://localhost:8080
Another app is not already using port 8080
```

If the terminal was closed, start the converter again.

Windows:

```text
Double-click run-web-converter.bat
```

macOS / Linux:

```bash
./run-web-converter.sh
```

Developers running from source:

```bash
./gradlew run --args="serve"
```

---

## The Converter Window Opens and Immediately Closes

This usually means Java is missing or the release package was not fully extracted.

Check Java:

```bash
java -version
```

You need:

```text
Java 17 or newer
```

Also make sure the release ZIP was fully extracted before running the launcher.

The unzipped folder should include:

```text
README-FIRST.txt
run-web-converter.bat
run-web-converter.sh
bin/
lib/
```

---

## Java Is Not Installed

Install Java 17 or newer.

After installing Java, open a new terminal or command window and run:

```bash
java -version
```

Then try the converter again.

---

## Upload Does Not Work

Make sure the file is a Manga Storm `.msbf` favorites export.

The file should end with:

```text
.msbf
```

If the file is zipped or renamed, extract or rename it first.

---

## Conversion Is Slow

Metadata fetching can be slow for large libraries.

For fastest conversion:

```text
Leave Fetch MangaDex metadata unchecked
```

Metadata is off by default in V1.

Enable metadata only if you want extra information like:

```text
Covers
Authors
Artists
Descriptions
Genres
Manga status
```

---

## The Browser Shows Conversion Running for a Long Time

Large files can take time, especially with metadata enabled.

Try again with metadata unchecked.

If the browser still waits too long:

```text
Stop the converter
Restart it
Run the conversion again with metadata unchecked
```

---

## Duplicate Manga Are Found

This is normal if the Manga Storm export contains the same MangaDex title more than once.

Default behavior:

```text
Duplicates are reported but kept
```

The local web page will offer a duplicate report download when duplicates are found.

You can choose to remove duplicates by checking:

```text
Remove duplicate MangaDex entries
```

This keeps the first copy and removes later duplicate copies.

---

## The Duplicate Report Is Missing

A duplicate report is only available when duplicate MangaDex entries are found.

If there are no duplicates, the duplicate report download button will not appear.

---

## Manga Restore Works but Entries Open as WebView / 404

If restored MangaDex entries open as a WebView or website 404 page, manually disable delegated sources in Komikku.

Recommended restore behavior:

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

Then disable delegated sources in Komikku:

```text
Komikku settings → Advanced / Developer tools → Disable Enable delegated sources
```

---

## Manga Are Restored Without Covers or Descriptions

Metadata fetching may have been skipped.

V1 defaults to:

```text
Metadata fetch: skipped
```

To include more metadata, enable:

```text
Fetch MangaDex metadata
```

Metadata can add:

```text
Covers
Authors
Artists
Descriptions
Genres
Manga status
```

Metadata depends on MangaDex API availability.

---

## Some Metadata Is Missing

Some MangaDex entries may be removed, unavailable, rate-limited, or temporarily unreachable.

The converter may generate:

```text
missing-metadata-links.txt
failed-connection-links.txt
```

These report files help identify metadata issues.

---

## Generated Files Should Not Be Committed

Do not commit personal backup or report files.

Avoid committing:

```text
*.msbf
*.tachibk
*.proto.gz
duplicate-manga-report.txt
missing-metadata-links.txt
failed-connection-links.txt
build/
```

---

## Developer: Build Fails

Run:

```bash
./gradlew clean build
```

If build fails, check the first Kotlin error.

A common issue is accidentally pasting HTML into a Kotlin file or only partially replacing a file.

If `WebRoutes.kt` fails with many errors like unresolved references or `<!doctype html>` appearing in Kotlin errors, replace the entire file again instead of patching small sections.

---

## Developer: Version Still Shows Old Number

Run:

```bash
./gradlew run --args="--version"
```

Expected:

```text
MSBF-to-TachiBK 1.0.0
```

If it still shows an older version, check:

```text
src/main/kotlin/config/Constants.kt
```

---

## Developer: Build Release ZIP

Build the downloadable release ZIP:

```bash
./gradlew clean build
./gradlew distZip
```

The output should appear in:

```text
build/distributions/
```

Expected files:

```text
MSBF-to-TachiBK.zip
MSBF-to-TachiBK.tar
```

---

## Developer: Test the Release ZIP Locally

```bash
rm -rf build/test-release
mkdir -p build/test-release
unzip build/distributions/MSBF-to-TachiBK.zip -d build/test-release
find build/test-release -maxdepth 3 -type f | sort
```

Then test:

```bash
cd build/test-release/MSBF-to-TachiBK
chmod +x run-web-converter.sh
./run-web-converter.sh
```

Open:

```text
http://localhost:8080
```

---

## Developer: GitHub Release Does Not Build

Check:

```text
GitHub repository → Actions → Build Release Package
```

If the release workflow fails because it cannot create a release, check:

```text
Repository Settings → Actions → General → Workflow permissions
```

Use:

```text
Read and write permissions
```

---

## Developer: Delete a Test Release Tag

For a temporary test release tag like `v1.0.0-test`:

```bash
git push origin :refs/tags/v1.0.0-test
git tag -d v1.0.0-test
```

Then delete the test release from GitHub Releases if needed.

---

## Developer: Create the Real V1 Release

```bash
git tag -a v1.0.0 -m "Initial stable MSBF-to-TachiBK release"
git push origin v1.0.0
```

This triggers the GitHub release workflow and uploads the downloadable ZIP.