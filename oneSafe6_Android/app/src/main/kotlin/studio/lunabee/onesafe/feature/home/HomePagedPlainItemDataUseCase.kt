package studio.lunabee.onesafe.feature.home

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.GetPagerItemByParentsUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.item.PagedPlainItemDataUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import javax.inject.Inject

class HomePagedPlainItemDataUseCase @Inject constructor(
    private val getPagerItemByParentsUseCase: GetPagerItemByParentsUseCase,
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
        getPagerItemByParentsUseCase.invoke(pagingConfig, null, false)

    override fun itemWithIdentifierFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItemWithIdentifier>> =
        getPagerItemByParentsUseCase.withIdentifier(pagingConfig, null, false)
}
