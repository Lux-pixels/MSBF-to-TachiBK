package web

import app.ConversionRequest
import app.ConversionResult
import app.ConverterService
import com.sun.net.httpserver.HttpExchange
import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Collections
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

/**
 * HTTP routes for the local browser converter.
 *
 * Routes:
 * - GET  /                    Browser upload page
 * - GET  /style.css           Page styling
 * - POST /api/convert         Start conversion job
 * - GET  /api/status/{id}     Read live conversion status/logs
 * - GET  /download/{id}       Download generated .tachibk file
 */
class WebRoutes {
    private val conversionFolder = File("build/web-conversions")
    private val jobs = ConcurrentHashMap<String, ConversionJob>()

    init {
        conversionFolder.mkdirs()
    }

    fun handle(exchange: HttpExchange) {
        val method = exchange.requestMethod
        val path = exchange.requestURI.path

        when {
            method.equals("GET", ignoreCase = true) && path == "/" -> {
                sendHtml(exchange, resourceText("web/index.html") ?: fallbackIndexPage())
            }

            method.equals("GET", ignoreCase = true) && path == "/style.css" -> {
                sendCss(exchange, resourceText("web/style.css") ?: "")
            }

            method.equals("POST", ignoreCase = true) && path == "/api/convert" -> {
                handleStartConversion(exchange)
            }

            method.equals("GET", ignoreCase = true) && path.startsWith("/api/status/") -> {
                handleStatus(exchange)
            }

            method.equals("GET", ignoreCase = true) && path.startsWith("/download/") -> {
                handleDownload(exchange)
            }

            else -> {
                sendText(exchange, 404, "Not found")
            }
        }
    }

    /**
     * Start a conversion job and return immediately.
     *
     * This prevents the browser/Codespaces forwarded port from timing out
     * during large conversions.
     */
    private fun handleStartConversion(exchange: HttpExchange) {
        val contentType = exchange.requestHeaders.getFirst("Content-Type").orEmpty()

        if (!contentType.startsWith("multipart/form-data")) {
            sendJson(exchange, 400, """{"error":"Expected multipart/form-data upload."}""")
            return
        }

        val boundary = contentType
            .substringAfter("boundary=", missingDelimiterValue = "")
            .trim()
            .removeSurrounding("\"")

        if (boundary.isBlank()) {
            sendJson(exchange, 400, """{"error":"Missing multipart boundary."}""")
            return
        }

        val body = exchange.requestBody.readBytes()
        val form = parseMultipart(body, boundary)
        val upload = form.files["msbfFile"]

        if (upload == null || upload.bytes.isEmpty()) {
            sendJson(exchange, 400, """{"error":"Please choose a Manga Storm .msbf file."}""")
            return
        }

        val originalName = sanitizeFileName(upload.fileName.ifBlank { "favorites.msbf" })

        if (!originalName.endsWith(".msbf", ignoreCase = true)) {
            sendJson(exchange, 400, """{"error":"Please upload a Manga Storm .msbf file."}""")
            return
        }

        val baseName = originalName.substringBeforeLast(".", originalName).ifBlank { "favorites" }
        val runId = UUID.randomUUID().toString()

        val inputFile = File(conversionFolder, "$runId-$originalName")
        val outputFile = File(conversionFolder, "$runId-$baseName.tachibk")
        val downloadName = "$baseName.tachibk"

        inputFile.writeBytes(upload.bytes)

        val fetchMetadata = form.fields["metadata"] == "on"
        val removeDuplicates = form.fields["removeDuplicates"] == "on"

        val job = ConversionJob(
            id = runId,
            inputFile = inputFile,
            outputFile = outputFile,
            downloadName = downloadName,
            fetchMetadata = fetchMetadata,
            removeDuplicates = removeDuplicates,
        )

        jobs[runId] = job
        startJob(job)

        sendJson(exchange, 200, """{"jobId":"$runId"}""")
    }

