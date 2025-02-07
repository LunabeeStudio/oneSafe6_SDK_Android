package studio.lunabee.onesafe.feature.bin

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.accessibility.rememberOSAccessibilityState
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.feature.bin.model.BinGlobalAction
import studio.lunabee.onesafe.model.OSTopAppBarOption
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun BinTopBar(
    onBackClick: (() -> Unit),
    actions: LinkedHashSet<BinGlobalAction>?,
    modifier: Modifier = Modifier,
) {
    val options = mutableListOf<OSTopAppBarOption>(
        topAppBarOptionNavBack(onBackClick),
    )
    if (!actions.isNullOrEmpty()) {
        options +=
            TopAppBarOptionTrailing {
                var expanded by remember {
                    mutableStateOf(false)
                }
                Box {
                    OSIconButton(
                        image = OSImageSpec.Drawable(OSDrawable.ic_menu),
                        contentDescription = LbcTextSpec.StringResource(
                            OSString.bin_topBar_menu_accessibility_description,
                        ),
                        buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                        onClick = { expanded = !expanded },
                        colors = OSIconButtonDefaults.secondaryIconButtonColors(),
                        modifier = Modifier
                            .testTag(tag = UiConstants.TestTag.OSAppBarMenu),
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        actions.forEach { action ->
                            OSDropdownMenuItem(
                                text = action.text,
                                icon = action.icon,
                                onClick = {
                                    action.onClick()
                                    expanded = false
                                },
                            )
                        }
                        val accessibilityState = rememberOSAccessibilityState()
                        if (accessibilityState.isAccessibilityEnabled) {
                            OSDropdownMenuItem(
                                text = LbcTextSpec.StringResource(id = OSString.common_accessibility_popup_dismiss),
                                icon = null,
                                onClick = {
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
    }

    OSTopAppBar(
        title = LbcTextSpec.StringResource(OSString.common_bin),
        options = options,
        modifier = modifier,
    )
}

@OsDefaultPreview
@Composable
private fun BinTopBarPreview() {
    OSTheme {
        BinTopBar(
            onBackClick = {},
            actions = linkedSetOf(),
        )
    }
}
