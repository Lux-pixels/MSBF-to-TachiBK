package backup

import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import models.Backup
import okio.buffer
import okio.gzip
import okio.sink
import java.io.File

object BackupWriter {
    fun write(backup: Backup, output: File) {
        val bytes = ProtoBuf.encodeToByteArray(backup)

        output.sink().gzip().buffer().use { sink ->
            sink.write(bytes)
        }
    }
}