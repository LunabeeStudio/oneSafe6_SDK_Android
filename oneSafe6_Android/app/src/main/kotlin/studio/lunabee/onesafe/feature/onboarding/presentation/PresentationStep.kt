package studio.lunabee.onesafe.feature.onboarding.presentation

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.model.OSActionState

data class PresentationStep(
    val title: LbcTextSpec?,
    val description: LbcTextSpec?,
    @DrawableRes val imageRes: Int,
    val actions: List<PresentationAction> = listOf(),
)

data class PresentationAction(
    val label: LbcTextSpec,
    val action: () -> Unit,
    val attributes: PresentationActionAttributes = PresentationActionAttributes(),
)

class PresentationActionAttributes {
    var state: OSActionState = OSActionState.Enabled
        private set
    var leadingIcon: (@Composable () -> Unit)? = null
        private set
    var filled: Boolean = true
    var fillMaxWidth: Boolean = false

    fun disabled(): PresentationActionAttributes =
        apply {
            state = OSActionState.Disabled
        }

    fun leadingIcon(icon: OSImageSpec): PresentationActionAttributes =
        apply {
            leadingIcon = { OSImage(image = icon) }
        }

    fun notFilled(): PresentationActionAttributes = apply { filled = false }

    fun fillMaxWidth(): PresentationActionAttributes = apply { fillMaxWidth = true }
}
