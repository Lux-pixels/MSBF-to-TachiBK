# Troubleshooting

Common issues and fixes for **MSBF-to-TachiBK**.

---

## Java Version Error

If the build fails with an error involving a Java version like:

```text
25.0.2
```

Use Java 17.

Check your version:

```bash
java -version
```

Expected:

```text
openjdk version "17.x"
```

In GitHub Codespaces, you may need to switch to Java 17 before building.

---

## Help Command

To show CLI help:

```bash
./gradlew run --args="--help"
```

---

## Version Command

To show the current converter version:

```bash
./gradlew run --args="--version"
```

Expected during Commit 8:

```text
MSBF-to-TachiBK 0.6.0
```

---

## Backup File Not Found in Komikku

If Komikku cannot open the `.tachibk` file, especially in BlueStacks:

1. Copy the `.tachibk` file into Android’s **Downloads** folder.
2. Restore from Downloads inside Komikku.
3. Avoid restoring directly from the BlueStacks shared folder.

---

## MangaDex Opens a WebView / 404 Page

If restored MangaDex entries open a website/WebView 404 page:

1. Make sure the MangaDex extension is installed.
2. Do **not** restore App Settings.
3. Manually disable delegated sources in Komikku.
4. Reopen Komikku and test the manga again.

Look for:

```text
Komikku settings → Advanced / Developer tools → Enable delegated sources
```

Make sure delegated sources are disabled.

---

## Manga Entries Restore but Metadata Is Missing

Metadata is fetched by default.

Run without `--no-metadata`:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.6/MSBF-to-TachiBK-v0.6test.tachibk"
```

Do not use this unless doing a quick test:

```bash
--no-metadata
```

---

## Categories Do Not Restore

When restoring, make sure **Categories** is checked.

Recommended restore options:

```text
Check:
Manga
Categories

Uncheck:
App Settings
Saved Searches
Source Settings
Extension Repos
```

---

## Output Folder Does Not Exist

Commit 8 creates output folders automatically when possible.

This should work:

```bash
./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.6/test.tachibk"
```

If folder creation fails, create it manually:

```bash
mkdir -p testdata/v0.6
```

---

## Duplicate Manga

The converter does not remove duplicates automatically.

If duplicates are found, a report is created:

```text
duplicate-manga-report.txt
```

This report lists duplicate MangaDex UUIDs and the entries that share them.

---

## Missing Metadata

If MangaDex metadata cannot be found, a report may be created:

```text
missing-metadata-links.txt
```

This usually means:

```text
The MangaDex title no longer exists
The URL is invalid
The title was removed
The MangaDex UUID could not be parsed
```

---

## MangaDex Connection Failures

If MangaDex cannot be reached during metadata fetching, a report may be created:

```text
failed-connection-links.txt
```

Try running the converter again later.

---

## Unsupported Source

Currently supported source:

```text
MangaDex
```

If your Manga Storm backup includes another source, it may not convert yet.

Future versions may add more source mappings.