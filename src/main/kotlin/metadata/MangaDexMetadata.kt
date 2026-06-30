package metadata

data class MangaDexMetadata(
    val title: String?,
    val author: String?,
    val artist: String?,
    val description: String?,
    val status: Int,
    val genres: List<String>,
    val coverUrl: String?,
)