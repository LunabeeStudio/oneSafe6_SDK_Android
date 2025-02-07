package studio.lunabee.onesafe.feature.biometric

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed interface BiometricCreationLabels {
    val sectionTitle: LbcTextSpec
    val sectionMessage: LbcTextSpec

    data object Onboarding : BiometricCreationLabels {
        override val sectionTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_sectionTitle)
        override val sectionMessage: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_sectionMessage_android)
    }

    data object ChangePassword : BiometricCreationLabels {
        override val sectionTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_fastIdScreen_sectionTitle)
        override val sectionMessage: LbcTextSpec = LbcTextSpec.StringResource(OSString.changePassword_fastIdScreen_sectionMessage_android)
    }

    data object MultiSafe : BiometricCreationLabels {
        override val sectionTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_fastIdScreen_sectionTitle)
        override val sectionMessage: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_fastIdScreen_sectionMessage_android)
    }
}
