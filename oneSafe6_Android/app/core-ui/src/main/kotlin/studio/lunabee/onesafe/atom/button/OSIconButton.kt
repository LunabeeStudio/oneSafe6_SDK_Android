package studio.lunabee.onesafe.atom.button

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.compose.foundation.haptic.LbcHapticFeedback
import studio.lunabee.compose.foundation.haptic.rememberLbcHapticFeedback
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.OSHapticEffect
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun OSIconButton(
    image: OSImageSpec,
    onClick: () -> Unit,
    contentDescription: LbcTextSpec?,
    modifier: Modifier = Modifier,
    buttonSize: OSDimens.SystemButtonDimension = OSDimens.SystemButtonDimension.Regular,
    state: OSActionState = OSActionState.Enabled,
    colors: ButtonColors = OSIconButtonDefaults.primaryIconButtonColors(state = state),
    hapticEffect: OSHapticEffect? = OSHapticEffect.Primary.takeIf { colors.containerColor == MaterialTheme.colorScheme.primary },
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val hapticFeedback: LbcHapticFeedback = rememberLbcHapticFeedback()
    // We can't use an [IconButton] as size is limited for this type of Composable.
    OutlinedButton(
        onClick = {
            onClick()
            hapticEffect?.perform(hapticFeedback)
        },
        modifier = modifier
            .size(size = buttonSize.container.dp),
        colors = colors,
        border = BorderStroke(width = 0.dp, color = Color.Transparent), // Remove default border
        contentPadding = PaddingValues(all = 0.dp), // remove default inner padding of the button.
        shape = CircleShape,
        enabled = state.enabled,
        interactionSource = interactionSource,
    ) {
        OSImage(
            image = image,
            modifier = Modifier
                .size(size = buttonSize.image.dp),
            contentDescription = contentDescription,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSIconButtonPreview() {
    OSPreviewBackgroundTheme {
        Column {
            val buttonColors = listOf(
                OSIconButtonDefaults.primaryIconButtonColors(),
                OSIconButtonDefaults.secondaryIconButtonColors(),
                OSIconButtonDefaults.tertiaryIconButtonColors(),
            )
            OSDimens.SystemButtonDimension.entries.forEach { dimension ->
                buttonColors.forEach { colors ->
                    OSIconButton(
                        image = drawableSample,
                        onClick = { },
                        contentDescription = null,
                        buttonSize = dimension,
                        colors = colors,
                    )
                }
            }
        }
    }
}
