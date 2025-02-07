package studio.lunabee.onesafe.feature.search.holder

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.common.model.item.PlainItemDataRow

@Stable
data class SearchUiState(
    val isLoading: Boolean,
    val itemCount: Int,
    val resultUIState: SearchResultUiState = SearchResultUiState.Idle(SearchData()),
)

@Stable
sealed interface SearchResultUiState {

    @Stable
    data class Idle(
        val searchData: SearchData,
    ) : SearchResultUiState

    @Stable
    data class Searching(
        val result: List<PlainItemDataRow>?,
        val deletedResult: List<PlainItemDataRow>?,
    ) : SearchResultUiState
}
