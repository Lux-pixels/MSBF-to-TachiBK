package metadata

sealed class MangaDexFetchResult {
    data class Success(val metadata: MangaDexMetadata) : MangaDexFetchResult()
    data class NotFound(val reason: String) : MangaDexFetchResult()
    data class ConnectionFailed(val reason: String) : MangaDexFetchResult()
}