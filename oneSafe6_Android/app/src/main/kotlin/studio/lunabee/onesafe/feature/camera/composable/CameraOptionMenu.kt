package studio.lunabee.onesafe.feature.camera.composable

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.PopupProperties
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.feature.camera.model.CameraOption
import studio.lunabee.onesafe.model.OSActionState

@Composable
fun CameraOptionMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    selectedOption: CameraOption,
    options: List<CameraOption>,
    onSelectedOption: (CameraOption) -> Unit,
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        properties = PopupProperties(
            focusable = true,
            clippingEnabled = true,
        ),
    ) {
        options.forEach { option ->
            val state = remember(selectedOption) {
                if (selectedOption == option) {
                    OSActionState.Disabled
                } else {
                    OSActionState.Enabled
                }
            }
            OSClickableRow(
                onClick = {
                    onSelectedOption(option)
                    onDismiss()
                },
                text = option.title,
                state = state,
                leadingIcon = { OSIconDecorationButton(image = OSImageSpec.Drawable(option.drawableRes)) },
                buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(state = state),
            )
        }
    }
}
