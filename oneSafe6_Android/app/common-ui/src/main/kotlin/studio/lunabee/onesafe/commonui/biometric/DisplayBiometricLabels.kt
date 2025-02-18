package studio.lunabee.onesafe.commonui.biometric

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed class DisplayBiometricLabels {
    abstract val title: LbcTextSpec
    abstract val description: LbcTextSpec
    abstract val negativeButtonText: LbcTextSpec

    object Verify : DisplayBiometricLabels() {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_popupBiometric_title)
        override val description: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_popupBiometric_message)
        override val negativeButtonText: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_cancel)
    }

    object Login : DisplayBiometricLabels() {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_popupBiometric_title)
        override val description: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_popupBiometric_message)
        override val negativeButtonText: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_popupBiometric_cancel)
    }

    class SignUp(biometricHardware: BiometricHardware) : DisplayBiometricLabels() {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_popupBiometric_title)
        override val description: LbcTextSpec = biometricHardware.message
        override val negativeButtonText: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_popupBiometric_refuse)
    }
}
