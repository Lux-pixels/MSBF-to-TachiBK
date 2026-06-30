package cli

import config.Constants
import java.io.File

/**
 * Parsed command-line options for one converter run.
 *
 * The goal is to keep argument parsing separate from Main.kt so the main
 * conversion flow stays readable.
 */
data class CliOptions(
    val inputFile: File?,
    val outputFile: File?,
    val fetchMetadata: Boolean,
    val showHelp: Boolean,
    val showVersion: Boolean,
    val errors: List<String>,
)

/**
 * Small command-line parser for MSBF-to-TachiBK.
 *
 * Supported styles:
 *
 * Old/simple style:
 * ./gradlew run --args="samples/favorites.msbf testdata/v0.6/output.tachibk"
 *
 * New command style:
 * ./gradlew run --args="convert samples/favorites.msbf --output testdata/v0.6/output.tachibk"
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
                errors = emptyList(),
            )
        }

        val errors = mutableListOf<String>()
        val positionalArgs = mutableListOf<String>()

        var outputFile: File? = null
        var fetchMetadata = true
        var showHelp = false
        var showVersion = false

        var index = 0

        /**
         * Allow the cleaner future command format:
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
                    /**
                     * Metadata is already enabled by default, but this flag is accepted
                     * so users can be explicit.
                     */
                    fetchMetadata = true
                }

                "--no-metadata" -> {
                    /**
                     * Fast test mode. This skips MangaDex API calls.
                     */
                    fetchMetadata = false
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

        /**
         * Positional arguments:
         *
         * 1st = input .msbf file
         * 2nd = output .tachibk file, unless --output was used
         */
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
              ./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.6/MSBF-to-TachiBK-v0.6test.tachibk"
              ./gradlew run --args="samples/testfavorites.msbf testdata/v0.6/MSBF-to-TachiBK-v0.6test.tachibk"
              ./gradlew run --args="convert samples/testfavorites.msbf --output testdata/v0.6/quick-test.tachibk --no-metadata"

            Options:
              --output, -o <file>    Output .tachibk file path
              --metadata             Fetch MangaDex metadata. This is the default.
              --no-metadata          Skip MangaDex metadata for quick tests
              --version, -v          Print version
              --help, -h             Show this help message

            Notes:
              Metadata is fetched by default.
              Output folders are created automatically when possible.
        """.trimIndent()
    }
}