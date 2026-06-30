package parser

import java.io.File

object MsbfParser {

    private val sourceRegex = Regex("""^(z\d+[A-Za-z0-9_-]+)\s+""")
    private val urlRegex = Regex("""https?://\S+""")

    fun parse(file: File): List<MsbfEntry> {
        return file.readLines()
            .drop(1)
            .mapNotNull { line ->
                val urlMatch = urlRegex.find(line) ?: return@mapNotNull null
                val sourceMatch = sourceRegex.find(line) ?: return@mapNotNull null

                val sourceKey = sourceMatch.groupValues[1]
                val url = urlMatch.value.trim()

                val title = line
                    .substring(sourceMatch.range.last + 1, urlMatch.range.first)
                    .trim()

                val afterUrl = line.substring(urlMatch.range.last + 1).trim()
                val afterParts = afterUrl.split(Regex("""\s+"""))

                MsbfEntry(
                    sourceKey = sourceKey,
                    title = title,
                    url = url,
                    status = afterParts.getOrNull(0),
                    timestamp = afterParts.getOrNull(1)?.toDoubleOrNull()?.toLong()
                )
            }
    }
}