package cli

import config.Constants
import java.io.File

/**
 * Parsed command-line options for one converter run.
 */
data class CliOptions(
    val inputFile: File?,
    val outputFile: File?,
    val fetchMetadata: Boolean,
    val showHelp: Boolean,
    val showVersion: Boolean,

    /**
     * If true, only generate duplicate-manga-report.txt and stop.
     * No backup will be written.
     */
    val reportDuplicatesOnly: Boolean,

    /**
     * If true, keep the first copy of each MangaDex UUID and remove later copies.
     */
    val removeDuplicates: Boolean,

    val errors: List<String>,
)

/**
 * Small command-line parser for MSBF-to-TachiBK.
 */
object CliParser {
    fun parse(args: Array<String>): CliOptions {
        if (args.isEmpty()) {
            return CliOptions(
                inputFile = null,
                outputFile = null,
                fetchMetadata = true,
                showHelp = true,
                showVersion = false,
                reportDuplicatesOnly = false,
                removeDuplicates = false,
                errors = emptyList(),
            )
        }

        val errors = mutableListOf<String>()
        val positionalArgs = mutableListOf<String>()

        var outputFile: File? = null
        var fetchMetadata = true
        var showHelp = false
        var showVersion = false
        var reportDuplicatesOnly = false
        var removeDuplicates = false

        var index = 0

        /**
         * Allow the cleaner command format:
         *
         * convert input.msbf --output output.tachibk
         *
         * The word "convert" is optional so older commands still work.
         */
        if (args.firstOrNull()?.lowercase() == "convert") {
            index++
        }

        while (index < args.size) {
            val arg = args[index]

            when (arg) {
                "--help", "-h" -> {
                    showHelp = true
                }

                "--version", "-v" -> {
                    showVersion = true
                }

                "--metadata" -> {
                    fetchMetadata = true
                }

                "--no-metadata" -> {
                    fetchMetadata = false
                }

                "--report-duplicates-only" -> {
                    reportDuplicatesOnly = true
                }

                "--remove-duplicates" -> {
                    removeDuplicates = true
                }

                "--output", "-o" -> {
                    val nextValue = args.getOrNull(index + 1)

                    if (nextValue == null || nextValue.startsWith("-")) {
                        errors += "Missing value after $arg"
                    } else {
                        outputFile = File(nextValue)
                        index++
                    }
                }

                else -> {
                    if (arg.startsWith("-")) {
                        errors += "Unknown option: $arg"
                    } else {
                        positionalArgs += arg
                    }
                }
            }

            index++
        }

        if (reportDuplicatesOnly && removeDuplicates) {
            errors += "Use either --report-duplicates-only or --remove-duplicates, not both."
        }

        val inputFile = positionalArgs.getOrNull(0)?.let { File(it) }

        if (outputFile == null) {
            outputFile = positionalArgs.getOrNull(1)?.let { File(it) }
        }

        if (positionalArgs.size > 2) {
            errors += "Too many positional arguments: ${positionalArgs.drop(2).joinToString(" ")}"
        }

        if (!showHelp && !showVersion && inputFile == null) {
            errors += "Missing input .msbf file"
        }

        return CliOptions(
            inputFile = inputFile,
            outputFile = outputFile,
            fetchMetadata = fetchMetadata,
            showHelp = showHelp,
            showVersion = showVersion,
            reportDuplicatesOnly = reportDuplicatesOnly,
            removeDuplicates = removeDuplicates,
            errors = errors,
        )
    }

    /**
     * Version text printed by --version.
     */
    fun versionText(): String {
        return "${Constants.APP_NAME} ${Constants.VERSION}"
    }

    /**
     * Help text printed by --help or when no arguments are provided.
     */
    fun usage(): String {
        return """
            ${Constants.APP_NAME} ${Constants.VERSION}

            Convert Manga Storm .msbf favorites exports into Komikku/Tachiyomi .tachibk backups.

            Usage:
              ./gradlew run --args="convert <input.msbf> --output <output.tachibk>"
              ./gradlew run --args="<input.msbf> <output.tachibk>"

            Examples:
              ./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.10/MSBF-to-TachiBK-v0.10test.tachibk"
              ./gradlew run --args="samples/testfavorites.msbf testdata/v0.10/MSBF-to-TachiBK-v0.10test.tachibk"
              ./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.10/quick-test.tachibk --no-metadata"
              ./gradlew run --args="convert samples/testfavorites.msbf --report-duplicates-only"
              ./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.10/deduped.tachibk --remove-duplicates"

            Options:
              --output, -o <file>       Output .tachibk file path
              --metadata                Fetch MangaDex metadata. This is the default.
              --no-metadata             Skip MangaDex metadata for quick tests
              --report-duplicates-only  Write duplicate report and stop before backup creation
              --remove-duplicates       Keep first MangaDex entry and remove later duplicate copies
              --version, -v             Print version
              --help, -h                Show this help message

            Notes:
              Metadata is fetched by default.
              Duplicates are kept by default.
              Output folders are created automatically when possible.
        """.trimIndent()
    }
}