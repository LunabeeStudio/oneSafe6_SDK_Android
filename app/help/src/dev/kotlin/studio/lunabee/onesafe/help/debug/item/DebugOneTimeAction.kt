package studio.lunabee.onesafe.help.debug.item

import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.molecule.OSDropdownMenuItem

@Composable
internal fun DebugOneTimeAction(
    modifier: Modifier,
    data: DebugOneTimeActionData,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Button(
        onClick = {
            isExpanded = true
        },
        modifier = modifier,
    ) {
        OSText(LbcTextSpec.Raw("\uD83D\uDCE2 Reset one time stuff"))
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
        ) {
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Show support oS6 card"), null) {
                data.forceShowSupportOs()
                isExpanded = false
                data.closeDrawer()
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Show enable backup CTA"), null) {
                data.resetBackupCta()
                isExpanded = false
                data.closeDrawer()
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Reset oSK onboarding"), null) {
                data.resetOSKOnboarding()
                isExpanded = false
                data.closeDrawer()
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Reset oSK tutorial"), null) {
                data.resetOSKTutorial()
                isExpanded = false
                data.closeDrawer()
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Reset Tips camera"), null) {
                data.resetCameraTips()
                isExpanded = false
                data.closeDrawer()
            }
            OSDropdownMenuItem(text = LbcTextSpec.Raw("Reset ToolTips"), null) {
                data.resetTips()
                isExpanded = false
                data.closeDrawer()
            }
        }
    }
}
