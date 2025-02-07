package studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import studio.lunabee.onesafe.usecase.AndroidGetCachedThumbnailUseCase
import java.io.File
import java.util.UUID

/**
 * @property thumbnail A flow containing the file thumbnail being created (
 * @property file A flow containing the file being created (decrypted to cache)
 */
@Stable
class InformationTabEntryFileField(
    override val id: UUID,
    override val kind: SafeItemFieldKind,
    val thumbnail: StateFlow<OSImageSpec?>,
    val file: StateFlow<File?>,
    val name: LbcTextSpec,
    val loadFile: () -> Unit,
) : InformationTabEntry {
    companion object {
        @Suppress("LongParameterList")
        suspend fun fromFileSafeItemField(
            field: SafeItemField,
            decryptUseCase: ItemDecryptUseCase,
            kind: SafeItemFieldKind,
            coroutineScope: CoroutineScope,
            loadFileUseCase: LoadFileUseCase,
            getThumbnailFromFileUseCase: AndroidGetCachedThumbnailUseCase,
            isFullScreen: Boolean,
        ): InformationTabEntry {
            val fileFlow: MutableStateFlow<File?> = MutableStateFlow(null)
            val thumbnailFlow: MutableStateFlow<OSImageSpec?> = MutableStateFlow(null)
            val name = field.encName?.let { decryptUseCase(it, field.itemId, String::class).data }
                ?: field.id.toString() // fallback to field id if the name is null
            coroutineScope.launch(Dispatchers.IO) {
                thumbnailFlow.value = getThumbnailFromFileUseCase(field, isFullScreen)
            }

            return InformationTabEntryFileField(
                id = field.id,
                kind = kind,
                name = LbcTextSpec.Raw(name),
                thumbnail = thumbnailFlow.asStateFlow(),
                file = fileFlow.asStateFlow(),
                loadFile = {
                    coroutineScope.launch(Dispatchers.IO) {
                        loadFileUseCase(safeItemField = field).map { it.data }.collect(fileFlow)
                    }
                },
            )
        }
    }
}
