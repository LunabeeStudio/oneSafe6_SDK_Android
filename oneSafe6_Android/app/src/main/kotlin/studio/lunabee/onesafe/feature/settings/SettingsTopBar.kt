package studio.lunabee.onesafe.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.tabs.OSTabs
import studio.lunabee.onesafe.molecule.tabs.TabsData
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun SettingsTopBar(
    currentPage: Int,
    onTabSelected: (Int) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Small),
    ) {
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.settings_title),
            modifier = modifier,
            options = listOf(topAppBarOptionNavBack(onBackClick)),
        )
        OSTabs(
            data = SettingsTab.entries.map { tab ->
                TabsData(
                    title = tab.title,
                    contentDescription = null,
                )
            },
            selectedTabIndex = currentPage,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = OSDimens.SystemSpacing.Small),
            onTabSelected = onTabSelected,
        )
    }
}

@OsDefaultPreview
@Composable
private fun SettingsTopBarPreview() {
    OSTheme {
        SettingsTopBar(
            currentPage = 0,
            onTabSelected = {},
            onBackClick = {},
        )
    }
}
