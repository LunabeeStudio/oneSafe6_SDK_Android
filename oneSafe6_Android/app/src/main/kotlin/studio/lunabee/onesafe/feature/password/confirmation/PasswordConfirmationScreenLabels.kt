package studio.lunabee.onesafe.feature.password.confirmation

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed interface PasswordConfirmationScreenLabels {
    val loading: LbcTextSpec?
    val title: LbcTextSpec
    val description: LbcTextSpec?
    val fieldLabel: LbcTextSpec

    data object Onboarding : PasswordConfirmationScreenLabels {
        override val loading: LbcTextSpec? = null
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.onboarding_passwordConfirmationScreen_sectionTitle)
        override val description: LbcTextSpec? = null
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object ChangePassword : PasswordConfirmationScreenLabels {
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.changePassword_passwordConfirmationScreen_loading)
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.onboarding_passwordConfirmationScreen_sectionTitle)
        override val description: LbcTextSpec? = null
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object MultiSafe : PasswordConfirmationScreenLabels {
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_passwordConfirmationScreen_loading)
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_passwordConfirmationScreen_sectionTitle)
        override val description: LbcTextSpec? = null
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object VerifyPassword : PasswordConfirmationScreenLabels {
        override val loading: LbcTextSpec? = null
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.verifyPassword_inputCard_title)
        override val description: LbcTextSpec = LbcTextSpec.StringResource(OSString.verifyPassword_inputCard_description)
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.onBoarding_passwordCreationScreen_passwordLabel)
    }

    data object AutoDestruction : PasswordConfirmationScreenLabels {
        override val loading: LbcTextSpec = LbcTextSpec.StringResource(OSString.multiSafe_passwordConfirmationScreen_loading)
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.autodestruction_passwordConfirmation_description)
        override val description: LbcTextSpec? = null
        override val fieldLabel: LbcTextSpec = LbcTextSpec.StringResource(OSString.fieldName_password)
    }
}
