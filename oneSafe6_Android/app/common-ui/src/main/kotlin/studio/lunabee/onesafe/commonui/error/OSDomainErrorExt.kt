package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.error.OSDomainError

fun OSDomainError.localizedTitle(): LbcTextSpec? = when (this.code) {
    OSDomainError.Code.SAFE_ITEM_DELETE_FAILURE,
    OSDomainError.Code.SAFE_ITEM_REMOVE_FAILURE,
    OSDomainError.Code.SAFE_ITEM_NO_ICON,
    OSDomainError.Code.DUPLICATE_ICON_FAILED,
    OSDomainError.Code.OLD_DATA_IMPORT_FAILURE,
    OSDomainError.Code.SIGNIN_NOT_SIGNED_UP,
    OSDomainError.Code.UNZIP_FAILURE,
    OSDomainError.Code.ZIP_FAILURE,
    OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD,
    OSDomainError.Code.NO_MATCHING_CONTACT,
    OSDomainError.Code.DUPLICATED_MESSAGE,
    OSDomainError.Code.DECRYPT_MESSAGE_NOT_BASE64,
    OSDomainError.Code.HAND_SHAKE_DATA_NOT_FOUND,
    OSDomainError.Code.WRONG_CONTACT,
    OSDomainError.Code.NOT_AN_INVITATION_MESSAGE,
    OSDomainError.Code.CRYPTO_NOT_READY_TIMEOUT,
    OSDomainError.Code.MISSING_FILE_ID_IN_FIELD,
    OSDomainError.Code.MISSING_URI_OUTPUT_STREAM,
    OSDomainError.Code.UNZIP_SECURITY_TRAVERSAL_VULNERABILITY,
    OSDomainError.Code.ALPHA_INDEX_COMPUTE_FAILED,
    OSDomainError.Code.UNKNOWN_ERROR,
    OSDomainError.Code.NO_HTML_PAGE_FOUND,
    OSDomainError.Code.DATABASE_ENCRYPTION_NOT_ENABLED,
    OSDomainError.Code.DATABASE_ENCRYPTION_ALREADY_ENABLED,
    OSDomainError.Code.DATABASE_KEY_BAD_FORMAT,
    OSDomainError.Code.DATABASE_ENCRYPTION_KEY_KEYSTORE_LOST,
    OSDomainError.Code.SAFE_ID_NOT_READY_TIMEOUT,
    OSDomainError.Code.MISSING_BIOMETRIC_KEY,
    -> null
}

fun OSDomainError.localizedDescription(): LbcTextSpec? = when (this.code) {
    OSDomainError.Code.SAFE_ITEM_DELETE_FAILURE,
    OSDomainError.Code.SAFE_ITEM_REMOVE_FAILURE,
    OSDomainError.Code.SAFE_ITEM_NO_ICON,
    OSDomainError.Code.DUPLICATE_ICON_FAILED,
    OSDomainError.Code.OLD_DATA_IMPORT_FAILURE,
    OSDomainError.Code.SIGNIN_NOT_SIGNED_UP,
    OSDomainError.Code.UNZIP_FAILURE,
    OSDomainError.Code.ZIP_FAILURE,
    OSDomainError.Code.NO_MATCHING_CONTACT,
    OSDomainError.Code.DUPLICATED_MESSAGE,
    OSDomainError.Code.DECRYPT_MESSAGE_NOT_BASE64,
    OSDomainError.Code.HAND_SHAKE_DATA_NOT_FOUND,
    OSDomainError.Code.WRONG_CONTACT,
    OSDomainError.Code.NOT_AN_INVITATION_MESSAGE,
    OSDomainError.Code.CRYPTO_NOT_READY_TIMEOUT,
    OSDomainError.Code.MISSING_FILE_ID_IN_FIELD,
    OSDomainError.Code.MISSING_URI_OUTPUT_STREAM,
    OSDomainError.Code.UNZIP_SECURITY_TRAVERSAL_VULNERABILITY,
    OSDomainError.Code.ALPHA_INDEX_COMPUTE_FAILED,
    OSDomainError.Code.UNKNOWN_ERROR,
    OSDomainError.Code.NO_HTML_PAGE_FOUND,
    OSDomainError.Code.DATABASE_ENCRYPTION_NOT_ENABLED,
    OSDomainError.Code.DATABASE_ENCRYPTION_ALREADY_ENABLED,
    OSDomainError.Code.DATABASE_ENCRYPTION_KEY_KEYSTORE_LOST,
    OSDomainError.Code.SAFE_ID_NOT_READY_TIMEOUT,
    OSDomainError.Code.MISSING_BIOMETRIC_KEY,
    -> null
    OSDomainError.Code.DATABASE_KEY_BAD_FORMAT ->
        LbcTextSpec.StringResource(OSString.cipherRecover_keycard_error_badFormat)
    OSDomainError.Code.WRONG_CONFIRMATION_PASSWORD ->
        LbcTextSpec.StringResource(OSString.onboarding_passwordConfirmationScreen_errorMessage)
}
