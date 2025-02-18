package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.OSImportExportError

fun OSImportExportError.localizedTitle(): LbcTextSpec? = when (this.code) {
    OSImportExportError.Code.SALT_INVALID -> LbcTextSpec.StringResource(id = OSString.backup_protectBackup_title_invalidBackup)
    OSImportExportError.Code.METADATA_FILE_NOT_FOUND,
    OSImportExportError.Code.ARCHIVE_MALFORMED,
    OSImportExportError.Code.DATA_FILE_NOT_FOUND,
    OSImportExportError.Code.ID_NOT_FOUND,
    OSImportExportError.Code.EXPORT_METADATA_FAILURE,
    OSImportExportError.Code.EXPORT_DATA_FAILURE,
    OSImportExportError.Code.EXPORT_ICON_FAILURE,
    OSImportExportError.Code.WRONG_CREDENTIALS,
    OSImportExportError.Code.UNEXPECTED_ERROR,
    OSImportExportError.Code.METADATA_NOT_IN_CACHE,
    OSImportExportError.Code.EXPORT_FILE_FAILURE,
    OSImportExportError.Code.ENGINE_NOT_PREPARED,
    OSImportExportError.Code.BACKUP_FILE_DELETE_FAILED,
    OSImportExportError.Code.BACKUP_ID_NOT_FOUND_IN_DB,
    OSImportExportError.Code.FILE_NOT_A_BACKUP,
    OSImportExportError.Code.CANNOT_OPEN_URI,
    OSImportExportError.Code.CANNOT_OPEN_STREAM_WITHOUT_SAFE_ID,
    OSImportExportError.Code.MISSING_MAPPED_BACKUP_ICON,
    OSImportExportError.Code.MISSING_MAPPED_BACKUP_FILE,
    -> null
}

fun OSImportExportError.localizedDescription(): LbcTextSpec? = when (this.code) {
    OSImportExportError.Code.SALT_INVALID -> LbcTextSpec.StringResource(id = OSString.backup_protectBackup_description_invalidBackup)
    OSImportExportError.Code.METADATA_FILE_NOT_FOUND,
    OSImportExportError.Code.ARCHIVE_MALFORMED,
    OSImportExportError.Code.DATA_FILE_NOT_FOUND,
    OSImportExportError.Code.ID_NOT_FOUND,
    OSImportExportError.Code.EXPORT_METADATA_FAILURE,
    OSImportExportError.Code.EXPORT_DATA_FAILURE,
    OSImportExportError.Code.EXPORT_ICON_FAILURE,
    OSImportExportError.Code.WRONG_CREDENTIALS,
    OSImportExportError.Code.UNEXPECTED_ERROR,
    OSImportExportError.Code.METADATA_NOT_IN_CACHE,
    OSImportExportError.Code.EXPORT_FILE_FAILURE,
    OSImportExportError.Code.ENGINE_NOT_PREPARED,
    OSImportExportError.Code.BACKUP_FILE_DELETE_FAILED,
    OSImportExportError.Code.BACKUP_ID_NOT_FOUND_IN_DB,
    OSImportExportError.Code.FILE_NOT_A_BACKUP,
    OSImportExportError.Code.CANNOT_OPEN_URI,
    OSImportExportError.Code.CANNOT_OPEN_STREAM_WITHOUT_SAFE_ID,
    OSImportExportError.Code.MISSING_MAPPED_BACKUP_ICON,
    OSImportExportError.Code.MISSING_MAPPED_BACKUP_FILE,
    -> null
}
