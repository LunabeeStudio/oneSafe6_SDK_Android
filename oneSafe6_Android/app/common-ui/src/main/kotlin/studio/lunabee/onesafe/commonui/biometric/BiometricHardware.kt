package studio.lunabee.onesafe.commonui.biometric

import androidx.annotation.DrawableRes
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString

enum class BiometricHardware(@DrawableRes val icon: Int?, val message: LbcTextSpec, val biometricName: LbcTextSpec) {
    FEATURE_FACE(
        icon = OSDrawable.ic_face,
        message = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_message_face),
        biometricName = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_name_face),
    ),
    FEATURE_FINGERPRINT(
        icon = OSDrawable.ic_fingerprint,
        message = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_message_fingerprint),
        biometricName = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_name_fingerprint),
    ),
    FEATURE_IRIS(
        icon = OSDrawable.ic_iris,
        message = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_message_fingerprint),
        biometricName = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_name_iris),
    ),
    NONE(
        icon = null,
        message = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_message_fingerprint),
        biometricName = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_name_fingerprint),
    ),
}
