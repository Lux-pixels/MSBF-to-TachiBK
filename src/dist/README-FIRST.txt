MSBF-to-TachiBK v1.0.0
======================

This is the local web converter for Manga Storm .msbf favorites exports.

What it does
------------
- Runs locally on your computer
- Opens a local browser converter at http://localhost:8080
- Lets you upload your favorites.msbf file
- Converts it into a Komikku / Tachiyomi-style .tachibk backup
- Lets you download the generated .tachibk file
- Nothing is uploaded to the internet

Requirements
------------
Java 17 or newer must be installed.

Windows
-------
1. Unzip the release package.
2. Double-click:

   run-web-converter.bat

3. Open:

   http://localhost:8080

4. Upload your favorites.msbf file.
5. Download the generated .tachibk file.

Windows Smart App Control Note
------------------------------
If Windows Smart App Control blocks run-web-converter.bat, unblock the downloaded ZIP before extracting it:

1. Delete the extracted MSBF-to-TachiBK folder.
2. Right-click MSBF-to-TachiBK-v1.0.0.zip.
3. Click Properties.
4. Check Unblock if you see it.
5. Click Apply.
6. Extract the ZIP again.
7. Double-click run-web-converter.bat again.

This happens because Windows may block unknown downloaded script files after extraction.

macOS / Linux
-------------
1. Unzip the release package.
2. Open a terminal in the unzipped folder.
3. Run:

   chmod +x run-web-converter.sh
   ./run-web-converter.sh

4. Open:

   http://localhost:8080

5. Upload your favorites.msbf file.
6. Download the generated .tachibk file.

Komikku Restore Recommendation
------------------------------
When restoring the generated .tachibk backup in Komikku, check:

- Manga
- Categories

Uncheck:

- App Settings
- Saved Searches
- Source Settings
- Extension Repos

If MangaDex entries open as a WebView / website 404 page, manually disable delegated sources in Komikku.

Included Test File
------------------
This release includes a small sample Manga Storm .msbf file for testing:

samples/testfavorites.msbf

You can use it to confirm the converter opens, runs, and creates a .tachibk file before using your own Manga Storm export.

This sample file is only for testing. Use your own Manga Storm favorites export for your real library migration.