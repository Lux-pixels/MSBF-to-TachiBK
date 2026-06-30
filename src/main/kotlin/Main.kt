import backup.BackupBuilder
import backup.BackupWriter
import cli.CliParser
import metadata.MangaDexClient
import metadata.MangaDexFetchResult
import metadata.MangaDexMetadata
import parser.MsbfEntry
import parser.MsbfParser
import java.io.File

/**
 * CLI entry point for MSBF-to-TachiBK.
 *
 * This program:
 * - Reads a Manga Storm .msbf file
 * - Detects duplicate manga
 * - Fetches MangaDex metadata by default
 * - Builds a Komikku/Tachiyomi .tachibk backup
 */
fun main(args: Array<String>) {
    val options = CliParser.parse(args)

    /**
     * Print help and exit.
     */
    if (options.showHelp) {
        println(CliParser.usage())
        return
    }

    /**
     * Print version and exit.
     */
    if (options.showVersion) {
        println(CliParser.versionText())
        return
    }

    /**
     * Stop early if CLI parsing found problems.
     */
    if (options.errors.isNotEmpty()) {
        println("Command error:")
        options.errors.forEach { error ->
            println("  - $error")
        }

        println()
        println(CliParser.usage())
        return
    }

    val inputFile = options.inputFile ?: run {
        println("Error: Missing input .msbf file")
        return
    }

    /**
     * If no output file is provided, write to a default file in the current folder.
     */
    val outputFile = options.outputFile ?: File("MSBF-to-TachiBK.tachibk")

    if (!inputFile.exists()) {
        println("Error: File not found: ${inputFile.absolutePath}")
        return
    }

    /**
     * Create the output folder automatically.
     *
     * Example:
     * testdata/v0.6/output.tachibk
     *
     * If testdata/v0.6 does not exist, it will be created.
     */
    outputFile.parentFile?.let { parent ->
        if (!parent.exists()) {
            val created = parent.mkdirs()

            if (!created) {
                println("Error: Could not create output folder: ${parent.absolutePath}")
                return
            }
        }
    }

    /**
     * Metadata is enabled by default because Komikku imports work better
     * when MangaDex metadata is included in the backup.
     */
    val fetchMetadata = options.fetchMetadata

    val entries = MsbfParser.parse(inputFile)

    println()
    println("Manga Backup Status Summary")
    println("===========================")
    println("Total Manga Identified: ${entries.size}")
    println()

    println("Sources:")
    entries.groupBy { it.sourceKey }.forEach { (source, list) ->
        println("  $source: ${list.size}")
    }

    println()
    println("Manga Storm Categories:")

    /**
     * Count how many manga will go into each Manga Storm category.
     */
    val categoryCounts = entries.groupBy {
        BackupBuilder.mangaStormStatusLabel(it.status)
    }

    listOf("Reading", "Following", "On Hold").forEach { category ->
        println("  $category: ${categoryCounts[category]?.size ?: 0}")
    }

    val uncategorizedCount = categoryCounts["Uncategorized"]?.size ?: 0
    if (uncategorizedCount > 0) {
        println("  Uncategorized: $uncategorizedCount")
    }

    println()
    println("Conversion Options:")
    println("  Metadata fetch: ${if (fetchMetadata) "enabled" else "skipped"}")
    println("  Output file: ${outputFile.path}")
    println("  Delegated sources: manually disable in Komikku")

    /**
     * Duplicate detection does not require metadata.
     * It uses the MangaDex UUID from the .msbf URL.
     */
    writeDuplicateReport(entries)

    /**
     * Fetch MangaDex metadata unless --no-metadata was provided.
     */
    val metadataByUuid = if (fetchMetadata) {
        fetchMangaDexMetadata(entries)
    } else {
        File("missing-metadata-links.txt").delete()
        File("failed-connection-links.txt").delete()
        emptyMap()
    }

    /**
     * Build the backup object and write it as a .tachibk file.
     */
    val backup = BackupBuilder.build(entries, metadataByUuid)

    println()

    if (!fetchMetadata) {
        println("Metadata fetch skipped.")
    } else {
        println("Metadata Applied")
        println("================")
        println("Covers: ${backup.backupManga.count { !it.thumbnailUrl.isNullOrBlank() }}/${backup.backupManga.size}")
        println("Authors: ${backup.backupManga.count { !it.author.isNullOrBlank() }}/${backup.backupManga.size}")
        println("Artists: ${backup.backupManga.count { !it.artist.isNullOrBlank() }}/${backup.backupManga.size}")
        println("Descriptions: ${backup.backupManga.count { !it.description.isNullOrBlank() }}/${backup.backupManga.size}")
        println("Genres: ${backup.backupManga.count { it.genre.isNotEmpty() }}/${backup.backupManga.size}")
        println("Statuses: ${backup.backupManga.count { it.status != 0 }}/${backup.backupManga.size}")

        println()
        println("Manga Status Summary")
        println("====================")

        backup.backupManga
            .groupBy { statusLabel(it.status) }
            .toSortedMap()
            .forEach { (status, manga) ->
                println("$status: ${manga.size}")
            }
    }

    BackupWriter.write(backup, outputFile)

    println()
    println("Backup written to:")
    println(outputFile.absolutePath)
}

