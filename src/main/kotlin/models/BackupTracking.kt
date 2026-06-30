package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class BackupTracking(
    @ProtoNumber(1) var syncId: Int = 0,
    @ProtoNumber(2) var libraryId: Long = 0,
)