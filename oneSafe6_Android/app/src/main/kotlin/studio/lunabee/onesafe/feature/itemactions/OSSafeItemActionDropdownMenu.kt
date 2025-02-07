package studio.lunabee.onesafe.feature.itemactions

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.feature.itemdetails.model.SafeItemAction
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.UiConstants

@Composable
fun OSSafeItemActionDropdownMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actions: List<SafeItemAction>,
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(
            UiConstants.TestTag.Item.QuickItemActionMenu,
        ),
    ) {
        actions.sortedBy { it.type == SafeItemAction.Type.Dangerous }.forEach { action ->
            OSClickableRow(
                onClick = {
                    action.onClick()
                    onDismiss()
                },
                text = action.text,
                leadingIcon = {
                    when (action.type) {
                        SafeItemAction.Type.Normal -> OSIconDecorationButton(image = OSImageSpec.Drawable(action.icon))
                        SafeItemAction.Type.Dangerous -> OSIconAlertDecorationButton(image = OSImageSpec.Drawable(action.icon))
                    }
                },
                buttonColors = when (action.type) {
                    SafeItemAction.Type.Normal -> OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled)
                    SafeItemAction.Type.Dangerous -> OSTextButtonDefaults.secondaryAlertTextButtonColors(state = OSActionState.Enabled)
                },
            )
        }
    }
}
