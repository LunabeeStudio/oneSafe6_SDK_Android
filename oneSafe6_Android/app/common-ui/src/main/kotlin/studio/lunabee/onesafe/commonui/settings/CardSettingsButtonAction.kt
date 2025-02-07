package studio.lunabee.onesafe.commonui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.molecule.OSActionButton
import studio.lunabee.onesafe.molecule.OSActionButtonStyle

open class CardSettingsButtonAction(
    private val onClick: () -> Unit,
    private val icon: Int,
    private val text: LbcTextSpec,
    private val isDangerous: Boolean = false,
) {
    fun settingsAction(contentPadding: PaddingValues): SettingsAction = object : SettingsAction {
        @Composable
        override fun Composable() {
            val style = if (isDangerous) {
                OSActionButtonStyle.Destructive
            } else {
                OSActionButtonStyle.Default
            }
            OSActionButton(
                text = text,
                onClick = onClick,
                contentPadding = contentPadding,
                startIcon = OSImageSpec.Drawable(icon),
                style = style,
            ).Composable()
        }
    }
}
