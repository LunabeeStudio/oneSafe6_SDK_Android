package studio.lunabee.onesafe.common.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.ColorPickerController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.bottomsheet.BottomSheetHolderColumnContent
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ColorPickerBottomSheetContent(
    paddingValues: PaddingValues,
    currentSelectedColor: Color,
    onColorPicked: (color: Color) -> Unit,
    onValidate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val initialColor = remember { currentSelectedColor }
    BottomSheetHolderColumnContent(
        paddingValues = paddingValues,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            OSText(
                text = LbcTextSpec.StringResource(id = OSString.itemCreation_colorChoice_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .align(alignment = Alignment.Center),
            )

            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_done),
                onClick = onValidate,
                contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_done),
                colors = OSIconButtonDefaults.secondaryIconButtonColors(),
                modifier = Modifier
                    .align(alignment = Alignment.TopEnd)
                    .testTag(tag = UiConstants.TestTag.Item.SaveColorButton),
            )
        }

        OSRegularSpacer()

        val controller: ColorPickerController = rememberColorPickerController()

        HsvColorPicker(
            modifier = Modifier
                .size(size = OSDimens.ColorPicker.size)
                .testTag(tag = UiConstants.TestTag.Item.ColorPicker),
            controller = controller,
            onColorChanged = { colorEnvelope: ColorEnvelope ->
                if (colorEnvelope.fromUser) {
                    onColorPicked(colorEnvelope.color)
                }
            },
            initialColor = initialColor,
        )
    }
}

@OsDefaultPreview
@Composable
private fun ColorPickerBottomSheetContentPreview() {
    OSTheme {
        ColorPickerBottomSheetContent(
            paddingValues = PaddingValues(0.dp),
            currentSelectedColor = MaterialTheme.colorScheme.primary,
            onColorPicked = { },
            onValidate = { },
        )
    }
}
