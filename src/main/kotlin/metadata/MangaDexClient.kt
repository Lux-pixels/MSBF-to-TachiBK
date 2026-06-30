package metadata

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

object MangaDexClient {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    fun fetchDetailed(uuid: String): MangaDexFetchResult {
        return try {
            val url = "https://api.mangadex.org/manga/$uuid?includes[]=author&includes[]=artist&includes[]=cover_art"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MSBF-to-TachiBK/0.2.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return MangaDexFetchResult.NotFound("HTTP ${response.code}")
                }

                val body = response.body?.string()
                    ?: return MangaDexFetchResult.NotFound("Empty response body")

                val root = json.parseToJsonElement(body).jsonObject
                val data = root["data"]?.jsonObject
                    ?: return MangaDexFetchResult.NotFound("Missing data object")

                val attributes = data["attributes"]?.jsonObject
                    ?: return MangaDexFetchResult.NotFound("Missing attributes object")

                val title = attributes["title"]
                    ?.jsonObject
                    ?.get("en")
                    ?.jsonPrimitive
                    ?.contentOrNull

                val description = attributes["description"]
                    ?.jsonObject
                    ?.get("en")
                    ?.jsonPrimitive
                    ?.contentOrNull

                val statusText = attributes["status"]
                    ?.jsonPrimitive
                    ?.contentOrNull

                val tags = attributes["tags"]
                    ?.jsonArray
                    ?.mapNotNull {
                        it.jsonObject["attributes"]
                            ?.jsonObject
                            ?.get("name")
                            ?.jsonObject
                            ?.get("en")
                            ?.jsonPrimitive
                            ?.contentOrNull
                    }
                    ?: emptyList()

                var author: String? = null
                var artist: String? = null
                var coverFileName: String? = null

                data["relationships"]?.jsonArray?.forEach { rel ->
                    val relObj = rel.jsonObject
                    val type = relObj["type"]?.jsonPrimitive?.contentOrNull
                    val relAttrs: JsonObject? = relObj["attributes"]?.jsonObject

                    when (type) {
                        "author" -> author = relAttrs?.get("name")?.jsonPrimitive?.contentOrNull
                        "artist" -> artist = relAttrs?.get("name")?.jsonPrimitive?.contentOrNull
                        "cover_art" -> coverFileName = relAttrs?.get("fileName")?.jsonPrimitive?.contentOrNull
                    }
                }

                val coverUrl = coverFileName?.let {
                    "https://uploads.mangadex.org/covers/$uuid/$it.512.jpg"
                }

                MangaDexFetchResult.Success(
                    MangaDexMetadata(
                        title = title,
                        author = author,
                        artist = artist,
                        description = description,
                        status = MangaDexStatusMapper.toKomikkuStatus(statusText),
                        genres = tags,
                        coverUrl = coverUrl,
                    )
                )
            }
        } catch (e: IOException) {
            MangaDexFetchResult.ConnectionFailed(e.message ?: "Network connection failed")
        } catch (e: Exception) {
            MangaDexFetchResult.NotFound(e.message ?: "Metadata parse error")
        }
    }
}