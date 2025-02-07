package studio.lunabee.onesafe.feature.itemform.model.uifield

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconAlertDecorationButton
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem

@Composable
fun FieldActionMenu(
    isMenuExpended: Boolean,
    onDismiss: () -> Unit,
    actions: List<ItemFieldActions>,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = isMenuExpended,
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        actions.forEachIndexed { index, fieldAction ->
            OSClickableRow(
                text = fieldAction.text,
                onClick = fieldAction.onClick,
                leadingIcon = {
                    when (fieldAction.type) {
                        ItemFieldActions.Type.Normal -> OSIconDecorationButton(OSImageSpec.Drawable(drawable = fieldAction.icon))
                        ItemFieldActions.Type.Dangerous -> OSIconAlertDecorationButton(OSImageSpec.Drawable(drawable = fieldAction.icon))
                    }
                },
                contentPadding = LocalDesignSystem.current.getRowClickablePaddingValuesDependingOnIndex(
                    index = index,
                    elementsCount = actions.size,
                ),
                buttonColors = when (fieldAction.type) {
                    ItemFieldActions.Type.Normal -> OSTextButtonDefaults.secondaryTextButtonColors(state = OSActionState.Enabled)
                    ItemFieldActions.Type.Dangerous -> OSTextButtonDefaults.secondaryAlertTextButtonColors(state = OSActionState.Enabled)
                },
            )
        }
    }
}
