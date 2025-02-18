package studio.lunabee.onesafe.commonui.settings

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.chip.FeatureBetaInputChip
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.res.OSDimens

abstract class BetaCardSettingsAction(
    @DrawableRes icon: Int?,
    text: LbcTextSpec,
) : CardSettingsNavAction(icon, text) {

    @Composable
    override fun Label(modifier: Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Texts(state = OSActionState.Enabled, modifier = Modifier.weight(1f))
            FeatureBetaInputChip()
        }
    }
}
