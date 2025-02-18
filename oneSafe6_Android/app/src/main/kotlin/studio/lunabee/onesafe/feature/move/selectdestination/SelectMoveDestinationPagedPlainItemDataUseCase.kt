package studio.lunabee.onesafe.feature.move.selectdestination

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
import java.util.UUID

class SelectMoveDestinationPagedPlainItemDataUseCase @AssistedInject constructor(
    private val getPagerItemByParentsUseCase: GetPagerItemByParentsUseCase,
    itemSettingsRepository: ItemSettingsRepository,
    decryptUseCase: ItemDecryptUseCase,
    getIconUseCase: GetIconUseCase,
    @Assisted val parentId: UUID?,
    safeRepository: SafeRepository,
) : PagedPlainItemDataUseCase(
    itemSettingsRepository,
    decryptUseCase,
    getIconUseCase,
    null,
    safeRepository,
) {
    override fun itemFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItem>> =
        getPagerItemByParentsUseCase.invoke(pagingConfig, parentId, false)

    override fun itemWithIdentifierFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItemWithIdentifier>> =
        getPagerItemByParentsUseCase.withIdentifier(pagingConfig, parentId, false)
}

@AssistedFactory
interface SelectMoveDestinationPagedPlainItemDataUseCaseFactory {
    fun create(itemId: UUID?): SelectMoveDestinationPagedPlainItemDataUseCase
}
