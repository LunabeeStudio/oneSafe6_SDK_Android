package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.error.OSRepositoryError

fun OSRepositoryError.localizedTitle(): LbcTextSpec? = when (this.code) {
    OSRepositoryError.Code.SAFE_ID_NOT_LOADED,
    OSRepositoryError.Code.NO_BIOMETRIC_SAFE_FOUND,
    -> null
}

fun OSRepositoryError.localizedDescription(): LbcTextSpec? = when (this.code) {
    OSRepositoryError.Code.SAFE_ID_NOT_LOADED,
    OSRepositoryError.Code.NO_BIOMETRIC_SAFE_FOUND,
    -> null
}
