package studio.lunabee.onesafe.feature.password.creation

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed interface PasswordCreationScreenLabels {
    val title: LbcTextSpec
    val description: LbcTextSpec?
    val loading: LbcTextSpec
    val fieldLabel: LbcTextSpec

    data object Onboarding : PasswordCreationScreenLabels {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_sectionTitle)
        override val description: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_message)
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_loadingLabel)
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object MultiSafe : PasswordCreationScreenLabels {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafeOnBoarding_passwordCreationScreen_sectionTitle)
        override val description: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafeOnBoarding_passwordCreationScreen_message)
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_loadingLabel)
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object ChangePassword : PasswordCreationScreenLabels {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.changePassword_passwordCreationScreen_sectionTitle)
        override val description: LbcTextSpec? = null
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_loadingLabel)
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object AutoDestruction : PasswordCreationScreenLabels {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.autodestruction_passwordCreation_description)
        override val description: LbcTextSpec? = null
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.common_loading)
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.fieldName_password)
    }
}
