package studio.lunabee.onesafe.feature.favorite

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetPagerItemFavoriteUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.item.PagedPlainItemDataUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import javax.inject.Inject

class FavoritePagedPlainItemDataUseCase @Inject constructor(
    private val getPagerItemFavoriteUseCase: GetPagerItemFavoriteUseCase,
    itemSettingsRepository: ItemSettingsRepository,
    decryptUseCase: ItemDecryptUseCase,
    getIconUseCase: GetIconUseCase,
    getSafeItemActionHelper: GetSafeItemActionHelper,
    safeRepository: SafeRepository,
) : PagedPlainItemDataUseCase(
    itemSettingsRepository,
    decryptUseCase,
    getIconUseCase,
    getSafeItemActionHelper,
    safeRepository,
) {
    override fun itemFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItem>> =
        getPagerItemFavoriteUseCase.invoke(pagingConfig)

    override fun itemWithIdentifierFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItemWithIdentifier>> =
        getPagerItemFavoriteUseCase.withIdentifier(pagingConfig)
}
