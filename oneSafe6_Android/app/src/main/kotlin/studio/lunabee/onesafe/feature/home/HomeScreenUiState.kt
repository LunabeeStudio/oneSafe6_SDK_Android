package studio.lunabee.onesafe.feature.home

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.feature.home.model.ItemRowData

@Stable
data class HomeScreenUiState(
    val items: Flow<PagingData<PlainItemData>>,
    val initialItemCount: Int,
    val favoriteItems: Flow<ImmutableList<ItemRowData>>,
    val deletedItemCount: Int,
    val isBiometricEnabled: Boolean,
    val isLanguageGenerated: Boolean,
    val showFavoritesSeeAll: Boolean,
    val isAppBetaVersion: Boolean,
    val shouldVerifyPassword: Boolean,
    val hasDoneOnBoardingBubbles: Boolean,
    val isSafeReady: Boolean,
) {
    companion object {
        fun initializing(): HomeScreenUiState = HomeScreenUiState(
            items = emptyFlow(),
            initialItemCount = 0,
            favoriteItems = flowOf(InitialLoadingFavoritesList),
            deletedItemCount = 0,
            isBiometricEnabled = false,
            isLanguageGenerated = false,
            showFavoritesSeeAll = false,
            isAppBetaVersion = false,
            shouldVerifyPassword = false,
            hasDoneOnBoardingBubbles = false,
            isSafeReady = false,
        )

        val InitialLoadingFavoritesList: ImmutableList<ItemRowData> =
            List(AppConstants.Ui.HomeFavorite.MaxShowAmount) { ItemRowData.Loading }.toImmutableList()
    }
}
