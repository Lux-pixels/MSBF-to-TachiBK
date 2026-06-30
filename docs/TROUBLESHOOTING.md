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

## Backup File Not Found in Komikku

If Komikku cannot open the `.tachibk` file, especially in BlueStacks:

1. Copy the `.tachibk` file into Android’s **Downloads** folder.
2. Restore from Downloads inside Komikku.
3. Avoid restoring directly from the BlueStacks shared folder.

---

## MangaDex Opens a WebView / 404 Page

If restored MangaDex entries open a website/WebView 404 page:

1. Make sure the MangaDex extension is installed.
2. Restore **App Settings** from the generated backup.
3. Confirm delegated sources are disabled.
4. Reopen Komikku and test the manga again.

The converter includes this setting in the backup:

```text
enable_delegated_sources = false
```

---

## Manga Entries Restore but Metadata Is Missing

Metadata is fetched by default.

Run without `--no-metadata`:

```bash
./gradlew run --args="samples/testfavorites.msbf testdata/v0.4/MSBF-to-TachiBK-v0.4test.tachibk"
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
App Settings

Uncheck:
Saved Searches
Source Settings
Extension Repos
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