package parser

object SourceDetector {

    fun detect(url: String): String {
        return when {
            url.contains("mangadex.org", true) -> "MangaDex"
            url.contains("mangasee", true) -> "MangaSee"
            url.contains("batoto", true) -> "Bato.to"
            url.contains("comick", true) -> "Comick"
            else -> "Unknown"
        }
    }
}