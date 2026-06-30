package backup

import metadata.MangaDexMetadata
import models.*
import parser.MsbfEntry

object BackupBuilder {
    private const val READING_CATEGORY_ORDER = 0L
    private const val FOLLOWING_CATEGORY_ORDER = 1L
    private const val ON_HOLD_CATEGORY_ORDER = 2L

    fun build(
        entries: List<MsbfEntry>,
        metadata: Map<String, MangaDexMetadata>,
    ): Backup {
        val manga = entries.map { entry ->
            val uuid = extractMangaDexUuid(entry.url)
            val meta = metadata[uuid]

            BackupManga(
                source = SourceMapper.toKomikkuSourceId(entry.sourceKey),
                url = normalizeMangaDexUrl(entry.url),
                title = meta?.title ?: entry.title,
                artist = meta?.artist,
                author = meta?.author,
                description = meta?.description,
                genre = meta?.genres ?: emptyList(),
                status = meta?.status ?: 0,
                thumbnailUrl = meta?.coverUrl,
                dateAdded = System.currentTimeMillis(),
                favorite = true,
                categories = categoryOrdersForMangaStormStatus(entry.status),
                initialized = false
            )
        }

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
            backupCategories = mangaStormCategories(),
            backupSources = sources
        )
    }

    fun extractMangaDexUuid(url: String): String {
        val regex = Regex("""/title/([0-9a-fA-F-]{36})""")
        val match = regex.find(url)
            ?: error("Invalid MangaDex URL: $url")

        return match.groupValues[1]
    }

    fun mangaStormStatusLabel(status: String?): String {
        return when (status?.trim()?.uppercase()) {
            "R" -> "Reading"
            "Y" -> "Following"
            "A" -> "On Hold"
            else -> "Uncategorized"
        }
    }

    private fun categoryOrdersForMangaStormStatus(status: String?): List<Long> {
        return when (status?.trim()?.uppercase()) {
            "R" -> listOf(READING_CATEGORY_ORDER)
            "Y" -> listOf(FOLLOWING_CATEGORY_ORDER)
            "A" -> listOf(ON_HOLD_CATEGORY_ORDER)
            else -> emptyList()
        }
    }

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

    private fun normalizeMangaDexUrl(url: String): String {
        return "/title/${extractMangaDexUuid(url)}"
    }
}