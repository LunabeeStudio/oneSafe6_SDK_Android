package studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.creation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.usecase.item.AddAndRemoveFileUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.error.OSAppError
import studio.lunabee.onesafe.feature.itemform.destination.ItemCreationDestination
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemDataForSaving
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.SaveItemAndFieldDelegate
import studio.lunabee.onesafe.ui.extensions.hexValue
import java.util.UUID
import javax.inject.Inject

class SaveItemAndFieldCreationDelegate @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createItemUseCase: CreateItemUseCase,
    private val addFieldUseCase: AddFieldUseCase,
    private val addAndRemoveFileUseCase: AddAndRemoveFileUseCase,
    @ApplicationContext private val context: Context,
    private val imageHelper: ImageHelper,
) : SaveItemAndFieldDelegate {

    private val parentId: UUID? = savedStateHandle.get<String>(ItemCreationDestination.ItemParentIdArg)?.let(UUID::fromString)

    override suspend fun save(data: ItemDataForSaving): LBResult<UUID> = withContext(Dispatchers.Default) {
        val iconBytes = imageHelper.convertImageSpecToByteArray(data.icon, context)
        val creationResult = createItemUseCase(
            name = data.name,
            parentId = parentId,
            isFavorite = false,
            icon = iconBytes,
            color = data.color?.hexValue,
            position = null,
        )

        when (creationResult) {
            is LBResult.Failure -> LBResult.Failure(
                OSAppError(
                    code = OSAppError.Code.SAFE_ITEM_CREATION_FAILURE,
                    cause = creationResult.throwable,
                ),
            )
            is LBResult.Success -> {
                addAndRemoveFileUseCase(
                    item = creationResult.successData,
                    fileSavingData = data.fileSavingData,
                )
                addFields(
                    safeItemId = creationResult.successData.id,
                    itemFieldsData = data.itemFieldsData,
                )
            }
        }
    }

    private suspend fun addFields(
        safeItemId: UUID,
        itemFieldsData: List<ItemFieldData>,
    ): LBResult<UUID> {
        val operationResult: LBResult<List<SafeItemField>> = addFieldUseCase(
            itemId = safeItemId,
            itemFieldsData = itemFieldsData,
        )

        return when (operationResult) {
            is LBResult.Failure -> LBResult.Failure(operationResult.throwable)
            is LBResult.Success -> LBResult.Success(safeItemId)
        }
    }
}
