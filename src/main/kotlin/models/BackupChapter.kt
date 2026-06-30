package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class BackupChapter(
    @ProtoNumber(1) var url: String = "",
    @ProtoNumber(2) var name: String = "",
)