    /**
     * Run conversion in the background so the browser can poll live logs.
     */
    private fun startJob(job: ConversionJob) {
        thread(
            start = true,
            isDaemon = true,
            name = "conversion-${job.id}",
        ) {
            job.state = JobState.RUNNING

            job.addLog("Starting web conversion job...")
            job.addLog("Input file: ${job.inputFile.name}")
            job.addLog("Output file: ${job.downloadName}")
            job.addLog("Metadata fetch: ${if (job.fetchMetadata) "enabled" else "skipped"}")
            job.addLog("Duplicate handling: ${if (job.removeDuplicates) "remove duplicates" else "keep duplicates"}")
            job.addLog("")

            val conversionRun = runCatching {
                ConverterService.convert(
                    request = ConversionRequest(
                        inputFile = job.inputFile,
                        outputFile = job.outputFile,
                        fetchMetadata = job.fetchMetadata,
                        reportDuplicatesOnly = false,
                        removeDuplicates = job.removeDuplicates,
                    ),
                    logger = { line ->
                        job.addLog(line)
                        println(line)
                    }
                )
            }

            conversionRun.onSuccess { conversionResult ->
                job.result = conversionResult

                if (conversionResult.success && conversionResult.backupWritten && job.outputFile.exists()) {
                    job.state = JobState.COMPLETED
                    job.addLog("")
                    job.addLog("Web conversion complete.")
                    job.addLog("Download ready: ${job.downloadName}")
                } else {
                    job.state = JobState.FAILED
                    job.addLog("")
                    job.addLog("Conversion failed.")
                    job.addLog("No backup was written.")
                }
            }

            conversionRun.onFailure { error ->
                job.state = JobState.FAILED
                job.addLog("")
                job.addLog("Conversion crashed:")
                job.addLog(error.message ?: error::class.simpleName.orEmpty())
            }
        }
    }

    /**
     * Return live job status and logs as JSON.
     */
    private fun handleStatus(exchange: HttpExchange) {
        val id = exchange.requestURI.path.removePrefix("/api/status/")
        val job = jobs[id]

        if (job == null) {
            sendJson(exchange, 404, """{"error":"Job not found."}""")
            return
        }

        val result = job.result

        val downloadUrl = if (job.state == JobState.COMPLETED) {
            "/download/${job.id}"
        } else {
            null
        }

        val logsJson = job.snapshotLogs()
            .joinToString(
                separator = ",",
                prefix = "[",
                postfix = "]",
            ) { line ->
                jsonString(line)
            }

        val json = buildString {
            append("{")
            append("\"id\":")
            append(jsonString(job.id))
            append(",")

            append("\"state\":")
            append(jsonString(job.state.name.lowercase()))
            append(",")

            append("\"downloadUrl\":")
            append(downloadUrl?.let { jsonString(it) } ?: "null")
            append(",")

            append("\"downloadName\":")
            append(jsonString(job.downloadName))
            append(",")

            append("\"entriesParsed\":")
            append(result?.entriesParsed ?: 0)
            append(",")

            append("\"entriesWritten\":")
            append(result?.entriesWritten ?: 0)
            append(",")

            append("\"logs\":")
            append(logsJson)

            append("}")
        }

        sendJson(exchange, 200, json)
    }

