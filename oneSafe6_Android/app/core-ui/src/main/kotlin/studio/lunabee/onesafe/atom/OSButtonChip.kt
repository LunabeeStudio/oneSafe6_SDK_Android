package studio.lunabee.onesafe.atom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

/**
 * A chip to be used as a button
 */
@Composable
fun OSButtonChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    type: OSChipType = OSChipType.Default,
    style: OSChipStyle = OSChipStyle.Regular,
    enabled: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(OSDimens.SystemCornerRadius.Regular),
    colors: ChipColors = SuggestionChipDefaults.suggestionChipColors(
        labelColor = type.labelColor(),
        iconContentColor = type.labelColor(),
        containerColor = type.selectedContainerColor(),
    ),
    elevation: ChipElevation? = SuggestionChipDefaults.suggestionChipElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    // LocalTextStyle can't be provide because ChipContent internally provides labelLarge
    MaterialTheme(
        typography = MaterialTheme.typography.copy(labelLarge = style.textStyle()),
    ) {
        Box(
            modifier = modifier
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .minimumInteractiveComponentSize(),
        ) {
            SuggestionChip(
                onClick = onClick,
                label = label,
                modifier = Modifier
                    .align(Alignment.Center)
                    .height(style.containerDp),
                enabled = enabled,
                icon = icon,
                shape = shape,
                colors = colors,
                elevation = elevation,
                border = border,
                interactionSource = interactionSource,
            )
        }
    }
}

@Composable
@OsDefaultPreview
private fun OSButtonChipPreview() {
    OSPreviewOnSurfaceTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf(true, false).forEach { enabled ->
                OSChipType.entries.forEach { type ->
                    OSChipStyle.entries.forEach { style ->
                        Text(
                            "enabled $enabled, type $type, style $style",
                        )
                        OSButtonChip(
                            onClick = {},
                            type = type,
                            enabled = enabled,
                            label = {
                                OSText(text = LbcTextSpec.Raw(type.name))
                            },
                            icon = {
                                OSInputChipIcon(R.drawable.os_ic_sample, style)
                            },
                        )
                    }
                }
            }
        }
    }
}
