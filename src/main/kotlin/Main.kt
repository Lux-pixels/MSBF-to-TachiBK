import backup.BackupBuilder
import backup.BackupWriter
import parser.MsbfParser
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage:")
        println("  ./gradlew run --args=\"samples/favorites.msbf\"")
        println("  ./gradlew run --args=\"samples/favorites.msbf output.tachibk\"")
        return
    }

    val inputFile = File(args[0])

    if (!inputFile.exists()) {
        println("Error: File not found: ${inputFile.absolutePath}")
        return
    }

    val outputFile = if (args.size >= 2) {
        File(args[1])
    } else {
        File("MSBF-to-TachiBK.tachibk")
    }

    val entries = MsbfParser.parse(inputFile)

    println()
    println("Loaded ${entries.size} manga.")
    println()

    println("Sources:")
    entries.groupBy { it.sourceKey }.forEach { (source, list) ->
        println("  $source: ${list.size}")
    }

    val backup = BackupBuilder.build(entries)

    println()
    println("Backup Summary")
    println("==============")
    println("Manga: ${backup.backupManga.size}")
    println("Sources: ${backup.backupSources.size}")
    println("Categories: ${backup.backupCategories.size}")

    backup.backupSources.forEach {
        println("Source -> ${it.name} (${it.sourceId})")
    }

    BackupWriter.write(backup, outputFile)

    println()
    println("Backup written to:")
    println(outputFile.absolutePath)
}