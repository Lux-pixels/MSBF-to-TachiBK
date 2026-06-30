package backup

import config.Constants

object SourceMapper {
    fun toKomikkuSourceId(sourceKey: String): Long {
        return when (sourceKey.lowercase()) {
            Constants.MANGADEX_SOURCE_KEY -> Constants.MANGADEX_SOURCE_ID
            else -> error("Unsupported source: $sourceKey")
        }
    }

    fun sourceName(sourceKey: String): String {
        return when (sourceKey.lowercase()) {
            Constants.MANGADEX_SOURCE_KEY -> Constants.MANGADEX_SOURCE_NAME
            else -> sourceKey
        }
    }
}