package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSClickableRowText
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.coreui.R
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme

/**
 * @see <a href="https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?node-id=5325-77180&t=jxPWOo3zgBjUiFZg
 * -4">Option row component</a>
 */
@Composable
fun OSOptionRow(
    text: LbcTextSpec,
    onSelect: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    description: LbcTextSpec? = null,
    descriptionMaxLines: Int? = Int.MAX_VALUE,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val buttonColors = if (isSelected) {
        OSTextButtonDefaults.primaryTextButtonColors(state = OSActionState.Enabled)
    } else {
        OSTextButtonDefaults.textButtonColors(color = LocalDesignSystem.current.rowTextColor)
    }
    OSClickableRow(
        label = { labelModifier ->
            Column(modifier = labelModifier.height(IntrinsicSize.Min)) {
                OSClickableRowText(text = text, modifier = labelModifier)
                description?.let {
                    OSClickableRowText(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalDesignSystem.current.rowSecondaryColor.takeUnless { isSelected } ?: Color.Unspecified,
                        maxLines = descriptionMaxLines,
                    )
                }
            }
        },
        onClick = onSelect,
        modifier = modifier
            .selectable(selected = isSelected, onClick = onSelect),
        buttonColors = buttonColors,
        trailingIcon = if (isSelected) {
            { OSImage(image = OSImageSpec.Drawable(R.drawable.os_ic_check)) }
        } else {
            null
        },
        leadingIcon = leadingIcon,
    )
}

@Preview
@Composable
fun OSOptionRowPreview() {
    OSPreviewOnSurfaceTheme {
        var selected by remember { mutableStateOf(true to false) }
        Column {
            OSOptionRow(
                text = LbcTextSpec.Raw("Option 1"),
                onSelect = { selected = selected.copy(first = !selected.first) },
                isSelected = selected.first,
            )
            OSOptionRow(
                text = LbcTextSpec.Raw("Option 2"),
                onSelect = { selected = selected.copy(second = !selected.second) },
                isSelected = selected.second,
            )
            OSOptionRow(
                text = LbcTextSpec.Raw("Option 3"),
                description = loremIpsumSpec(15),
                onSelect = { selected = selected.copy(first = !selected.first) },
                isSelected = selected.first,
            )
            OSOptionRow(
                text = LbcTextSpec.Raw("Option 4"),
                description = loremIpsumSpec(15),
                onSelect = { selected = selected.copy(first = !selected.first) },
                isSelected = selected.second,
            )
        }
    }
}
