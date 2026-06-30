package config

/**
 * Shared project constants.
 *
 * Keep app-level names, versions, and source mappings here so they are easy
 * to update as the project moves toward V1.
 */
object Constants {
    const val APP_NAME = "MSBF-to-TachiBK"

    /**
     * Commit 9 development version.
     *
     * v0.7.0 = pre-conversion validation.
     */
    const val VERSION = "0.7.0"

    /**
     * Manga Storm source key for MangaDex.
     */
    const val MANGADEX_SOURCE_KEY = "z13mangadex"

    /**
     * Display name used in the generated backup source list.
     */
    const val MANGADEX_SOURCE_NAME = "MangaDex"

    /**
     * Komikku / Tachiyomi MangaDex source ID.
     */
    const val MANGADEX_SOURCE_ID = 2499283573021220255L
}