package studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.edition

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.usecase.CheckValueChangeUseCase
import studio.lunabee.onesafe.domain.usecase.item.UpdateItemUseCase
import studio.lunabee.onesafe.feature.itemform.destination.ItemEditionDestination
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.ItemDataForSaving
import studio.lunabee.onesafe.feature.itemform.viewmodel.delegate.SaveItemAndFieldDelegate
import studio.lunabee.onesafe.ui.extensions.hexValue
import java.util.UUID
import javax.inject.Inject

class SaveItemAndFieldEditionDelegate @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val updateItemUseCase: UpdateItemUseCase,
    private val checkValueChangeUseCase: CheckValueChangeUseCase,
    @ApplicationContext private val context: Context,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val imageHelper: ImageHelper,
) : SaveItemAndFieldDelegate {

    val itemId: UUID = savedStateHandle.get<String>(ItemEditionDestination.ItemIdArg)?.let(UUID::fromString)
        ?: error("Item id not provided")

    override suspend fun save(data: ItemDataForSaving): LBResult<UUID> = withContext(dispatcher) {
        val iconBytes = imageHelper.convertImageSpecToByteArray(data.icon, context)
        val updateResult = updateItemUseCase(
            itemId = itemId,
            updateData = UpdateItemUseCase.UpdateData(
                name = checkValueChangeUseCase(value = data.name, previousValue = data.initialInfo?.name),
                icon = checkValueChangeUseCase(value = iconBytes, previousValue = data.initialInfo?.icon),
                color = checkValueChangeUseCase(value = data.color?.hexValue, previousValue = data.initialInfo?.color?.hexValue),
            ),
            fields = data.itemFieldsData,
            fileSavingData = data.fileSavingData,
        )
        when (updateResult) {
            is LBResult.Failure -> LBResult.Failure(updateResult.throwable)
            is LBResult.Success -> LBResult.Success(itemId)
        }
    }
}
