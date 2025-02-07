package studio.lunabee.onesafe.commonui.error

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.error.OSRemoteError

fun OSRemoteError.localizedTitle(): LbcTextSpec? = when (code) {
    OSRemoteError.Code.UNKNOWN_HTTP_ERROR,
    OSRemoteError.Code.UNEXPECTED_TIMEOUT,
    -> null
}

fun OSRemoteError.localizedDescription(): LbcTextSpec? = when (code) {
    OSRemoteError.Code.UNKNOWN_HTTP_ERROR,
    OSRemoteError.Code.UNEXPECTED_TIMEOUT,
    -> null
}
