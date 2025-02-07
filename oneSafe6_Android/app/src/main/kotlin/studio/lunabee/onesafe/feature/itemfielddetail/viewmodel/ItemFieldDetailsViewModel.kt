package studio.lunabee.onesafe.feature.itemfielddetail.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.GetSafeItemFieldValueUseCase
import studio.lunabee.onesafe.feature.itemfielddetail.ItemFieldDetailsScreenDestination
import studio.lunabee.onesafe.feature.itemfielddetail.model.ItemFieldDetailsState
import studio.lunabee.onesafe.ui.extensions.toColor
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ItemFieldDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getSafeItemFieldValueUseCase: GetSafeItemFieldValueUseCase,
) : ViewModel() {
    private val itemId: UUID = savedStateHandle.get<String>(ItemFieldDetailsScreenDestination.ItemIdArg)!!.let(UUID::fromString)
    private val fieldId: UUID = savedStateHandle.get<String>(ItemFieldDetailsScreenDestination.FieldIdArg)!!.let(UUID::fromString)

    private val _uiState: MutableStateFlow<ItemFieldDetailsState> = MutableStateFlow(ItemFieldDetailsState.Initializing)
    val uiState: StateFlow<ItemFieldDetailsState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getSafeItemFieldValueUseCase(itemId = itemId, fieldId = fieldId).collect { result ->
                _uiState.value = when (result) {
                    is LBResult.Failure -> {
                        ItemFieldDetailsState.Error(error = result.throwable)
                    }
                    is LBResult.Success -> {
                        ItemFieldDetailsState.Data(
                            color = result.successData.color?.let { color -> Color(color.toColor().toArgb()) },
                            // Null should not be expected at this point as the screen could not be displayed if field does not exist
                            // and does not have a value (i.e value is at least empty but not null).
                            fieldName = LbcTextSpec.Raw(result.successData.fieldName.orEmpty()),
                            fieldValue = LbcTextSpec.Raw(result.successData.fieldValue.orEmpty()),
                            screenType = when (result.successData.fieldKind) {
                                is SafeItemFieldKind.Note -> ItemFieldDetailsState.Data.ScreenType.TEXT
                                else -> ItemFieldDetailsState.Data.ScreenType.PAGER
                            },
                        )
                    }
                }
            }
        }
    }
}
