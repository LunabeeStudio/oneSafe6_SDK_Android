package studio.lunabee.onesafe.feature.item

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.common.model.item.toPlainItemDataDefault
import studio.lunabee.onesafe.common.model.item.toPlainItemDataRow
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemWithIdentifier
import studio.lunabee.onesafe.domain.repository.ItemSettingsRepository
import studio.lunabee.onesafe.domain.repository.SafeRepository
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.itemactions.GetSafeItemActionHelper
import studio.lunabee.onesafe.jvm.mapPagingValues

/**
 * Map paged flow from [itemFlow] or [itemWithIdentifierFlow] according to the [ItemLayout] to a paged flow of [PlainItemData]
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class PagedPlainItemDataUseCase(
    private val itemSettingsRepository: ItemSettingsRepository,
    private val decryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    private val getSafeItemActionHelper: GetSafeItemActionHelper?,
    private val safeRepository: SafeRepository,
) {
    protected abstract fun itemFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItem>>
    protected abstract fun itemWithIdentifierFlow(pagingConfig: PagingConfig): Flow<PagingData<SafeItemWithIdentifier>>

    operator fun invoke(): Flow<PagingData<PlainItemData>> =
        safeRepository.currentSafeIdFlow().flatMapLatest { safeId ->
            safeId?.let {
                itemSettingsRepository.itemLayout(safeId).flatMapLatest { layoutStyle ->
                    when (layoutStyle) {
                        ItemLayout.Grid,
                        ItemLayout.LargeGrid,
                        -> {
                            itemFlow(AppConstants.Pagination.DefaultPagingConfig)
                                .mapPagingValues { item ->
                                    item.toPlainItemDataDefault(
                                        decryptUseCase = decryptUseCase,
                                        getIconUseCase = getIconUseCase,
                                        actions = getSafeItemActionHelper?.getQuickActions(item.id),
                                    )
                                }
                        }
                        ItemLayout.List -> {
                            itemWithIdentifierFlow(AppConstants.Pagination.RowPagingConfig)
                                .mapPagingValues { item ->
                                    item.toPlainItemDataRow(
                                        decryptUseCase = decryptUseCase,
                                        getIconUseCase = getIconUseCase,
                                        getQuickAction = { getSafeItemActionHelper?.getQuickActions(item.id) },
                                    )
                                }
                        }
                    }
                }
            } ?: flowOf(PagingData.empty())
        }
}
