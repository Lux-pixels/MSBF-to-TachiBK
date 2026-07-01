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
 * - GET  /                         Browser upload page
 * - GET  /style.css                Page styling
 * - POST /api/convert              Start conversion job
 * - GET  /api/status/{id}          Read live conversion status
 * - GET  /download/{id}            Download generated .tachibk file
 * - GET  /download-duplicates/{id} Download duplicate report, if available
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

            method.equals("GET", ignoreCase = true) && path.startsWith("/download-duplicates/") -> {
                handleDuplicateReportDownload(exchange)
            }

            method.equals("GET", ignoreCase = true) && path.startsWith("/download/") -> {
                handleBackupDownload(exchange)
            }

            else -> {
                sendText(exchange, 404, "Not found")
            }
        }
    }

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
        val duplicateReportFile = File(conversionFolder, "$runId-duplicate-manga-report.txt")

        inputFile.writeBytes(upload.bytes)

        val fetchMetadata = form.fields["metadata"] == "on"
        val removeDuplicates = form.fields["removeDuplicates"] == "on"

        val job = ConversionJob(
            id = runId,
            inputFile = inputFile,
            outputFile = outputFile,
            backupDownloadName = "$baseName.tachibk",
            duplicateReportFile = duplicateReportFile,
            duplicateReportDownloadName = "duplicate-manga-report.txt",
            fetchMetadata = fetchMetadata,
            removeDuplicates = removeDuplicates,
        )

        jobs[runId] = job
        startJob(job)

        sendJson(exchange, 200, """{"jobId":"$runId"}""")
    }

    private fun startJob(job: ConversionJob) {
        thread(
            start = true,
            isDaemon = true,
            name = "conversion-${job.id}",
        ) {
            job.state = JobState.RUNNING

            val sharedDuplicateReport = File("duplicate-manga-report.txt")
            sharedDuplicateReport.delete()
            job.duplicateReportFile.delete()

            job.addLog("Starting web conversion job...")

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

                if (sharedDuplicateReport.exists() && sharedDuplicateReport.length() > 0) {
                    sharedDuplicateReport.copyTo(job.duplicateReportFile, overwrite = true)
                }

                if (conversionResult.success && conversionResult.backupWritten && job.outputFile.exists()) {
                    job.state = JobState.COMPLETED
                    job.addLog("")
                    job.addLog("Web conversion complete.")
                    job.addLog("Download ready: ${job.backupDownloadName}")
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

    private fun handleStatus(exchange: HttpExchange) {
        val id = exchange.requestURI.path.removePrefix("/api/status/")
        val job = jobs[id]

        if (job == null) {
            sendJson(exchange, 404, """{"error":"Job not found."}""")
            return
        }

        val logs = job.snapshotLogs()
        val result = job.result

        val mangaIdentified = parseLogNumber(logs, "Total Manga Identified:")
            ?: result?.entriesParsed
            ?: 0

        val mangaIncluded = parseLogNumber(logs, "Manga Included In Backup:")
            ?: result?.entriesWritten
            ?: 0

        val mangaDuplicates = parseLogNumber(logs, "Extra duplicate copies:")
            ?: 0

        val progressText = parseProgressText(
            logs = logs,
            mangaIdentified = mangaIdentified,
            mangaIncluded = mangaIncluded,
            fetchMetadata = job.fetchMetadata,
            state = job.state,
        )

        val mangadexSourceCount = parseLogNumber(logs, "z13mangadex:")
            ?: 0

        val readingCount = parseLogNumber(logs, "Reading:")
            ?: 0

        val followingCount = parseLogNumber(logs, "Following:")
            ?: 0

        val onHoldCount = parseLogNumber(logs, "On Hold:")
            ?: 0

        val backupDownloadUrl = if (job.state == JobState.COMPLETED) {
            "/download/${job.id}"
        } else {
            null
        }

        val duplicateReportUrl = if (
            job.state == JobState.COMPLETED &&
            job.duplicateReportFile.exists() &&
            job.duplicateReportFile.length() > 0
        ) {
            "/download-duplicates/${job.id}"
        } else {
            null
        }

        val json = buildString {
            append("{")
            append("\"id\":")
            append(jsonString(job.id))
            append(",")

            append("\"state\":")
            append(jsonString(job.state.name.lowercase()))
            append(",")

            append("\"backupDownloadUrl\":")
            append(backupDownloadUrl?.let { jsonString(it) } ?: "null")
            append(",")

            append("\"backupDownloadName\":")
            append(jsonString(job.backupDownloadName))
            append(",")

            append("\"duplicateReportUrl\":")
            append(duplicateReportUrl?.let { jsonString(it) } ?: "null")
            append(",")

            append("\"duplicateReportDownloadName\":")
            append(jsonString(job.duplicateReportDownloadName))
            append(",")

            append("\"progressText\":")
            append(jsonString(progressText))
            append(",")

            append("\"mangaIdentified\":")
            append(mangaIdentified)
            append(",")

            append("\"mangaIncluded\":")
            append(mangaIncluded)
            append(",")

            append("\"mangaDuplicates\":")
            append(mangaDuplicates)
            append(",")

            append("\"mangadexSourceCount\":")
            append(mangadexSourceCount)
            append(",")

            append("\"readingCount\":")
            append(readingCount)
            append(",")

            append("\"followingCount\":")
            append(followingCount)
            append(",")

            append("\"onHoldCount\":")
            append(onHoldCount)

            append("}")
        }

        sendJson(exchange, 200, json)
    }

    private fun handleBackupDownload(exchange: HttpExchange) {
        val id = exchange.requestURI.path.removePrefix("/download/")
        val job = jobs[id]

        if (job == null || job.state != JobState.COMPLETED || !job.outputFile.exists()) {
            sendText(exchange, 404, "Download not found.")
            return
        }

        sendFile(
            exchange = exchange,
            file = job.outputFile,
            downloadName = job.backupDownloadName,
        )
    }

    private fun handleDuplicateReportDownload(exchange: HttpExchange) {
        val id = exchange.requestURI.path.removePrefix("/download-duplicates/")
        val job = jobs[id]

        if (
            job == null ||
            job.state != JobState.COMPLETED ||
            !job.duplicateReportFile.exists() ||
            job.duplicateReportFile.length() == 0L
        ) {
            sendText(exchange, 404, "Duplicate report not found.")
            return
        }

        sendFile(
            exchange = exchange,
            file = job.duplicateReportFile,
            downloadName = job.duplicateReportDownloadName,
        )
    }

    private fun sendFile(
        exchange: HttpExchange,
        file: File,
        downloadName: String,
    ) {
        exchange.responseHeaders.add("Content-Type", "application/octet-stream")
        exchange.responseHeaders.add(
            "Content-Disposition",
            "attachment; filename=\"$downloadName\"",
        )

        exchange.sendResponseHeaders(200, file.length())

        file.inputStream().use { input ->
            exchange.responseBody.use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun parseLogNumber(
        logs: List<String>,
        prefix: String,
    ): Int? {
        return logs
            .lastOrNull { it.trim().startsWith(prefix) }
            ?.substringAfter(prefix)
            ?.trim()
            ?.toIntOrNull()
    }

    private fun parseProgressText(
        logs: List<String>,
        mangaIdentified: Int,
        mangaIncluded: Int,
        fetchMetadata: Boolean,
        state: JobState,
    ): String {
        val metadataProgressLine = logs.lastOrNull { line ->
            line.contains("Checked ") &&
                line.contains("/") &&
                line.contains(" entries")
        }

        if (metadataProgressLine != null) {
            val match = Regex("""Checked\s+(\d+)/(\d+)\s+entries""")
                .find(metadataProgressLine)

            if (match != null) {
                return "${match.groupValues[1]}/${match.groupValues[2]}"
            }
        }

        return when (state) {
            JobState.QUEUED -> "Queued"

            JobState.RUNNING -> {
                if (fetchMetadata) {
                    if (mangaIdentified > 0) {
                        "Checking MangaDex metadata..."
                    } else {
                        "Starting metadata check..."
                    }
                } else {
                    if (mangaIdentified > 0) {
                        "Processing backup..."
                    } else {
                        "Starting conversion..."
                    }
                }
            }

            JobState.COMPLETED -> {
                if (mangaIdentified > 0) {
                    "$mangaIncluded/$mangaIdentified"
                } else {
                    "Complete"
                }
            }

            JobState.FAILED -> "Failed"
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
        val backupDownloadName: String,
        val duplicateReportFile: File,
        val duplicateReportDownloadName: String,
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