package studio.lunabee.onesafe.atom

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.SelectableChipElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.ui.theme.OSTypography
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall
import studio.lunabee.onesafe.utils.OsDefaultPreview

enum class OSChipType(
    val selectedContainerColor: @Composable () -> Color,
    val labelColor: @Composable () -> Color,
) {
    Default(selectedContainerColor = { LocalColorPalette.current.Primary03 }, labelColor = { LocalColorPalette.current.Neutral80 }),
    New(selectedContainerColor = { LocalColorPalette.current.FeedbackNew10 }, labelColor = { LocalColorPalette.current.Neutral80 }),
    Progress(
        selectedContainerColor = { LocalColorPalette.current.FeedbackProgress10 },
        labelColor = { LocalColorPalette.current.Neutral80 },
    ),
}

enum class OSChipStyle(
    val iconDp: Dp,
    val containerDp: Dp,
    val textStyle: @Composable () -> TextStyle,
) {
    Small(
        OSDimens.Chip.IconSmall,
        OSDimens.Chip.ContainerSmall,
        @Composable { OSTypography.Typography.labelXSmall },
    ),
    Regular(
        OSDimens.Chip.IconRegular,
        OSDimens.Chip.ContainerRegular,
        @Composable { OSTypography.Typography.labelLarge },
    ),
}

@Composable
fun OSInputChip(
    selected: Boolean,
    onClick: (() -> Unit)?,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    type: OSChipType = OSChipType.Default,
    style: OSChipStyle = OSChipStyle.Regular,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    avatar: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(OSDimens.SystemCornerRadius.Regular),
    colors: SelectableChipColors = InputChipDefaults.inputChipColors(
        selectedLabelColor = type.labelColor(),
        selectedLeadingIconColor = type.labelColor(),
        selectedTrailingIconColor = type.labelColor(),
        selectedContainerColor = type.selectedContainerColor(),
    ),
    elevation: SelectableChipElevation? = InputChipDefaults.inputChipElevation(),
    border: BorderStroke? = InputChipDefaults.inputChipBorder(enabled, selected),
    interactionSource: MutableInteractionSource? = if (onClick == null) null else remember { MutableInteractionSource() },
) {
    // LocalTextStyle can't be provide because SelectableChip internally provides labelLarge
    MaterialTheme(
        typography = MaterialTheme.typography.copy(labelLarge = style.textStyle()),
    ) {
        InputChip(
            selected = selected,
            onClick = onClick ?: {},
            label = label,
            modifier = modifier
                .height(style.containerDp),
            enabled = enabled,
            leadingIcon = leadingIcon,
            avatar = avatar,
            trailingIcon = trailingIcon,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            interactionSource = interactionSource ?: object : MutableInteractionSource {
                override val interactions: Flow<Interaction> = emptyFlow()
                override suspend fun emit(interaction: Interaction) {}
                override fun tryEmit(interaction: Interaction): Boolean = true
            },
        )
    }
}

@Composable
fun OSInputChipIcon(
    @DrawableRes icon: Int,
    size: OSChipStyle = OSChipStyle.Regular,
) {
    Icon(painter = painterResource(id = icon), contentDescription = null, modifier = Modifier.size(size.iconDp))
}

@Composable
@OsDefaultPreview
fun OSChipPreview() {
    OSPreviewOnSurfaceTheme {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            listOf({}, null).forEach { onClick ->
                listOf(true, false).forEach { enabled ->
                    listOf(true, false).forEach { selected ->
                        OSChipType.entries.forEach { type ->
                            OSChipStyle.entries.forEach { style ->
                                Text(
                                    "onClick ${
                                        onClick.toString()
                                            .take(4)
                                    }, enabled $enabled, selected $selected, type $type, style $style",
                                )
                                OSInputChip(
                                    onClick = onClick,
                                    selected = selected,
                                    type = type,
                                    enabled = enabled,
                                    label = {
                                        OSText(text = LbcTextSpec.Raw(type.name))
                                    },
                                    leadingIcon = {
                                        OSInputChipIcon(R.drawable.os_ic_sample, style)
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