/**
 * Fetch metadata from the MangaDex API for every unique MangaDex UUID.
 *
 * Duplicate manga are detected here too so the user can see them while metadata
 * is being fetched.
 */
private fun fetchMangaDexMetadata(entries: List<MsbfEntry>): Map<String, MangaDexMetadata> {
    println()
    println("Fetching MangaDex metadata...")

    val metadataByUuid = mutableMapOf<String, MangaDexMetadata>()
    val missingMetadataLinks = linkedMapOf<String, String>()
    val failedConnectionLinks = linkedMapOf<String, String>()

    /**
     * Prevent duplicate API calls.
     * If the same UUID appears twice, only fetch metadata once.
     */
    val checkedUuids = mutableSetOf<String>()

    /**
     * Used to print duplicate notices during metadata fetching.
     */
    val firstSeenByUuid = linkedMapOf<String, Pair<Int, MsbfEntry>>()
    val duplicatesFoundDuringFetch = mutableListOf<String>()

    entries.forEachIndexed { index, entry ->
        val entryNumber = index + 1

        val uuid = runCatching {
            BackupBuilder.extractMangaDexUuid(entry.url)
        }.getOrElse {
            missingMetadataLinks[entry.url] = "Invalid MangaDex URL"
            null
        }

        if (uuid != null) {
            val firstSeen = firstSeenByUuid[uuid]

            if (firstSeen == null) {
                firstSeenByUuid[uuid] = entryNumber to entry
            } else {
                val (originalEntryNumber, originalEntry) = firstSeen

                val duplicateMessage =
                    "Entry #$entryNumber duplicates Entry #$originalEntryNumber | UUID: $uuid | \"${entry.title}\" duplicates \"${originalEntry.title}\""

                duplicatesFoundDuringFetch += duplicateMessage

                println()
                println("Duplicate found:")
                println("  $duplicateMessage")
                println()
            }

            /**
             * Fetch metadata only once per unique MangaDex UUID.
             */
            if (checkedUuids.add(uuid)) {
                when (val result = MangaDexClient.fetchDetailed(uuid)) {
                    is MangaDexFetchResult.Success -> {
                        metadataByUuid[uuid] = result.metadata
                    }

                    is MangaDexFetchResult.NotFound -> {
                        missingMetadataLinks[entry.url] = result.reason
                    }

                    is MangaDexFetchResult.ConnectionFailed -> {
                        failedConnectionLinks[entry.url] = result.reason
                    }
                }

                /**
                 * Small delay to avoid hitting MangaDex too aggressively.
                 */
                Thread.sleep(250)
            }
        }

        /**
         * Print progress every 25 entries.
         */
        if (entryNumber % 25 == 0) {
            println(
                "  Checked $entryNumber/${entries.size} entries, " +
                    "metadata found: ${metadataByUuid.size}, " +
                    "missing: ${missingMetadataLinks.size}, " +
                    "connection failures: ${failedConnectionLinks.size}, " +
                    "duplicates found: ${duplicatesFoundDuringFetch.size}"
            )
        }
    }

    writeLinkReport(
        fileName = "missing-metadata-links.txt",
        title = "MSBF-to-TachiBK Missing Metadata Report",
        rows = missingMetadataLinks,
    )

    writeLinkReport(
        fileName = "failed-connection-links.txt",
        title = "MSBF-to-TachiBK Failed Connection Report",
        rows = failedConnectionLinks,
    )

    return metadataByUuid
}

