package studio.lunabee.onesafe.commonui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.model.OSSwitchState
import studio.lunabee.onesafe.molecule.OSSwitchRow
import studio.lunabee.onesafe.ui.res.OSDimens

class SwitchSettingAction(
    val label: LbcTextSpec,
    val isChecked: OSSwitchState,
    val description: LbcTextSpec? = null,
    val onValueChange: (OSSwitchState) -> Unit,
) : SettingsAction {

    constructor(
        label: LbcTextSpec,
        isChecked: Boolean,
        description: LbcTextSpec? = null,
        onValueChange: (Boolean) -> Unit,
    ) : this(label, OSSwitchState.fromChecked(isChecked), description, { onValueChange(it.checked) })

    @Composable
    override fun Composable() {
        OSSwitchRow(
            state = isChecked,
            onStateChange = onValueChange,
            label = label,
            description = description,
            modifier = Modifier
                .padding(vertical = OSDimens.SystemSpacing.Regular)
                .padding(start = OSDimens.SystemSpacing.Regular, end = OSDimens.SystemSpacing.Small),
        )
    }
}
