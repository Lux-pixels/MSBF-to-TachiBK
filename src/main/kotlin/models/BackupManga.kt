package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

@Serializable
data class BackupManga(
    @ProtoNumber(1) var source: Long,
    @ProtoNumber(2) var url: String,
    @ProtoNumber(3) var title: String = "",
    @ProtoNumber(13) var dateAdded: Long = 0,
    @ProtoNumber(17) var categories: List<Long> = emptyList(),
    @ProtoNumber(100) var favorite: Boolean = true,
    @ProtoNumber(111) var initialized: Boolean = false,
)