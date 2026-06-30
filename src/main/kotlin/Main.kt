import backup.BackupBuilder
import backup.BackupWriter
import cli.CliParser
import duplicates.DuplicateHandler
import metadata.MangaDexClient
import metadata.MangaDexFetchResult
import metadata.MangaDexMetadata
import parser.MsbfEntry
import parser.MsbfParser
import validation.ConversionValidator
import validation.ValidationResult
import java.io.File

/**
 * CLI entry point for MSBF-to-TachiBK.
 *
 * This program:
 * - Reads a Manga Storm .msbf file
 * - Validates the conversion before writing a backup
 * - Detects duplicate manga
 * - Optionally reports duplicates only
 * - Optionally removes duplicate manga
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
     *
     * For --report-duplicates-only, this output file is not used because no backup
     * is written.
     */
    val outputFile = options.outputFile ?: File("MSBF-to-TachiBK.tachibk")

    /**
     * Validate input/output files before parsing.
     *
     * If this is duplicate-report-only mode, the output file does not matter,
     * so use a safe temporary .tachibk name for validation.
     */
    val outputFileForValidation = if (options.reportDuplicatesOnly) {
        File("duplicate-report-only.tachibk")
    } else {
        outputFile
    }

    val fileValidation = ConversionValidator.validateFiles(
        inputFile = inputFile,
        outputFile = outputFileForValidation,
    )

    if (!fileValidation.isValid) {
        ConversionValidator.printSummary(
            inputFile = inputFile,
            outputFile = outputFileForValidation,
            entries = emptyList(),
            result = fileValidation,
        )

        println()
        println("No backup was written.")
        return
    }

    /**
     * Parse the Manga Storm .msbf file.
     *
     * If parsing fails, show a clean validation-style error instead of a stacktrace.
     */
    val entries = runCatching {
        MsbfParser.parse(inputFile)
    }.getOrElse { error ->
        val parseValidation = ValidationResult(
            errors = listOf("Could not parse input file: ${error.message ?: error::class.simpleName}")
        )

        ConversionValidator.printSummary(
            inputFile = inputFile,
            outputFile = outputFileForValidation,
            entries = emptyList(),
            result = parseValidation,
        )

        println()
        println("No backup was written.")
        return
    }

    /**
     * Validate parsed entries before duplicate handling, metadata fetching,
     * and backup writing.
     */
    val entryValidation = ConversionValidator.validateEntries(entries)
    val validationResult = fileValidation + entryValidation

    ConversionValidator.printSummary(
        inputFile = inputFile,
        outputFile = outputFileForValidation,
        entries = entries,
        result = validationResult,
    )

    if (!validationResult.isValid) {
        println()
        println("No backup was written.")
        return
    }

    /**
     * Write duplicate report before any optional duplicate removal.
     *
     * Default behavior:
     * - Report duplicates
     * - Keep duplicates
     */
    val duplicateStats = DuplicateHandler.writeDuplicateReport(entries)

    /**
     * In report-only mode, stop after creating the duplicate report.
     */
    if (options.reportDuplicatesOnly) {
        println("Duplicate report-only mode enabled.")
        println("No backup was written.")

        if (duplicateStats.reportPath != null) {
            println()
            println("Duplicate report:")
            println(duplicateStats.reportPath)
        }

        return
    }

    /**
     * Optionally remove duplicate entries.
     *
     * Safe default remains keeping duplicates.
     */
    val conversionEntries = if (options.removeDuplicates) {
        val removalResult = DuplicateHandler.removeDuplicates(entries)

        println()
        println("Duplicate removal:")
        println("  Original entries: ${entries.size}")
        println("  Removed duplicate copies: ${removalResult.removedEntries.size}")
        println("  Entries kept: ${removalResult.entries.size}")

        if (removalResult.removedEntries.isNotEmpty()) {
            println()
            println("Removed duplicate entries:")

            removalResult.removedEntries.forEach { (entryNumber, entry) ->
                println("  Entry #$entryNumber")
                println("    Title: ${entry.title}")
                println("    Status: ${entry.status ?: "unknown"}")
                println("    URL: ${entry.url}")
            }
        }

        removalResult.entries
    } else {
        entries
    }

    /**
     * Metadata is enabled by default because Komikku imports work better
     * when MangaDex metadata is included in the backup.
     */
    val fetchMetadata = options.fetchMetadata

    println()
    println("Manga Backup Status Summary")
    println("===========================")
    println("Total Manga Identified: ${entries.size}")
    println("Manga Included In Backup: ${conversionEntries.size}")
    println()

    println("Sources:")
    conversionEntries.groupBy { it.sourceKey }.forEach { (source, list) ->
        println("  $source: ${list.size}")
    }

    println()
    println("Manga Storm Categories:")

    /**
     * Count how many manga will go into each Manga Storm category.
     */
    val categoryCounts = conversionEntries.groupBy {
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
    println("  Duplicate handling: ${if (options.removeDuplicates) "remove duplicates" else "keep duplicates"}")
    println("  Delegated sources: manually disable in Komikku")

    /**
     * Fetch MangaDex metadata unless --no-metadata was provided.
     *
     * If duplicates were removed, metadata is fetched only for entries that
     * remain in the final backup.
     */
    val metadataByUuid = if (fetchMetadata) {
        fetchMangaDexMetadata(conversionEntries)
    } else {
        File("missing-metadata-links.txt").delete()
        File("failed-connection-links.txt").delete()
        emptyMap()
    }

    /**
     * Build the backup object and write it as a .tachibk file.
     */
    val backup = BackupBuilder.build(conversionEntries, metadataByUuid)

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
 * is being fetched. When --remove-duplicates is used, this receives the deduped
 * entry list.
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