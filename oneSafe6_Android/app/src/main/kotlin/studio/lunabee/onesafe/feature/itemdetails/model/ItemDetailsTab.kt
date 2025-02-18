package studio.lunabee.onesafe.feature.itemdetails.model

import androidx.compose.runtime.Composable
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.molecule.tabs.TabsData

enum class ItemDetailsTab {
    Information,
    Elements,
    More,
}

@Composable
fun Collection<ItemDetailsTab>.getTabsData(
    elementsCount: Int,
): List<TabsData> = map { detailsTab ->
    when (detailsTab) {
        ItemDetailsTab.Information -> TabsData(
            title = LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_tab_informations),
            contentDescription = null,
        )
        ItemDetailsTab.More -> TabsData(
            title = LbcTextSpec.StringResource(id = OSString.safeItemDetail_contentCard_tab_more),
            contentDescription = null,
        )
        ItemDetailsTab.Elements -> TabsData(
            title = LbcTextSpec.PluralsResource(
                id = OSPlurals.safeItemDetail_contentCard_tab_elements,
                count = elementsCount,
                elementsCount,
            ),
            contentDescription = LbcTextSpec.PluralsResource(
                id = OSPlurals.safeItemDetail_contentCard_tab_elements_accessibility,
                count = elementsCount,
                elementsCount,
            ),
        )
    }
}
