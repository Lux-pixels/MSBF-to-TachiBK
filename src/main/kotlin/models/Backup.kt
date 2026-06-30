package models

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber

/**
 * Root backup model for the generated Komikku/Tachiyomi backup.
 *
 * For V1 safety, this only restores:
 * - Manga
 * - Categories
 * - Sources
 *
 * App Settings are intentionally not included yet because restoring them can
 * unexpectedly toggle delegated sources back on in Komikku.
 */
@Serializable
data class Backup(
    @ProtoNumber(1) val backupManga: List<BackupManga>,
    @ProtoNumber(2) var backupCategories: List<BackupCategory> = emptyList(),
    @ProtoNumber(101) var backupSources: List<BackupSource> = emptyList(),
)