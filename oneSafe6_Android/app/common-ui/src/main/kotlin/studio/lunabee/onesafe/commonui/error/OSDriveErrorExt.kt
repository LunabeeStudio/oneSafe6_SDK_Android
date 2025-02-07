package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.OSDriveError

fun OSDriveError.localizedTitle(): LbcTextSpec? = when (this.code) {
    OSDriveError.Code.DRIVE_NETWORK_FAILURE ->
        LbcTextSpec.StringResource(OSString.common_error_network_title)
    OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND,
    OSDriveError.Code.DRIVE_UNEXPECTED_NULL_AUTH_INTENT,
    OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE,
    OSDriveError.Code.DRIVE_REQUEST_EXECUTION_FAILED,
    OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED,
    OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT,
    OSDriveError.Code.DRIVE_ENGINE_NOT_INITIALIZED,
    OSDriveError.Code.DRIVE_UNKNOWN_ERROR,
    OSDriveError.Code.DRIVE_CANNOT_DELETE_BACKUP_WITHOUT_SAFE_ID,
    -> null
}

fun OSDriveError.localizedDescription(): LbcTextSpec? = when (this.code) {
    OSDriveError.Code.DRIVE_UNEXPECTED_NULL_AUTH_INTENT ->
        LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_error_wrongAuthenticationErrorCause)
    OSDriveError.Code.DRIVE_NETWORK_FAILURE ->
        LbcTextSpec.StringResource(OSString.common_error_network_message)
    OSDriveError.Code.DRIVE_BACKUP_REMOTE_ID_NOT_FOUND ->
        LbcTextSpec.StringResource(OSString.settings_autoBackupScreen_error_driveBackupNotFound)
    OSDriveError.Code.DRIVE_WRONG_ACCOUNT_TYPE,
    OSDriveError.Code.DRIVE_REQUEST_EXECUTION_FAILED,
    OSDriveError.Code.DRIVE_AUTHENTICATION_REQUIRED,
    OSDriveError.Code.DRIVE_UNEXPECTED_NULL_ACCOUNT,
    OSDriveError.Code.DRIVE_ENGINE_NOT_INITIALIZED,
    OSDriveError.Code.DRIVE_UNKNOWN_ERROR,
    OSDriveError.Code.DRIVE_CANNOT_DELETE_BACKUP_WITHOUT_SAFE_ID,
    -> null
}
