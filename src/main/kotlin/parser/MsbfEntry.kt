package parser

data class MsbfEntry(
    val title: String,
    val url: String,
    val sourceKey: String,
    val status: String?,
    val timestamp: Long?
)