package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.OSAppError

fun OSAppError.localizedTitle(): LbcTextSpec? = when (code) {
    OSAppError.Code.UNIMPLEMENTED_FEATURE,
    OSAppError.Code.NO_ITEM_FOUND_FOR_ID,
    OSAppError.Code.SAFE_ITEM_CREATION_FAILURE,
    OSAppError.Code.SAFE_ITEM_EDITION_FAILURE,
    OSAppError.Code.SIGN_UP_FAILURE,
    OSAppError.Code.BIOMETRIC_TOO_WEAK,
    OSAppError.Code.BIOMETRIC_ERROR,
    OSAppError.Code.URI_INVALID,
    OSAppError.Code.BIOMETRIC_LOGIN_ERROR,
    OSAppError.Code.URL_DATA_FETCHING_FAIL,
    OSAppError.Code.MIGRATION_IMPORT_MODE_NOT_SET,
    OSAppError.Code.MIGRATION_ONESAFE5_SERVICE_NULL_BINDING,
    OSAppError.Code.MIGRATION_MISSING_PASSWORD,
    OSAppError.Code.BIOMETRIC_LOGIN_CIPHER_ERROR,
    OSAppError.Code.EXPORT_WORKER_FAILURE,
    OSAppError.Code.EXPORT_WORKER_CANCELED,
    OSAppError.Code.FILE_SAVING_ERROR,
    OSAppError.Code.IMAGE_CAPTURE_FAILED,
    OSAppError.Code.CAMERA_SETUP_FAILED,
    OSAppError.Code.EXPORT_UNKNOWN_ID,
    -> null
    OSAppError.Code.IMAGE_CAPTURED_NOT_FOUND -> LbcTextSpec.StringResource(OSString.imagePicker_error_captureNotFound_title)
}

fun OSAppError.localizedDescription(): LbcTextSpec? = when (code) {
    OSAppError.Code.UNIMPLEMENTED_FEATURE,
    OSAppError.Code.NO_ITEM_FOUND_FOR_ID,
    OSAppError.Code.SAFE_ITEM_CREATION_FAILURE,
    OSAppError.Code.SAFE_ITEM_EDITION_FAILURE,
    OSAppError.Code.SIGN_UP_FAILURE,
    OSAppError.Code.BIOMETRIC_TOO_WEAK,
    OSAppError.Code.BIOMETRIC_ERROR,
    OSAppError.Code.URI_INVALID,
    OSAppError.Code.BIOMETRIC_LOGIN_ERROR,
    OSAppError.Code.MIGRATION_IMPORT_MODE_NOT_SET,
    OSAppError.Code.MIGRATION_ONESAFE5_SERVICE_NULL_BINDING,
    OSAppError.Code.MIGRATION_MISSING_PASSWORD,
    OSAppError.Code.BIOMETRIC_LOGIN_CIPHER_ERROR,
    OSAppError.Code.EXPORT_WORKER_FAILURE,
    OSAppError.Code.EXPORT_WORKER_CANCELED,
    OSAppError.Code.FILE_SAVING_ERROR,
    OSAppError.Code.IMAGE_CAPTURE_FAILED,
    OSAppError.Code.CAMERA_SETUP_FAILED,
    OSAppError.Code.EXPORT_UNKNOWN_ID,
    -> null
    OSAppError.Code.URL_DATA_FETCHING_FAIL -> LbcTextSpec.StringResource(OSString.safeItemDetail_urlFetching_errorDescription)
    OSAppError.Code.IMAGE_CAPTURED_NOT_FOUND -> LbcTextSpec.StringResource(OSString.imagePicker_error_captureNotFound_message)
}
