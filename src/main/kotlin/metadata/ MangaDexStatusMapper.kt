package metadata

object MangaDexStatusMapper {
    fun toKomikkuStatus(status: String?): Int {
        return when (status?.lowercase()) {
            "ongoing" -> 1
            "completed" -> 2
            "cancelled" -> 3
            "hiatus" -> 4
            else -> 0
        }
    }
}