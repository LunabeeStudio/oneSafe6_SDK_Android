package studio.lunabee.onesafe.model

import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.molecule.OSPlaceHolder
import studio.lunabee.onesafe.molecule.OSRoundImage
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.UiConstants.Alpha.EmojiBackground

sealed interface OSItemIllustration {

    @Composable
    fun ImageComposable(contentDescription: LbcTextSpec?, style: OSSafeItemStyle)

    class Image(val image: OSImageSpec) : OSItemIllustration {

        @Composable
        override fun ImageComposable(contentDescription: LbcTextSpec?, style: OSSafeItemStyle) {
            OSRoundImage(
                image = image,
                contentDescription = contentDescription,
                modifier = Modifier
                    .testTag(UiConstants.TestTag.OSSafeItemImage)
                    .size(size = style.elementSize),
            )
        }
    }

    class Text(val text: LbcTextSpec, val color: Color?) : OSItemIllustration {

        @Composable
        override fun ImageComposable(contentDescription: LbcTextSpec?, style: OSSafeItemStyle) {
            OSPlaceHolder(
                placeholderName = text,
                elementSize = style.elementSize,
                placeholderColor = color ?: MaterialTheme.colorScheme.primary,
                placeholderTextStyle = style.placeholderTextStyle,
            )
        }
    }

    class Emoji(val text: LbcTextSpec, val color: Color?) : OSItemIllustration {

        @Composable
        override fun ImageComposable(contentDescription: LbcTextSpec?, style: OSSafeItemStyle) {
            OSPlaceHolder(
                placeholderName = text,
                elementSize = style.elementSize,
                placeholderColor = color?.copy(alpha = EmojiBackground)
                    ?: MaterialTheme.colorScheme.primary.copy(alpha = EmojiBackground),
                placeholderTextStyle = style.emojiPlaceHolderTextStyle,
            )
        }
    }
}
