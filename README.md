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