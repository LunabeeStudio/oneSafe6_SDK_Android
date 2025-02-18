package studio.lunabee.onesafe.feature.itemdetails

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import studio.lunabee.onesafe.feature.itemdetails.model.ItemDetailsTab
import studio.lunabee.onesafe.feature.itemdetails.model.getTabsData
import studio.lunabee.onesafe.molecule.tabs.OSTabs
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ItemDetailsTabs(
    tabs: LinkedHashSet<ItemDetailsTab>,
    selectedTab: ItemDetailsTab,
    elementsCount: Int,
    modifier: Modifier = Modifier,
    onTabSelected: (ItemDetailsTab) -> Unit,
) {
    OSTabs(
        data = tabs.getTabsData(elementsCount),
        selectedTabIndex = tabs.indexOf(selectedTab),
        modifier = modifier,
        onTabSelected = { idx -> onTabSelected(tabs.elementAt(idx)) },
    )
}

@Composable
@OsDefaultPreview
@Suppress("SpreadOperator")
private fun ItemDetailsFullTabsPreview() {
    OSPreviewOnSurfaceTheme {
        ItemDetailsTabs(
            tabs = linkedSetOf(*ItemDetailsTab.entries.toTypedArray()),
            selectedTab = ItemDetailsTab.More,
            elementsCount = 10,
        ) {}
    }
}

@Composable
@OsDefaultPreview
private fun ItemDetailsTwoTabsPreview() {
    OSPreviewOnSurfaceTheme {
        ItemDetailsTabs(
            tabs = linkedSetOf(ItemDetailsTab.Elements, ItemDetailsTab.More),
            selectedTab = ItemDetailsTab.Elements,
            elementsCount = 10,
        ) {}
    }
}
