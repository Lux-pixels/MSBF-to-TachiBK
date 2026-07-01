import app.ConversionRequest
import app.ConverterService
import cli.CliParser
import java.io.File
import web.WebServer

/**
 * CLI entry point for MSBF-to-TachiBK.
 *
 * Main.kt is responsible for:
 * - Starting the local browser converter with the serve command
 * - Parsing CLI conversion arguments
 * - Showing help/version output
 * - Creating a ConversionRequest
 * - Calling ConverterService
 */
fun main(args: Array<String>) {
    /**
     * Local browser upload/download mode.
     *
     * Run:
     * ./gradlew run --args="serve"
     *
     * Then open:
     * http://localhost:8080
     */
    if (args.firstOrNull()?.lowercase() == "serve") {
        WebServer.start(port = 8080)
        return
    }

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