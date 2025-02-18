package studio.lunabee.onesafe.usecase

import android.content.Context
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import studio.lunabee.onesafe.common.utils.EmojiHelper
import studio.lunabee.onesafe.domain.model.safeitem.DiscoverPrefillData
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.ui.extensions.hexValue
import javax.inject.Inject

private const val DefaultLocale: String = "en"

class ImportPrefillItemUseCase @Inject constructor(
    private val createItemUseCase: CreateItemUseCase,
    @ApplicationContext private val context: Context,
    private val emojiHelper: EmojiHelper,
) {
    suspend operator fun invoke(
        data: DiscoverPrefillData,
        locale: String,
    ): LBResult<Unit> {
        return OSError.runCatching {
            val labels: Map<String, String>? = data.labels[locale] ?: data.labels[DefaultLocale]
            data.data.forEach { plainItem ->
                importItem(
                    prefillItem = plainItem.title,
                    labels = labels,
                )
            }
        }
    }

    private suspend fun importItem(
        prefillItem: String,
        labels: Map<String, String>?,
    ) {
        val name = labels?.get(prefillItem)
        createItemUseCase(
            name = name,
            parentId = null,
            isFavorite = false,
            icon = null,
            color = name?.let { emojiHelper.checkEmojiColor(it, context) }?.hexValue,
            position = null,
        )
    }
}
