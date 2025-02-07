package studio.lunabee.onesafe.feature.itemform.model.uifield.file

import androidx.compose.runtime.MutableState
import com.lunabee.lbcore.model.LBFlowResult
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.Constant
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import studio.lunabee.onesafe.usecase.AndroidGetThumbnailFromFileUseCase
import java.io.File
import java.util.UUID

private val logger = LBLogger.get<ExistingFileUiField>()

/**
 * Instantiate a fileField from an existing field and start loading the thumbnail on the provided [CoroutineScope]
 * Used for item edition
 */
class ExistingFileUiField(
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    val safeItemField: SafeItemField,
    override var fieldDescription: MutableState<LbcTextSpec>,
    private val loadFileUseCase: LoadFileUseCase,
    value: String,
    private val getThumbnailFromFileUseCase: AndroidGetThumbnailFromFileUseCase,
) : FileUiField() {

    private val _thumbnailFlow: MutableStateFlow<ThumbnailState> = MutableStateFlow(ThumbnailState.Loading)
    override val fileExtension: String = value.substringAfter(Constant.FileTypeExtSeparator)
    override val fileId: UUID = UUID.fromString(value.substringBefore(Constant.FileTypeExtSeparator))
    override val thumbnailFlow: StateFlow<ThumbnailState> = _thumbnailFlow.asStateFlow()

    var file: File? = null

    suspend fun loadFile() {
        loadFileUseCase(safeItemField = safeItemField).collect { result ->
            when (result) {
                is LBFlowResult.Failure -> result.throwable?.let(logger::e)
                is LBFlowResult.Loading -> _thumbnailFlow.value = ThumbnailState.Loading
                is LBFlowResult.Success -> {
                    file = result.successData.also {
                        val thumbnail = getThumbnailFromFileUseCase(it, displayDuration = false)
                        _thumbnailFlow.value = ThumbnailState.Finished(thumbnail.imageSpec)
                    }
                }
            }
        }
    }

    override fun isInError(): Boolean = false

    override fun getFileSavingData(): FileSavingData.ToSave? = null // ExistingFileUiField cannot be modified

    override fun deletePlainCache(): Boolean {
        return file?.delete() ?: true
    }
}
