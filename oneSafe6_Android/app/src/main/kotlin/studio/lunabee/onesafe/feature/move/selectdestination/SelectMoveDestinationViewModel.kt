package studio.lunabee.onesafe.feature.move.selectdestination

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.model.item.PlainItemData
import studio.lunabee.onesafe.commonui.EmojiNameProvider
import studio.lunabee.onesafe.commonui.ErrorNameProvider
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.domain.model.safeitem.SafeItem
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.GetIconUseCase
import studio.lunabee.onesafe.domain.usecase.item.CountSafeItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.model.OSItemIllustration
import studio.lunabee.onesafe.ui.extensions.toColor
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SelectMoveDestinationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle?,
    safeItemRepository: SafeItemRepository,
    private val decryptUseCase: ItemDecryptUseCase,
    private val getIconUseCase: GetIconUseCase,
    pagedPlainItemDataUseCaseFactory: SelectMoveDestinationPagedPlainItemDataUseCaseFactory,
    countSafeItemInParentUseCase: CountSafeItemUseCase,
) : ViewModel() {

    private val itemDestinationId: UUID? =
        savedStateHandle?.get<String?>(SelectMoveDestination.DestinationItemIdArgument)
            ?.let(UUID::fromString)

    var initialItemsCount: Int = 0
        private set

    val items: Flow<PagingData<PlainItemData>> = pagedPlainItemDataUseCaseFactory.create(itemDestinationId)()
        .cachedIn(viewModelScope)

    private val _currentItem = MutableStateFlow<MoveCurrentDestination?>(null)
    val currentItem: StateFlow<MoveCurrentDestination?> get() = _currentItem.asStateFlow()

    init {
        viewModelScope.launch {
            initialItemsCount = countSafeItemInParentUseCase.notDeleted(itemDestinationId).data ?: 0
            if (itemDestinationId != null) {
                val safeItem: SafeItem = safeItemRepository.getSafeItem(itemDestinationId)
                var isError = false
                val itemName = safeItem.encName?.let {
                    val result = decryptUseCase(it, itemDestinationId, String::class)
                    if (result is LBResult.Failure) isError = true
                    result.data.orEmpty()
                }
                val icon = safeItem.iconId?.let { getIconUseCase(it, itemDestinationId).data }

                val itemNameProvider = if (isError) {
                    ErrorNameProvider
                } else {
                    OSNameProvider.fromName(
                        name = itemName,
                        hasIcon = icon != null,
                    )
                }
                val colorString: String? = safeItem.encColor?.let { decryptUseCase(it, safeItem.id, String::class) }?.data
                _currentItem.value = MoveCurrentDestination(
                    itemNameProvider = itemNameProvider,
                    itemIcon = if (icon != null) {
                        OSItemIllustration.Image(OSImageSpec.Data(icon))
                    } else if (itemNameProvider is EmojiNameProvider) {
                        OSItemIllustration.Emoji(itemNameProvider.placeholderName, colorString?.toColor())
                    } else {
                        OSItemIllustration.Text(itemNameProvider.placeholderName, colorString?.toColor())
                    },
                )
            }
        }
    }
}
