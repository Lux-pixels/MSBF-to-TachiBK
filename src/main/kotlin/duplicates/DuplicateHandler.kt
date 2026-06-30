package duplicates

import backup.BackupBuilder
import parser.MsbfEntry
import java.io.File

/**
 * Summary of duplicate manga found in the Manga Storm backup.
 */
data class DuplicateStats(
    val duplicateGroups: Int,
    val duplicateEntries: Int,
    val extraDuplicateCopies: Int,
    val reportPath: String?,
)

/**
 * Result of removing duplicate manga entries.
 */
data class DuplicateRemovalResult(
    val entries: List<MsbfEntry>,
    val removedEntries: List<Pair<Int, MsbfEntry>>,
)

/**
 * Handles duplicate MangaDex entries.
 *
 * Default behavior is safe:
 * - Detect duplicates
 * - Report duplicates
 * - Keep all entries unless --remove-duplicates is used
 */
object DuplicateHandler {
    /**
     * Group duplicate manga by MangaDex UUID.
     */
    fun findDuplicateGroups(entries: List<MsbfEntry>): Map<String, List<Pair<Int, MsbfEntry>>> {
        return entries
            .mapIndexed { index, entry -> index + 1 to entry }
            .groupBy { (_, entry) ->
                runCatching {
                    BackupBuilder.extractMangaDexUuid(entry.url)
                }.getOrElse {
                    entry.url
                }
            }
            .filterValues { it.size > 1 }
    }

    /**
     * Write duplicate-manga-report.txt and optionally print duplicate details.
     */
    fun writeDuplicateReport(
        entries: List<MsbfEntry>,
        reportFile: File = File("duplicate-manga-report.txt"),
        printToConsole: Boolean = true,
        logger: (String) -> Unit = ::println,
    ): DuplicateStats {
        val duplicateGroups = findDuplicateGroups(entries)

        if (duplicateGroups.isEmpty()) {
            reportFile.delete()

            if (printToConsole) {
                logger("")
                logger("Duplicate check:")
                logger("  Extra duplicate copies: 0")
                logger("")
            }

            return DuplicateStats(
                duplicateGroups = 0,
                duplicateEntries = 0,
                extraDuplicateCopies = 0,
                reportPath = null,
            )
        }

        val duplicateEntryCount = duplicateGroups.values.sumOf { it.size }
        val extraDuplicateCount = duplicateGroups.values.sumOf { it.size - 1 }

        reportFile.writeText(
            buildString {
                appendLine("MSBF-to-TachiBK Duplicate Manga Report")
                appendLine("=====================================")
                appendLine()
                appendLine("Duplicate groups: ${duplicateGroups.size}")
                appendLine("Duplicate entries: $duplicateEntryCount")
                appendLine("Extra duplicate copies: $extraDuplicateCount")
                appendLine()

                duplicateGroups.forEach { (uuid, group) ->
                    appendLine("UUID: $uuid")

                    group.forEach { (entryNumber, entry) ->
                        appendLine("  Entry #$entryNumber")
                        appendLine("     Title: ${entry.title}")
                        appendLine("     Source: ${entry.sourceKey}")
                        appendLine("     Status: ${entry.status ?: "unknown"}")
                        appendLine("     URL: ${entry.url}")
                    }

                    appendLine()
                }
            }
        )

        if (printToConsole) {
            logger("")
            logger("Duplicate check:")
            logger("  Duplicate manga groups: ${duplicateGroups.size}")
            logger("  Duplicate entries: $duplicateEntryCount")
            logger("  Extra duplicate copies: $extraDuplicateCount")
            logger("")

            logger("Duplicate entries found:")

            duplicateGroups.forEach { (uuid, group) ->
                logger("")
                logger("UUID: $uuid")

                group.forEach { (entryNumber, entry) ->
                    logger("  Entry #$entryNumber")
                    logger("    Title: ${entry.title}")
                    logger("    Status: ${entry.status ?: "unknown"}")
                    logger("    URL: ${entry.url}")
                }
            }

            logger("")
            logger("Full duplicate report written to:")
            logger("  ${reportFile.absolutePath}")
            logger("")
        }

        return DuplicateStats(
            duplicateGroups = duplicateGroups.size,
            duplicateEntries = duplicateEntryCount,
            extraDuplicateCopies = extraDuplicateCount,
            reportPath = reportFile.absolutePath,
        )
    }

    /**
     * Remove duplicate MangaDex entries.
     *
     * The first copy of each MangaDex UUID is kept.
     * Later copies are removed.
     */
    fun removeDuplicates(entries: List<MsbfEntry>): DuplicateRemovalResult {
        val seenKeys = mutableSetOf<String>()
        val keptEntries = mutableListOf<MsbfEntry>()
        val removedEntries = mutableListOf<Pair<Int, MsbfEntry>>()

        entries.forEachIndexed { index, entry ->
            val entryNumber = index + 1

            val key = runCatching {
                BackupBuilder.extractMangaDexUuid(entry.url)
            }.getOrElse {
                entry.url
            }

            if (seenKeys.add(key)) {
                keptEntries += entry
            } else {
                removedEntries += entryNumber to entry
            }
        }

        return DuplicateRemovalResult(
            entries = keptEntries,
            removedEntries = removedEntries,
        )
    }
}