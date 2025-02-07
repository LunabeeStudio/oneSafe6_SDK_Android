package studio.lunabee.onesafe.usecase

import android.content.Context
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.common.utils.EmojiHelper
import studio.lunabee.onesafe.common.extensions.createTempFile
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.common.FieldIdProvider
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryData
import studio.lunabee.onesafe.domain.model.safeitem.DiscoveryItem
import studio.lunabee.onesafe.domain.model.safeitem.ItemFieldData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.qualifier.DefaultDispatcher
import studio.lunabee.onesafe.domain.usecase.GetUrlMetadataUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.qualifier.ImageCacheDirectory
import studio.lunabee.onesafe.ui.extensions.getFirstColorGenerated
import studio.lunabee.onesafe.ui.extensions.hexValue
import java.io.File
import java.util.UUID
import javax.inject.Inject

private const val DefaultLocale: String = "en"
private const val FetchingIconTimeOut: Long = 3_000
private const val ImageFileName: String = "icon_fetch"

class ImportDiscoveryItemUseCase @Inject constructor(
    private val createItemUseCase: CreateItemUseCase,
    private val addFieldUseCase: AddFieldUseCase,
    private val getUrlMetadataUseCase: GetUrlMetadataUseCase,
    private val fieldIdProvider: FieldIdProvider,
    @ApplicationContext private val context: Context,
    @ImageCacheDirectory private val imageCacheDir: File,
    @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
    private val emojiHelper: EmojiHelper,
    private val imageHelper: ImageHelper,
) {

    suspend operator fun invoke(
        data: DiscoveryData,
        locale: String,
    ): LBResult<Unit> {
        return OSError.runCatching {
            val labels: Map<String, String>? = data.labels[locale] ?: data.labels[DefaultLocale]
            data.data.forEach { plainItem ->
                importItem(
                    parentId = null,
                    discoveryItem = plainItem,
                    labels = labels,
                )
            }
        }
    }

    private suspend fun importItem(
        parentId: UUID? = null,
        discoveryItem: DiscoveryItem,
        labels: Map<String, String>?,
    ) {
        val itemUrl: String? = labels?.get(discoveryItem.fields.firstOrNull { it.kind == SafeItemFieldKind.Url.id }?.value)
        val (icon: ByteArray?, color: String?) = itemUrl?.let {
            withTimeoutOrNull(FetchingIconTimeOut) { getIconAndColor(itemUrl) }
        } ?: (null to null)

        val name = labels?.get(discoveryItem.title)
        val safeItem = createItemUseCase(
            name = name,
            parentId = parentId,
            isFavorite = discoveryItem.isFavorite,
            icon = icon,
            color = discoveryItem.color ?: color ?: name?.let { emojiHelper.checkEmojiColor(it, context) }?.hexValue,
            position = null,
        )

        if (safeItem is LBResult.Success) {
            val itemId: UUID = safeItem.successData.id
            addFieldUseCase(
                itemId = itemId,
                itemFieldsData = discoveryItem.fields.map { discoveryField ->
                    ItemFieldData(
                        id = fieldIdProvider(),
                        name = labels?.get(discoveryField.name),
                        position = discoveryField.position.toDouble(),
                        placeholder = labels?.get(discoveryField.placeholder),
                        value = labels?.get(discoveryField.value),
                        kind = SafeItemFieldKind.fromString(discoveryField.kind),
                        showPrediction = discoveryField.showPrediction,
                        isItemIdentifier = discoveryField.isItemIdentifier,
                        formattingMask = discoveryField.formattingMask,
                        secureDisplayMask = discoveryField.secureDisplayMask,
                        isSecured = discoveryField.isSecured,
                    )
                },
            )

            discoveryItem.items.forEach { child ->
                importItem(
                    parentId = itemId,
                    discoveryItem = child,
                    labels = labels,
                )
            }
        }
    }

    private suspend fun getIconAndColor(url: String): Pair<ByteArray?, String?> = withContext(dispatcher) {
        val iconFile = context.createTempFile(
            fileName = ImageFileName,
            directory = imageCacheDir,
        )

        try {
            getUrlMetadataUseCase.invoke(
                url = url,
                iconFile = iconFile,
                requestedData = GetUrlMetadataUseCase.RequestedData.Image,
                force = true,
            )

            val iconData = iconFile.readBytes()
            val colorFromIcon = imageHelper
                .osImageDataToBitmap(context = context, image = OSImageSpec.Data(iconData))
                ?.let { imageHelper.extractColorPaletteFromBitmap(it) }
                ?.getFirstColorGenerated()
                ?.hexValue
            iconData to colorFromIcon
        } finally {
            iconFile.delete()
        }
    }
}
