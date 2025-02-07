package studio.lunabee.onesafe.feature.itemform.model.uifield.file

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.StateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import java.io.File
import java.io.InputStream
import java.util.UUID

class CaptureFileUiField(
    override val thumbnailFlow: StateFlow<ThumbnailState>,
    override val id: UUID,
    override val fileId: UUID,
    fileName: String,
    private val getPlainInputStream: suspend () -> InputStream?,
    private val file: File,
    override val fileExtension: String,
    override val safeItemFieldKind: SafeItemFieldKind,
) : FileUiField() {

    override var fieldDescription: MutableState<LbcTextSpec> = mutableStateOf(LbcTextSpec.Raw(fileName))

    override fun isInError(): Boolean = false

    override fun getFileSavingData(): FileSavingData.ToSave {
        return FileSavingData.ToSave(
            fileId = fileId,
            getStream = getPlainInputStream,
        )
    }

    override fun deletePlainCache(): Boolean {
        return file.delete()
    }
}
