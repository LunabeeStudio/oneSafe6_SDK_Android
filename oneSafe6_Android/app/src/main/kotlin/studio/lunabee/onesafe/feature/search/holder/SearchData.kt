package studio.lunabee.onesafe.feature.search.holder

import studio.lunabee.onesafe.common.model.item.PlainItemData

data class SearchData(
    val recentItem: List<PlainItemData> = listOf(),
    val recentSearch: List<String> = listOf(),
)
