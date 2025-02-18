package studio.lunabee.onesafe.feature.itemdetails.model.informationtabentry

import kotlinx.coroutines.CoroutineScope
import studio.lunabee.onesafe.AppConstants
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemField
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.feature.fileviewer.loadfile.LoadFileUseCase
import studio.lunabee.onesafe.usecase.AndroidGetCachedThumbnailUseCase
import java.util.UUID

sealed interface InformationTabEntry {
    val kind: SafeItemFieldKind
    val id: UUID

    companion object {
        @Suppress("LongParameterList")
        suspend fun fromSafeItemField(
            field: SafeItemField,
            decryptUseCase: ItemDecryptUseCase,
            coroutineScope: CoroutineScope,
            getThumbnailFromFileUseCase: AndroidGetCachedThumbnailUseCase,
            loadFileUseCase: LoadFileUseCase,
            isFullScreen: Boolean,
        ): InformationTabEntry? {
            val kindString = field.encKind?.let { decryptUseCase(it, field.itemId, String::class).data }
            val kind = kindString?.let(SafeItemFieldKind::fromString)
                ?: SafeItemFieldKind.Unknown(id = AppConstants.DefaultValue.NoSafeItemKindId)
            return if (SafeItemFieldKind.isKindFile(kind)) {
                InformationTabEntryFileField.fromFileSafeItemField(
                    field = field,
                    decryptUseCase = decryptUseCase,
                    kind = kind,
                    coroutineScope = coroutineScope,
                    getThumbnailFromFileUseCase = getThumbnailFromFileUseCase,
                    isFullScreen = isFullScreen,
                    loadFileUseCase = loadFileUseCase,
                )
            } else {
                InformationTabEntryTextField.fromTextSafeItemField(field = field, decryptUseCase = decryptUseCase, kind = kind)
            }
        }
    }
}
