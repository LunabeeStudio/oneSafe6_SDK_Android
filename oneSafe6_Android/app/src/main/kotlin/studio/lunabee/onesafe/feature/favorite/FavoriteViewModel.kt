package studio.lunabee.onesafe.feature.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.domain.usecase.item.CountAllFavoriteUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionUiStateDelegate
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    countAllFavoriteUseCase: CountAllFavoriteUseCase,
    private val getSafeItemActionHelper: GetSafeItemActionHelper,
    pagedPlainItemDataUseCase: FavoritePagedPlainItemDataUseCase,
) : ViewModel(), GetSafeItemActionUiStateDelegate by getSafeItemActionHelper {
    val uiState: StateFlow<FavoriteUiState>

    init {
        val items: Flow<PagingData<PlainItemData>> = pagedPlainItemDataUseCase()
            .cachedIn(viewModelScope)

        uiState = countAllFavoriteUseCase().map { count ->
            FavoriteUiState(
                items = items,
                itemsCount = count,
            )
        }.stateIn(
            viewModelScope,
            CommonUiConstants.Flow.DefaultSharingStarted,
            FavoriteUiState.initializing(),
        )
    }

    override fun onCleared() {
        super.onCleared()
        getSafeItemActionHelper.close()
    }
}
