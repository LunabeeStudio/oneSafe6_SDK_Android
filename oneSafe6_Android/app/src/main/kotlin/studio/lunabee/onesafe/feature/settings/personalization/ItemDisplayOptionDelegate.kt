package studio.lunabee.onesafe.feature.settings.personalization

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import studio.lunabee.onesafe.domain.model.safeitem.ItemLayout
import studio.lunabee.onesafe.domain.model.safeitem.ItemOrder
import studio.lunabee.onesafe.domain.usecase.settings.GetItemSettingUseCase
import studio.lunabee.onesafe.domain.usecase.settings.SetItemSettingUseCase
import javax.inject.Inject

interface ItemDisplayOptionDelegate {
    val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet>
}

@ViewModelScoped
class ItemDisplayOptionDelegateImpl @Inject constructor(
    getItemSettingUseCase: GetItemSettingUseCase,
    private val setItemSettingUseCase: SetItemSettingUseCase,
) : ItemDisplayOptionDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {
    override val itemDisplayOptionsBottomSheet: StateFlow<ItemDisplayOptionsBottomSheet> = combine(
        getItemSettingUseCase.itemOrdering(),
        getItemSettingUseCase.itemsLayout(),
    ) { itemOrder, itemsLayout ->
        ItemDisplayOptionsBottomSheet(
            onSelectItemOrder = ::selectItemOrder,
            selectedItemOrder = itemOrder,
            onSelectItemLayout = ::selectItemLayout,
            selectedItemLayout = itemsLayout,
        )
    }.stateIn(
        coroutineScope,
        CommonUiConstants.Flow.DefaultSharingStarted,
        ItemDisplayOptionsBottomSheet(
            onSelectItemOrder = ::selectItemOrder,
            selectedItemOrder = ItemOrder.Alphabetic,
            onSelectItemLayout = ::selectItemLayout,
            selectedItemLayout = ItemLayout.Grid,
        ),
    )

    private fun selectItemOrder(itemOrder: ItemOrder) {
        coroutineScope.launch {
            setItemSettingUseCase.setItemOrdering(itemOrder)
        }
    }

    private fun selectItemLayout(itemLayout: ItemLayout) {
        coroutineScope.launch {
            setItemSettingUseCase.setItemLayout(itemLayout)
        }
    }
}
