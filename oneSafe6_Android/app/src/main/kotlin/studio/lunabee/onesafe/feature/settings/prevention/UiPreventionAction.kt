package studio.lunabee.onesafe.feature.settings.prevention

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString

sealed class UiPreventionAction(
    val title: LbcTextSpec,
) {

    abstract val onClick: () -> Unit

    class CreateBackup(
        override val onClick: () -> Unit,
    ) : UiPreventionAction(
        title = LbcTextSpec.StringResource(OSString.settings_warning_bottomsheet_createBackup_action),
    )

    class EnableAutoBackup(
        override val onClick: () -> Unit,
    ) : UiPreventionAction(
        title = LbcTextSpec.StringResource(OSString.settings_warning_bottomsheet_enableAutoBackup_action),
    )

    class EnablePasswordVerification(
        override val onClick: () -> Unit,
    ) : UiPreventionAction(
        title = LbcTextSpec.StringResource(OSString.settings_warning_bottomsheet_passwordVerification_action),
    )
}
