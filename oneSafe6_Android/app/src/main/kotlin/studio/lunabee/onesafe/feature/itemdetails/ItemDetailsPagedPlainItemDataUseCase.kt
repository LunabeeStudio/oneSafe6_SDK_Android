package studio.lunabee.onesafe.feature.itemdetails

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
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
import java.util.UUID

class ItemDetailsPagedPlainItemDataUseCase @AssistedInject constructor(
    private val getPagerItemByParentsUseCase: GetPagerItemByParentsUseCase,
    itemSettingsRepository: ItemSettingsRepository,
    decryptUseCase: ItemDecryptUseCase,
    getIconUseCase: GetIconUseCase,
    getSafeItemActionHelper: GetSafeItemActionHelper,
    @Assisted private val itemId: UUID,
    @Assisted private val isDeleted: Boolean,
    safeRepository: SafeRepository,
) : PagedPlainItemDataUseCase(
    itemSettingsRepository,
    decryptUseCase,
    getIconUseCase,
    getSafeItemActionHelper,
    safeRepository,
) {
    override fun itemFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItem>> =
        getPagerItemByParentsUseCase.invoke(pagingConfig, itemId, isDeleted)

    override fun itemWithIdentifierFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItemWithIdentifier>> =
        getPagerItemByParentsUseCase.withIdentifier(pagingConfig, itemId, isDeleted)
}

@AssistedFactory
interface ItemDetailsPagedPlainItemDataUseCaseFactory {
    fun create(itemId: UUID, isDeleted: Boolean): ItemDetailsPagedPlainItemDataUseCase
}
