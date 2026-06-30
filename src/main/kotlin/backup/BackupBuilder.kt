package backup

import models.*
import parser.MsbfEntry

object BackupBuilder {
    fun build(entries: List<MsbfEntry>): Backup {
        val categoryId = 0L

        val manga = entries.map { entry ->
            BackupManga(
                source = SourceMapper.toKomikkuSourceId(entry.sourceKey),
                url = entry.url,
                title = entry.title,
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
}