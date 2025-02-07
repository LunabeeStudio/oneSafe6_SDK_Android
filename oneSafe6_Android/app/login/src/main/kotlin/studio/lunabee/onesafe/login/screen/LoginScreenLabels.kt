package studio.lunabee.onesafe.login.screen

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

interface LoginScreenLabels {
    val title: LbcTextSpec
    val message: LbcTextSpec?

    object FirstTime : LoginScreenLabels {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_firstTime_welcome)
        override val message: LbcTextSpec = LbcTextSpec.StringResource(OSString.login_firstLogin_message)
    }

    object Accustomed : LoginScreenLabels {
        override val title: LbcTextSpec = LbcTextSpec.StringResource(OSString.signInScreen_welcome)
        override val message: LbcTextSpec? = null
    }
}
