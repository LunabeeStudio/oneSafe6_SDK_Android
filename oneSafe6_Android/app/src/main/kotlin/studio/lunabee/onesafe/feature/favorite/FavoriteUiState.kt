package studio.lunabee.onesafe.feature.favorite

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import studio.lunabee.onesafe.common.model.item.PlainItemData

@Stable
class FavoriteUiState(
    val items: Flow<PagingData<PlainItemData>>,
    val itemsCount: Int,
) {
    companion object {
        fun initializing(): FavoriteUiState = FavoriteUiState(
            items = emptyFlow(),
            itemsCount = 0,
        )
    }
}
