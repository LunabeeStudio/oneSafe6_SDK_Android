package studio.lunabee.onesafe.feature.congratulation

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed interface CongratulationScreenLabels {
    val screenTitle: LbcTextSpec
    val sectionTitle: LbcTextSpec
    val sectionMessage: LbcTextSpec
    val label: LbcTextSpec

    data object OnBoarding : CongratulationScreenLabels {
        override val screenTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_congratulationScreen_title)
        override val sectionTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_congratulationScreen_sectionTitle)
        override val sectionMessage: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_congratulationScreen_sectionMessage)
        override val label: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_congratulationScreen_goButton)
    }

    data object MultiSafe : CongratulationScreenLabels {
        override val screenTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_congratulationScreen_title)
        override val sectionTitle: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_congratulationScreen_sectionTitle)
        override val sectionMessage: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_congratulationScreen_sectionMessage)
        override val label: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_congratulationScreen_goButton)
    }
}