    /**
     * Download the generated .tachibk file.
     */
    private fun handleDownload(exchange: HttpExchange) {
        val id = exchange.requestURI.path.removePrefix("/download/")
        val job = jobs[id]

        if (job == null || job.state != JobState.COMPLETED || !job.outputFile.exists()) {
            sendText(exchange, 404, "Download not found.")
            return
        }

        exchange.responseHeaders.add("Content-Type", "application/octet-stream")
        exchange.responseHeaders.add(
            "Content-Disposition",
            "attachment; filename=\"${job.downloadName}\"",
        )

        exchange.sendResponseHeaders(200, job.outputFile.length())

        job.outputFile.inputStream().use { input ->
            exchange.responseBody.use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun fallbackIndexPage(): String {
        return "<!doctype html><html lang=\"en\"><head><meta charset=\"utf-8\"><title>MSBF-to-TachiBK</title></head><body><h1>MSBF-to-TachiBK</h1><p>Missing web/index.html resource.</p></body></html>"
    }

    private fun parseMultipart(
        body: ByteArray,
        boundary: String,
    ): MultipartForm {
        val delimiter = "--$boundary"
        val bodyText = String(body, StandardCharsets.ISO_8859_1)

        val fields = mutableMapOf<String, String>()
        val files = mutableMapOf<String, UploadedFile>()

        bodyText
            .split(delimiter)
            .asSequence()
            .map { it.trim('\r', '\n') }
            .filter { it.isNotBlank() && it != "--" }
            .forEach { part ->
                val headerEnd = part.indexOf("\r\n\r\n")

                if (headerEnd == -1) {
                    return@forEach
                }

                val rawHeaders = part.substring(0, headerEnd)
                val content = part.substring(headerEnd + 4).trimEnd('\r', '\n')

                val disposition = rawHeaders
                    .lineSequence()
                    .firstOrNull { it.startsWith("Content-Disposition", ignoreCase = true) }
                    .orEmpty()

                val name = disposition.extractDispositionValue("name")
                val fileName = disposition.extractDispositionValue("filename")

                if (name.isBlank()) {
                    return@forEach
                }

                if (fileName.isNotBlank()) {
                    files[name] = UploadedFile(
                        fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8),
                        bytes = content.toByteArray(StandardCharsets.ISO_8859_1),
                    )
                } else {
                    fields[name] = content
                }
            }

        return MultipartForm(
            fields = fields,
            files = files,
        )
    }

    private fun String.extractDispositionValue(key: String): String {
        val regex = Regex("""$key="([^"]*)"""")
        return regex.find(this)?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun resourceText(path: String): String? {
        return Thread.currentThread()
            .contextClassLoader
            .getResourceAsStream(path)
            ?.use { input ->
                input.readBytes().toString(StandardCharsets.UTF_8)
            }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName
            .replace("\\", "_")
            .replace("/", "_")
            .replace(":", "_")
            .ifBlank { "favorites.msbf" }
    }

    private fun jsonString(value: String): String {
        return buildString {
            append("\"")

            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }

            append("\"")
        }
    }

    private fun sendHtml(exchange: HttpExchange, html: String) {
        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { output ->
            output.write(bytes)
        }
    }

    private fun sendCss(exchange: HttpExchange, css: String) {
        val bytes = css.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/css; charset=utf-8")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.use { output ->
            output.write(bytes)
        }
    }

    private fun sendJson(exchange: HttpExchange, status: Int, json: String) {
        val bytes = json.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { output ->
            output.write(bytes)
        }
    }

    private fun sendText(exchange: HttpExchange, status: Int, text: String) {
        val bytes = text.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
        exchange.sendResponseHeaders(status, bytes.size.toLong())
        exchange.responseBody.use { output ->
            output.write(bytes)
        }
    }

    private enum class JobState {
        QUEUED,
        RUNNING,
        COMPLETED,
        FAILED,
    }

    private data class ConversionJob(
        val id: String,
        val inputFile: File,
        val outputFile: File,
        val downloadName: String,
        val fetchMetadata: Boolean,
        val removeDuplicates: Boolean,
        val logs: MutableList<String> = Collections.synchronizedList(mutableListOf()),
        @Volatile var state: JobState = JobState.QUEUED,
        @Volatile var result: ConversionResult? = null,
    ) {
        fun addLog(line: String) {
            logs += line
        }

        fun snapshotLogs(): List<String> {
            return synchronized(logs) {
                logs.toList()
            }
        }
    }

    private data class MultipartForm(
        val fields: Map<String, String>,
        val files: Map<String, UploadedFile>,
    )

    private data class UploadedFile(
        val fileName: String,
        val bytes: ByteArray,
    )
}