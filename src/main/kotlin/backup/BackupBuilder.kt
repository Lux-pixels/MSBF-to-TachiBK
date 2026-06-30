package backup

import metadata.MangaDexMetadata
import models.Backup
import models.BackupCategory
import models.BackupManga
import models.BackupSource
import parser.MsbfEntry

/**
 * Builds the final Komikku/Tachiyomi backup object from parsed Manga Storm entries.
 *
 * This class is responsible for:
 * - Mapping Manga Storm entries into BackupManga records
 * - Applying MangaDex metadata when available
 * - Mapping Manga Storm status codes into Komikku categories
 * - Adding source information
 *
 * App Settings are intentionally not included for now.
 * Restoring App Settings was causing delegated sources to become enabled again,
 * so delegated sources should be handled manually in Komikku until we can verify
 * the exact backup preference behavior after V1.
 */
object BackupBuilder {
    /**
     * Komikku expects manga category values to match category order.
     *
     * Reading = 0
     * Following = 1
     * On Hold = 2
     */
    private const val READING_CATEGORY_ORDER = 0L
    private const val FOLLOWING_CATEGORY_ORDER = 1L
    private const val ON_HOLD_CATEGORY_ORDER = 2L

    /**
     * Build a complete Backup object.
     *
     * @param entries Parsed manga entries from the Manga Storm .msbf file.
     * @param metadata MangaDex metadata keyed by MangaDex UUID.
     */
    fun build(
        entries: List<MsbfEntry>,
        metadata: Map<String, MangaDexMetadata>,
    ): Backup {
        val manga = entries.map { entry ->
            val uuid = extractMangaDexUuid(entry.url)
            val meta = metadata[uuid]

            BackupManga(
                // Convert Manga Storm source key into Komikku source ID.
                source = SourceMapper.toKomikkuSourceId(entry.sourceKey),

                // Komikku expects the source-relative MangaDex URL, not the full web URL.
                url = normalizeMangaDexUrl(entry.url),

                // Prefer fetched MangaDex title when available.
                // Fall back to the title from the .msbf file.
                title = meta?.title ?: entry.title,

                // These fields come from MangaDex metadata when metadata fetching is enabled.
                artist = meta?.artist,
                author = meta?.author,
                description = meta?.description,
                genre = meta?.genres ?: emptyList(),
                status = meta?.status ?: 0,
                thumbnailUrl = meta?.coverUrl,

                // Use current conversion time as the date added.
                dateAdded = System.currentTimeMillis(),

                // Manga Storm favorites become Komikku library entries.
                favorite = true,

                // Map Manga Storm status codes into Komikku categories.
                categories = categoryOrdersForMangaStormStatus(entry.status),

                // Keep false so Komikku can still initialize/source-refresh the manga.
                initialized = false
            )
        }

        /**
         * Add one BackupSource entry per unique Manga Storm source key.
         *
         * Right now MangaDex is the only supported source.
         */
        val sources = entries
            .map { it.sourceKey }
            .distinct()
            .map {
                BackupSource(
                    name = SourceMapper.sourceName(it),
                    sourceId = SourceMapper.toKomikkuSourceId(it)
                )
            }

        return Backup(
            backupManga = manga,

            // Add Manga Storm categories to the backup.
            backupCategories = mangaStormCategories(),

            // Add source mapping so Komikku recognizes the manga source.
            backupSources = sources
        )
    }

    /**
     * Extract the MangaDex UUID from a MangaDex title URL.
     *
     * Example:
     * https://mangadex.org/title/657ccfcc-5847-40f7-8859-5631eeeb3784/title-slug
     *
     * Returns:
     * 657ccfcc-5847-40f7-8859-5631eeeb3784
     */
    fun extractMangaDexUuid(url: String): String {
        val regex = Regex("""/title/([0-9a-fA-F-]{36})""")
        val match = regex.find(url)
            ?: error("Invalid MangaDex URL: $url")

        return match.groupValues[1]
    }

    /**
     * Convert Manga Storm status codes into human-readable category names.
     *
     * R = Reading
     * Y = Following
     * A = On Hold
     */
    fun mangaStormStatusLabel(status: String?): String {
        return when (status?.trim()?.uppercase()) {
            "R" -> "Reading"
            "Y" -> "Following"
            "A" -> "On Hold"
            else -> "Uncategorized"
        }
    }

    /**
     * Convert Manga Storm status codes into Komikku category order values.
     *
     * Unknown statuses are left uncategorized.
     */
    private fun categoryOrdersForMangaStormStatus(status: String?): List<Long> {
        return when (status?.trim()?.uppercase()) {
            "R" -> listOf(READING_CATEGORY_ORDER)
            "Y" -> listOf(FOLLOWING_CATEGORY_ORDER)
            "A" -> listOf(ON_HOLD_CATEGORY_ORDER)
            else -> emptyList()
        }
    }

    /**
     * Create the categories that will appear in Komikku after restore.
     */
    private fun mangaStormCategories(): List<BackupCategory> {
        return listOf(
            BackupCategory(
                name = "Reading",
                order = READING_CATEGORY_ORDER,
                id = READING_CATEGORY_ORDER
            ),
            BackupCategory(
                name = "Following",
                order = FOLLOWING_CATEGORY_ORDER,
                id = FOLLOWING_CATEGORY_ORDER
            ),
            BackupCategory(
                name = "On Hold",
                order = ON_HOLD_CATEGORY_ORDER,
                id = ON_HOLD_CATEGORY_ORDER
            ),
        )
    }

    /**
     * Convert a full MangaDex web URL into the relative URL expected by the MangaDex source.
     *
     * Full URL:
     * https://mangadex.org/title/<uuid>/<slug>
     *
     * Source-relative URL:
     * /title/<uuid>
     */
    private fun normalizeMangaDexUrl(url: String): String {
        return "/title/${extractMangaDexUuid(url)}"
    }
}