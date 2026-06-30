package app

import java.io.File

/**
 * Request object for one conversion run.
 *
 * This is used by both:
 * - CLI conversion
 * - Future local browser upload/download conversion
 */
data class ConversionRequest(
    val inputFile: File,
    val outputFile: File,
    val fetchMetadata: Boolean,
    val reportDuplicatesOnly: Boolean,
    val removeDuplicates: Boolean,
)

/**
 * Result object returned after one conversion run.
 *
 * This gives the CLI or future web UI a simple way to know what happened.
 */
data class ConversionResult(
    val success: Boolean,
    val backupWritten: Boolean,
    val outputFile: File?,
    val entriesParsed: Int,
    val entriesWritten: Int,
    val errors: List<String> = emptyList(),
)