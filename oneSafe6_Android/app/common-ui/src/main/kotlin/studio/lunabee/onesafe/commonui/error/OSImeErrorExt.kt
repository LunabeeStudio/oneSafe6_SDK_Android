package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.error.OSImeError

fun OSImeError.localizedTitle(): LbcTextSpec? = when (code) {
    OSImeError.Code.IME_BIOMETRIC_LOGIN_ERROR,
    -> null
}

fun OSImeError.localizedDescription(): LbcTextSpec? = when (code) {
    OSImeError.Code.IME_BIOMETRIC_LOGIN_ERROR,
    -> null
}
