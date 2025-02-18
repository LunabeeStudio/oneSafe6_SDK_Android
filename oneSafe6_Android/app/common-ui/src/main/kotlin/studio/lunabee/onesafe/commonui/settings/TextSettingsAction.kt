package studio.lunabee.onesafe.commonui.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.ui.res.OSDimens

sealed class TextSettingsAction(
    val text: LbcTextSpec,
    val maxLines: Int = 1,
) : SettingsAction {
    @Composable
    override fun Composable() {
        OSText(
            text = text,
            modifier = Modifier.padding(
                top = OSDimens.SystemSpacing.Regular,
                bottom = OSDimens.SystemSpacing.Regular,
                start = OSDimens.SystemSpacing.Regular,
                end = OSDimens.SystemSpacing.ExtraSmall,
            ),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = maxLines,
        )
    }

    class AutofillEnableLabel : TextSettingsAction(
        text = LbcTextSpec.StringResource(OSString.extension_autofillCard_enableLabel),
        maxLines = 2,
    )
}
