package studio.lunabee.onesafe.feature.autofill.itemlist

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow

@Stable
interface AfItemListScreenUiState {
    object Idle : AfItemListScreenUiState

    data class Exit(
        val identifier: String,
        val password: String,
    ) : AfItemListScreenUiState

    data class Data(
        val isLoading: Boolean,
        val itemCount: Int,
    ) : AfItemListScreenUiState
}

@Stable
interface AfItemDataState {
    object Idle : AfItemDataState
    object NoItem : AfItemDataState
    data class AllItems(
        val suggestedItems: List<PlainItemDataRow>,
        val items: Flow<PagingData<PlainItemDataRow>>,
    ) : AfItemDataState

    data class Searching(
        val items: List<PlainItemDataRow>,
        val allItemsCount: Int,
    ) : AfItemDataState
}
