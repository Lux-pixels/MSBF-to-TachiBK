import app.ConversionRequest
import app.ConverterService
import cli.CliParser
import java.io.File

/**
 * CLI entry point for MSBF-to-TachiBK.
 *
 * Main.kt should stay small.
 *
 * It is responsible for:
 * - Parsing CLI arguments
 * - Showing help/version output
 * - Creating a ConversionRequest
 * - Calling ConverterService
 *
 * The actual conversion logic lives in ConverterService so it can also be used
 * by the future local browser upload/download converter.
 */
fun main(args: Array<String>) {
    val options = CliParser.parse(args)

    if (options.showHelp) {
        println(CliParser.usage())
        return
    }

    if (options.showVersion) {
        println(CliParser.versionText())
        return
    }

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

    val outputFile = options.outputFile ?: File("MSBF-to-TachiBK.tachibk")

    val request = ConversionRequest(
        inputFile = inputFile,
        outputFile = outputFile,
        fetchMetadata = options.fetchMetadata,
        reportDuplicatesOnly = options.reportDuplicatesOnly,
        removeDuplicates = options.removeDuplicates,
    )

    ConverterService.convert(request)
}