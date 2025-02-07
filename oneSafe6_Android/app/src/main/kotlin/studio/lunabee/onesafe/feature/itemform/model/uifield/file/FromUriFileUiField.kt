package studio.lunabee.onesafe.feature.itemform.model.uifield.file

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.StateFlow
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.UUID

/**
 * Instantiate a fileField from a new file
 * Used for field creation
 */
class FromUriFileUiField(
    override val thumbnailFlow: StateFlow<ThumbnailState>,
    override val id: UUID,
    override val safeItemFieldKind: SafeItemFieldKind,
    override val fileId: UUID,
    override val fileExtension: String?,
    private val getInputStream: () -> InputStream?,
    fileName: String,
) : FileUiField() {

    override var fieldDescription: MutableState<LbcTextSpec> = mutableStateOf(LbcTextSpec.Raw(fileName))

    /**
     * Try to read the input stream, if FileNotFoundException, we display an error
     */
    override fun isInError(): Boolean {
        return try {
            getInputStream()?.also(InputStream::close) == null
        } catch (e: FileNotFoundException) {
            true
        } catch (e: SecurityException) {
            true
        }
    }

    override fun getFileSavingData(): FileSavingData.ToSave {
        return FileSavingData.ToSave(
            fileId = fileId,
            getStream = getInputStream,
        )
    }

    override fun deletePlainCache(): Boolean = true // nothing to delete
}
