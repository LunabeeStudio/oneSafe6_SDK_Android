package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.OSStorageError

fun OSStorageError.localizedTitle(): LbcTextSpec? = when (this.code) {
    OSStorageError.Code.ITEM_KEY_NOT_FOUND,
    OSStorageError.Code.ITEM_NOT_FOUND,
    OSStorageError.Code.UNKNOWN_DATABASE_ERROR,
    OSStorageError.Code.PROTO_DATASTORE_READ_ERROR,
    OSStorageError.Code.CONTACT_NOT_FOUND,
    OSStorageError.Code.CONTACT_KEY_NOT_FOUND,
    OSStorageError.Code.ENQUEUED_MESSAGE_ALREADY_EXIST_ERROR,
    OSStorageError.Code.ENQUEUED_MESSAGE_NOT_FOUND_FOR_DELETE,
    OSStorageError.Code.UNKNOWN_FILE_ERROR,
    OSStorageError.Code.MISSING_BACKUP_FILE,
    OSStorageError.Code.DATABASE_WRONG_KEY,
    OSStorageError.Code.DATABASE_NOT_FOUND,
    OSStorageError.Code.DATABASE_BACKUP_ERROR,
    OSStorageError.Code.DATABASE_CANNOT_ACCESS_FILES,
    OSStorageError.Code.DATABASE_CANNOT_ACCESS_DIR,
    OSStorageError.Code.DATABASE_CORRUPTED,
    OSStorageError.Code.NO_SALT_FOUND,
    OSStorageError.Code.SAFE_ID_ALREADY_LOADED,
    -> null
}

fun OSStorageError.localizedDescription(): LbcTextSpec? = when (this.code) {
    OSStorageError.Code.ITEM_KEY_NOT_FOUND,
    OSStorageError.Code.ITEM_NOT_FOUND,
    OSStorageError.Code.UNKNOWN_DATABASE_ERROR,
    OSStorageError.Code.PROTO_DATASTORE_READ_ERROR,
    OSStorageError.Code.CONTACT_NOT_FOUND,
    OSStorageError.Code.CONTACT_KEY_NOT_FOUND,
    OSStorageError.Code.ENQUEUED_MESSAGE_ALREADY_EXIST_ERROR,
    OSStorageError.Code.ENQUEUED_MESSAGE_NOT_FOUND_FOR_DELETE,
    OSStorageError.Code.UNKNOWN_FILE_ERROR,
    OSStorageError.Code.MISSING_BACKUP_FILE,
    OSStorageError.Code.DATABASE_NOT_FOUND,
    OSStorageError.Code.DATABASE_BACKUP_ERROR,
    OSStorageError.Code.DATABASE_CANNOT_ACCESS_FILES,
    OSStorageError.Code.DATABASE_CANNOT_ACCESS_DIR,
    OSStorageError.Code.DATABASE_CORRUPTED,
    OSStorageError.Code.NO_SALT_FOUND,
    OSStorageError.Code.SAFE_ID_ALREADY_LOADED,
    -> null
    OSStorageError.Code.DATABASE_WRONG_KEY ->
        LbcTextSpec.StringResource(OSString.cipherRecover_keycard_error_wrongKey)
}
