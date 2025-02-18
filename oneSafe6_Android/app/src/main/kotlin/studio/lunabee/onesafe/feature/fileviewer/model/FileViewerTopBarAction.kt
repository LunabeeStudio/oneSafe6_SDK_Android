package studio.lunabee.onesafe.feature.fileviewer.model

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSClickableRow
import studio.lunabee.onesafe.atom.OSIconDecorationButton
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.feature.itemdetails.model.FileFieldAction
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

fun fileViewerTopBarAction(
    actions: List<FileFieldAction>,
): TopAppBarOptionTrailing {
    return TopAppBarOptionTrailing {
        var isMenuExpended by remember { mutableStateOf(false) }
        Box {
            OSIconButton(
                image = OSImageSpec.Drawable(OSDrawable.ic_menu),
                contentDescription = LbcTextSpec.StringResource(
                    OSString.accessibility_field_displayActions,
                ),
                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                onClick = { isMenuExpended = !isMenuExpended },
                colors = OSIconButtonDefaults.secondaryIconButtonColors(),
                modifier = Modifier
                    .testTag(tag = UiConstants.TestTag.OSAppBarMenu),
            )
            DropdownMenu(
                expanded = isMenuExpended,
                onDismissRequest = { isMenuExpended = false },
            ) {
                actions.forEach { action ->
                    OSClickableRow(
                        onClick = {
                            action.onClick()
                            isMenuExpended = false
                        },
                        text = action.text,
                        leadingIcon = {
                            OSIconDecorationButton(image = OSImageSpec.Drawable(action.icon))
                        },
                        buttonColors = OSTextButtonDefaults.secondaryTextButtonColors(OSActionState.Enabled),
                    )
                }
            }
        }
    }
}
