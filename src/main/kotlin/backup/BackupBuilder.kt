package backup

import metadata.MangaDexMetadata
import models.*
import parser.MsbfEntry

object BackupBuilder {
    fun build(
        entries: List<MsbfEntry>,
        metadata: Map<String, MangaDexMetadata>,
    ): Backup {
        val categoryId = 0L

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
                categories = listOf(categoryId),
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
            backupCategories = listOf(
                BackupCategory(
                    name = "Manga Storm",
                    order = 0,
                    id = categoryId
                )
            ),
            backupSources = sources
        )
    }

    fun extractMangaDexUuid(url: String): String {
        val regex = Regex("""/title/([0-9a-fA-F-]{36})""")
        val match = regex.find(url)
            ?: error("Invalid MangaDex URL: $url")

        return match.groupValues[1]
    }

    private fun normalizeMangaDexUrl(url: String): String {
        return "/title/${extractMangaDexUuid(url)}"
    }
}