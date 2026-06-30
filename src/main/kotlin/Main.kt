import parser.MsbfParser
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: ./gradlew run --args=\"favorites.msbf\"")
        return
    }

    val inputFile = File(args[0])

    if (!inputFile.exists()) {
        println("Error: File not found: ${inputFile.absolutePath}")
        return
    }

   val entries = MsbfParser.parse(inputFile)

    println()
    println("Loaded ${entries.size} manga.")
    println()

    println("Sources:")

    entries
     .groupBy { it.sourceKey }
     .forEach { (source, list) ->
        println("  $source: ${list.size}")
     }

    println()
    println("First 10 titles:")
    println()

    entries
        .take(10)
        .forEach {
            println(" • ${it.title}")
        }
}