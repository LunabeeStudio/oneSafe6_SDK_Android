package studio.lunabee.onesafe.common.utils

import android.content.Context
import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.extension.startEmojiOrNull
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.ui.extensions.getFirstColorGenerated
import javax.inject.Inject

class EmojiHelper @Inject constructor(
    private val imageHelper: ImageHelper,
) {
    suspend fun checkEmojiColor(text: String, context: Context): Color? {
        val emoji = text.startEmojiOrNull()
        return emoji?.let {
            imageHelper.createBitmapWithText(emoji)?.let { bitmapWithText ->
                val image = OSImageSpec.Data(imageHelper.convertBitmapToByteArray(bitmapWithText))
                imageHelper
                    .osImageDataToBitmap(context = context, image = image)
                    ?.let { imageHelper.extractColorPaletteFromBitmap(it) }
                    ?.getFirstColorGenerated()
            }
        }
    }
}
