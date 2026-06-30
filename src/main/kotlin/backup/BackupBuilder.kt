package backup

import metadata.MangaDexMetadata
import models.*
import parser.MsbfEntry

object BackupBuilder {
    private const val READING_CATEGORY_ID = 1L
    private const val FOLLOWING_CATEGORY_ID = 2L
    private const val ON_HOLD_CATEGORY_ID = 3L

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
                categories = categoryIdsForMangaStormStatus(entry.status),
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

    private fun categoryIdsForMangaStormStatus(status: String?): List<Long> {
        return when (status?.trim()?.uppercase()) {
            "R" -> listOf(READING_CATEGORY_ID)
            "Y" -> listOf(FOLLOWING_CATEGORY_ID)
            "A" -> listOf(ON_HOLD_CATEGORY_ID)
            else -> emptyList()
        }
    }

    private fun mangaStormCategories(): List<BackupCategory> {
        return listOf(
            BackupCategory(
                name = "Reading",
                order = 0,
                id = READING_CATEGORY_ID
            ),
            BackupCategory(
                name = "Following",
                order = 1,
                id = FOLLOWING_CATEGORY_ID
            ),
            BackupCategory(
                name = "On Hold",
                order = 2,
                id = ON_HOLD_CATEGORY_ID
            ),
        )
    }

    private fun normalizeMangaDexUrl(url: String): String {
        return "/title/${extractMangaDexUuid(url)}"
    }
}