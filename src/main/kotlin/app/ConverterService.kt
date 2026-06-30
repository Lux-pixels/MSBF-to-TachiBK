package app

import backup.BackupBuilder
import backup.BackupWriter
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
 * Shared conversion service.
 *
 * This is the reusable conversion core used by:
 * - The command-line interface
 * - The future local browser upload/download converter
 *
 * Main.kt should stay small and only handle CLI argument parsing.
 */
object ConverterService {
    /**
     * Run one complete conversion.
     *
     * @param request Conversion settings and file paths.
     * @param logger Output handler. CLI uses println. Future web UI can capture logs.
     */
    fun convert(
        request: ConversionRequest,
        logger: (String) -> Unit = ::println,
    ): ConversionResult {
        /**
         * In duplicate-report-only mode, no backup is written.
         * The output path is not important, but validation still expects a .tachibk file.
         */
        val outputFileForValidation = if (request.reportDuplicatesOnly) {
            File("duplicate-report-only.tachibk")
        } else {
            request.outputFile
        }

        /**
         * Validate input and output files before parsing.
         */
        val fileValidation = ConversionValidator.validateFiles(
            inputFile = request.inputFile,
            outputFile = outputFileForValidation,
        )

        if (!fileValidation.isValid) {
            ConversionValidator.printSummary(
                inputFile = request.inputFile,
                outputFile = outputFileForValidation,
                entries = emptyList(),
                result = fileValidation,
            )

            logger("")
            logger("No backup was written.")

            return ConversionResult(
                success = false,
                backupWritten = false,
                outputFile = null,
                entriesParsed = 0,
                entriesWritten = 0,
                errors = fileValidation.errors,
            )
        }

        /**
         * Parse the Manga Storm .msbf file.
         */
        val entries = runCatching {
            MsbfParser.parse(request.inputFile)
        }.getOrElse { error ->
            val parseValidation = ValidationResult(
                errors = listOf("Could not parse input file: ${error.message ?: error::class.simpleName}")
            )

            ConversionValidator.printSummary(
                inputFile = request.inputFile,
                outputFile = outputFileForValidation,
                entries = emptyList(),
                result = parseValidation,
            )

            logger("")
            logger("No backup was written.")

            return ConversionResult(
                success = false,
                backupWritten = false,
                outputFile = null,
                entriesParsed = 0,
                entriesWritten = 0,
                errors = parseValidation.errors,
            )
        }

        /**
         * Validate parsed manga entries.
         */
        val entryValidation = ConversionValidator.validateEntries(entries)
        val validationResult = fileValidation + entryValidation

        ConversionValidator.printSummary(
            inputFile = request.inputFile,
            outputFile = outputFileForValidation,
            entries = entries,
            result = validationResult,
        )

        if (!validationResult.isValid) {
            logger("")
            logger("No backup was written.")

            return ConversionResult(
                success = false,
                backupWritten = false,
                outputFile = null,
                entriesParsed = entries.size,
                entriesWritten = 0,
                errors = validationResult.errors,
            )
        }

        /**
         * Write duplicate report before optional duplicate removal.
         *
         * Default behavior:
         * - Report duplicates
         * - Keep duplicates
         */
        val duplicateStats = DuplicateHandler.writeDuplicateReport(
            entries = entries,
            logger = logger,
        )

        /**
         * Duplicate report-only mode stops here.
         */
        if (request.reportDuplicatesOnly) {
            logger("Duplicate report-only mode enabled.")
            logger("No backup was written.")

            if (duplicateStats.reportPath != null) {
                logger("")
                logger("Duplicate report:")
                logger(duplicateStats.reportPath)
            }

            return ConversionResult(
                success = true,
                backupWritten = false,
                outputFile = null,
                entriesParsed = entries.size,
                entriesWritten = 0,
            )
        }

        /**
         * Optional duplicate removal.
         *
         * Safe default remains keeping duplicates.
         */
        val conversionEntries = if (request.removeDuplicates) {
            val removalResult = DuplicateHandler.removeDuplicates(entries)

            logger("")
            logger("Duplicate removal:")
            logger("  Original entries: ${entries.size}")
            logger("  Removed duplicate copies: ${removalResult.removedEntries.size}")
            logger("  Entries kept: ${removalResult.entries.size}")

            if (removalResult.removedEntries.isNotEmpty()) {
                logger("")
                logger("Removed duplicate entries:")

                removalResult.removedEntries.forEach { (entryNumber, entry) ->
                    logger("  Entry #$entryNumber")
                    logger("    Title: ${entry.title}")
                    logger("    Status: ${entry.status ?: "unknown"}")
                    logger("    URL: ${entry.url}")
                }
            }

            removalResult.entries
        } else {
            entries
        }

        logger("")
        logger("Manga Backup Status Summary")
        logger("===========================")
        logger("Total Manga Identified: ${entries.size}")
        logger("Manga Included In Backup: ${conversionEntries.size}")
        logger("")

        logger("Sources:")
        conversionEntries.groupBy { it.sourceKey }.forEach { (source, list) ->
            logger("  $source: ${list.size}")
        }

        logger("")
        logger("Manga Storm Categories:")

        val categoryCounts = conversionEntries.groupBy {
            BackupBuilder.mangaStormStatusLabel(it.status)
        }

        listOf("Reading", "Following", "On Hold").forEach { category ->
            logger("  $category: ${categoryCounts[category]?.size ?: 0}")
        }

        val uncategorizedCount = categoryCounts["Uncategorized"]?.size ?: 0
        if (uncategorizedCount > 0) {
            logger("  Uncategorized: $uncategorizedCount")
        }

        logger("")
        logger("Conversion Options:")
        logger("  Metadata fetch: ${if (request.fetchMetadata) "enabled" else "skipped"}")
        logger("  Output file: ${request.outputFile.path}")
        logger("  Duplicate handling: ${if (request.removeDuplicates) "remove duplicates" else "keep duplicates"}")
        logger("  Delegated sources: manually disable in Komikku")

        /**
         * Fetch metadata unless --no-metadata was provided.
         */
        val metadataByUuid = if (request.fetchMetadata) {
            fetchMangaDexMetadata(conversionEntries, logger)
        } else {
            File("missing-metadata-links.txt").delete()
            File("failed-connection-links.txt").delete()
            emptyMap()
        }

        /**
         * Build and write the backup.
         */
        val backup = BackupBuilder.build(conversionEntries, metadataByUuid)

        logger("")

        if (!request.fetchMetadata) {
            logger("Metadata fetch skipped.")
        } else {
            logger("Metadata Applied")
            logger("================")
            logger("Covers: ${backup.backupManga.count { !it.thumbnailUrl.isNullOrBlank() }}/${backup.backupManga.size}")
            logger("Authors: ${backup.backupManga.count { !it.author.isNullOrBlank() }}/${backup.backupManga.size}")
            logger("Artists: ${backup.backupManga.count { !it.artist.isNullOrBlank() }}/${backup.backupManga.size}")
            logger("Descriptions: ${backup.backupManga.count { !it.description.isNullOrBlank() }}/${backup.backupManga.size}")
            logger("Genres: ${backup.backupManga.count { it.genre.isNotEmpty() }}/${backup.backupManga.size}")
            logger("Statuses: ${backup.backupManga.count { it.status != 0 }}/${backup.backupManga.size}")

            logger("")
            logger("Manga Status Summary")
            logger("====================")

            backup.backupManga
                .groupBy { statusLabel(it.status) }
                .toSortedMap()
                .forEach { (status, manga) ->
                    logger("$status: ${manga.size}")
                }
        }

        BackupWriter.write(backup, request.outputFile)

        logger("")
        logger("Backup written to:")
        logger(request.outputFile.absolutePath)

        return ConversionResult(
            success = true,
            backupWritten = true,
            outputFile = request.outputFile,
            entriesParsed = entries.size,
            entriesWritten = conversionEntries.size,
        )
    }

    /**
     * Fetch metadata from MangaDex for every unique MangaDex UUID.
     */
    private fun fetchMangaDexMetadata(
        entries: List<MsbfEntry>,
        logger: (String) -> Unit,
    ): Map<String, MangaDexMetadata> {
        logger("")
        logger("Fetching MangaDex metadata...")

        val metadataByUuid = mutableMapOf<String, MangaDexMetadata>()
        val missingMetadataLinks = linkedMapOf<String, String>()
        val failedConnectionLinks = linkedMapOf<String, String>()

        val checkedUuids = mutableSetOf<String>()
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

                    logger("")
                    logger("Duplicate found:")
                    logger("  $duplicateMessage")
                    logger("")
                }

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

                    Thread.sleep(250)
                }
            }

            if (entryNumber % 25 == 0) {
                logger(
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
}