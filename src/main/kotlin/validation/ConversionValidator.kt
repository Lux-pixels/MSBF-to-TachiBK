package validation

import backup.BackupBuilder
import config.Constants
import parser.MsbfEntry
import java.io.File

/**
 * Validates the conversion before a .tachibk backup is written.
 *
 * This gives users clear messages instead of letting the converter crash later
 * with a lower-level Kotlin or serialization error.
 */
object ConversionValidator {
    /**
     * Validate input and output files before parsing.
     *
     * This checks:
     * - Input file exists
     * - Input file is readable
     * - Input file is not empty
     * - Input file ends with .msbf
     * - Output file ends with .tachibk
     * - Output folder exists or can be created
     */
    fun validateFiles(
        inputFile: File,
        outputFile: File,
    ): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (!inputFile.exists()) {
            errors += "Input file does not exist: ${inputFile.path}"
        } else {
            if (!inputFile.isFile) {
                errors += "Input path is not a file: ${inputFile.path}"
            }

            if (!inputFile.canRead()) {
                errors += "Input file is not readable: ${inputFile.path}"
            }

            if (inputFile.length() == 0L) {
                errors += "Input file is empty: ${inputFile.path}"
            }

            if (!inputFile.name.endsWith(".msbf", ignoreCase = true)) {
                errors += "Input file must end with .msbf: ${inputFile.name}"
            }
        }

        if (!outputFile.name.endsWith(".tachibk", ignoreCase = true)) {
            errors += "Output file must end with .tachibk: ${outputFile.name}"
        }

        /**
         * Create the output folder automatically when possible.
         *
         * Example:
         * testdata/v0.7/output.tachibk
         */
        outputFile.parentFile?.let { parent ->
            if (parent.exists()) {
                if (!parent.isDirectory) {
                    errors += "Output parent path exists but is not a folder: ${parent.path}"
                } else if (!parent.canWrite()) {
                    warnings += "Output folder may not be writable: ${parent.path}"
                }
            } else {
                val created = parent.mkdirs()

                if (!created && !parent.exists()) {
                    errors += "Could not create output folder: ${parent.path}"
                }
            }
        }

        return ValidationResult(
            errors = errors,
            warnings = warnings,
        )
    }

    /**
     * Validate parsed Manga Storm entries before metadata fetching and backup writing.
     *
     * This checks:
     * - Parsed backup is not empty
     * - Sources are supported
     * - MangaDex URLs are valid
     * - Titles are present when possible
     */
    fun validateEntries(entries: List<MsbfEntry>): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        if (entries.isEmpty()) {
            errors += "No manga entries were found in the input file."
            return ValidationResult(errors = errors, warnings = warnings)
        }

        val unsupportedSources = entries
            .groupBy { it.sourceKey.lowercase() }
            .filterKeys { it != Constants.MANGADEX_SOURCE_KEY }

        unsupportedSources.forEach { (sourceKey, sourceEntries) ->
            errors += "Unsupported source: $sourceKey (${sourceEntries.size} entries)"
        }

        val invalidMangaDexUrls = mutableListOf<String>()

        entries.forEachIndexed { index, entry ->
            val entryNumber = index + 1

            if (entry.title.isBlank()) {
                warnings += "Entry #$entryNumber has a blank title."
            }

            if (entry.sourceKey.lowercase() == Constants.MANGADEX_SOURCE_KEY) {
                val uuidResult = runCatching {
                    BackupBuilder.extractMangaDexUuid(entry.url)
                }

                if (uuidResult.isFailure) {
                    invalidMangaDexUrls += "Entry #$entryNumber has an invalid MangaDex URL: ${entry.url}"
                }
            }
        }

        /**
         * Limit printed invalid URL errors so a broken file does not flood the terminal.
         */
        invalidMangaDexUrls.take(10).forEach { message ->
            errors += message
        }

        if (invalidMangaDexUrls.size > 10) {
            errors += "Additional invalid MangaDex URLs not shown: ${invalidMangaDexUrls.size - 10}"
        }

        return ValidationResult(
            errors = errors,
            warnings = warnings,
        )
    }

    /**
     * Print a clear validation summary before conversion continues.
     */
    fun printSummary(
        inputFile: File,
        outputFile: File,
        entries: List<MsbfEntry>,
        result: ValidationResult,
    ) {
        val sourceCounts = entries.groupBy { it.sourceKey }
        val supportedSourceCount = sourceCounts
            .filterKeys { it.lowercase() == Constants.MANGADEX_SOURCE_KEY }
            .values
            .sumOf { it.size }

        val unsupportedSourceCount = sourceCounts
            .filterKeys { it.lowercase() != Constants.MANGADEX_SOURCE_KEY }
            .values
            .sumOf { it.size }

        val invalidMangaDexUrlCount = entries.count { entry ->
            entry.sourceKey.lowercase() == Constants.MANGADEX_SOURCE_KEY &&
                runCatching { BackupBuilder.extractMangaDexUuid(entry.url) }.isFailure
        }

        println()
        println("Validation Summary")
        println("==================")
        println("Input file: ${inputFile.path}")
        println("Output file: ${outputFile.path}")
        println("Entries parsed: ${entries.size}")
        println("Supported source entries: $supportedSourceCount")
        println("Unsupported source entries: $unsupportedSourceCount")
        println("Invalid MangaDex URLs: $invalidMangaDexUrlCount")
        println("Warnings: ${result.warnings.size}")
        println("Errors: ${result.errors.size}")

        if (result.warnings.isNotEmpty()) {
            println()
            println("Validation warnings:")
            result.warnings.forEach { warning ->
                println("  - $warning")
            }
        }

        if (result.errors.isNotEmpty()) {
            println()
            println("Validation failed:")
            result.errors.forEach { error ->
                println("  - $error")
            }
        } else {
            println()
            println("Validation passed.")
        }
    }
}