/**
 * Summary data returned by duplicate detection.
 */
private data class DuplicateStats(
    val duplicateGroups: Int,
    val duplicateEntries: Int,
    val extraDuplicateCopies: Int,
    val reportPath: String?,
)

/**
 * Find duplicate MangaDex entries by UUID and write a duplicate report file.
 *
 * Duplicate detection works even when metadata fetching is skipped.
 */
private fun writeDuplicateReport(entries: List<MsbfEntry>): DuplicateStats {
    val duplicateGroups = entries
        .mapIndexed { index, entry -> index + 1 to entry }
        .groupBy { (_, entry) ->
            runCatching {
                BackupBuilder.extractMangaDexUuid(entry.url)
            }.getOrElse {
                entry.url
            }
        }
        .filterValues { it.size > 1 }

    val reportFile = File("duplicate-manga-report.txt")

    if (duplicateGroups.isEmpty()) {
        reportFile.delete()

        println()
        println("Duplicate check:")
        println("  Extra duplicate copies: 0")
        println()

        return DuplicateStats(
            duplicateGroups = 0,
            duplicateEntries = 0,
            extraDuplicateCopies = 0,
            reportPath = null,
        )
    }

    val duplicateEntryCount = duplicateGroups.values.sumOf { it.size }
    val extraDuplicateCount = duplicateGroups.values.sumOf { it.size - 1 }

    /**
     * Write the full duplicate report to a text file.
     */
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

    println()
    println("Duplicate check:")
    println("  Duplicate manga groups: ${duplicateGroups.size}")
    println("  Duplicate entries: $duplicateEntryCount")
    println("  Extra duplicate copies: $extraDuplicateCount")
    println()

    println("Duplicate entries found:")

    duplicateGroups.forEach { (uuid, group) ->
        println()
        println("UUID: $uuid")

        group.forEach { (entryNumber, entry) ->
            println("  Entry #$entryNumber")
            println("    Title: ${entry.title}")
            println("    Status: ${entry.status ?: "unknown"}")
            println("    URL: ${entry.url}")
        }
    }

    println()
    println("Full duplicate report written to:")
    println("  ${reportFile.absolutePath}")
    println()

    return DuplicateStats(
        duplicateGroups = duplicateGroups.size,
        duplicateEntries = duplicateEntryCount,
        extraDuplicateCopies = extraDuplicateCount,
        reportPath = reportFile.absolutePath,
    )
}

/**
 * Write a URL report file for missing metadata or connection failures.
 */
private fun writeLinkReport(
    fileName: String,
    title: String,
    rows: Map<String, String>,
) {
    val reportFile = File(fileName)

    if (rows.isEmpty()) {
        reportFile.delete()
        return
    }

    reportFile.writeText(
        buildString {
            appendLine(title)
            appendLine("=".repeat(title.length))
            appendLine()

            rows.forEach { (url, reason) ->
                appendLine("Reason: $reason")
                appendLine("URL: $url")
                appendLine()
            }
        }
    )
}

/**
 * Convert Komikku/Tachiyomi numeric manga status values into readable labels.
 */
private fun statusLabel(status: Int): String {
    return when (status) {
        1 -> "Ongoing"
        2 -> "Completed"
        3 -> "Cancelled"
        4 -> "Hiatus"
        else -> "Unknown"
    }
}