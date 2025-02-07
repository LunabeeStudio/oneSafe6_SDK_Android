package studio.lunabee.onesafe.molecule

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

/**
 * https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System-%26-Android?node-id=225%3A8778
 */
@Composable
fun OSNavigationItem(
    text: LbcTextSpec,
    onClick: () -> Unit,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    actionState: OSActionState = OSActionState.Enabled,
) {
    OSTextButton(
        text = text,
        onClick = onClick,
        buttonColors = OSTextButtonDefaults.textButtonColors(
            color = LocalDesignSystem.current.navigationItemLabelColor(isActive),
            state = actionState,
        ),
        state = actionState,
        modifier = modifier,
    )
}

@OsDefaultPreview
@Composable
private fun OSNavigationItemActivePreview() {
    OSTheme {
        OSNavigationItem(
            text = loremIpsumSpec(2),
            onClick = { },
            isActive = true,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSNavigationItemInactivePreview() {
    OSTheme {
        OSNavigationItem(
            text = loremIpsumSpec(2),
            onClick = { },
            isActive = false,
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSNavigationItemLongTextPreview() {
    OSTheme {
        OSNavigationItem(
            text = loremIpsumSpec(20),
            onClick = { },
            isActive = true,
        )
    }
}
