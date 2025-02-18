package studio.lunabee.onesafe.model

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import studio.lunabee.onesafe.extension.nonScaledSp
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTypography.labelXSmall

enum class OSSafeItemStyle(
    val elementSize: Dp,
    val paddingWithLabel: Dp,
    val iconSize: Dp,
    val spacing: Dp,
) {
    Tiny(
        elementSize = OSDimens.SystemRoundImage.tinySize,
        paddingWithLabel = OSDimens.SystemSpacing.ExtraSmall,
        iconSize = OSDimens.SystemRoundImage.TinyIconSize,
        spacing = OSDimens.SystemSpacing.Regular,
    ),
    Small(
        elementSize = OSDimens.SystemRoundImage.SmallSize,
        paddingWithLabel = OSDimens.SystemSpacing.Small,
        iconSize = OSDimens.SystemRoundImage.SmallIconSize,
        spacing = OSDimens.SystemSpacing.Regular,
    ),
    Regular(
        elementSize = OSDimens.SystemRoundImage.RegularSize,
        paddingWithLabel = OSDimens.SystemSpacing.Small,
        iconSize = OSDimens.SystemRoundImage.RegularIconSize,
        spacing = OSDimens.SystemSpacing.Regular,
    ),
    Large(
        elementSize = OSDimens.SystemRoundImage.LargeSize,
        paddingWithLabel = OSDimens.AlternativeSpacing.Dimens12,
        iconSize = OSDimens.SystemRoundImage.LargeIconSize,
        spacing = OSDimens.SystemSpacing.Small,
    ),
    ExtraLarge(
        elementSize = OSDimens.SystemRoundImage.ExtraLargeSize,
        paddingWithLabel = OSDimens.AlternativeSpacing.Dimens12,
        iconSize = OSDimens.SystemRoundImage.ExtraLargeIconSize,
        spacing = OSDimens.SystemSpacing.Small,
    ),
    ;

    val labelTextStyle: TextStyle
        @Composable
        get() {
            return when (this) {
                Tiny -> MaterialTheme.typography.labelXSmall
                Small -> MaterialTheme.typography.labelXSmall
                Regular -> MaterialTheme.typography.labelXSmall
                Large -> MaterialTheme.typography.labelMedium
                ExtraLarge -> MaterialTheme.typography.labelMedium
            }
        }

    val placeholderTextStyle: TextStyle
        @Composable
        get() {
            return when (this) {
                Tiny -> MaterialTheme.typography.titleSmall
                Small -> MaterialTheme.typography.titleMedium
                Regular -> MaterialTheme.typography.titleMedium
                Large -> MaterialTheme.typography.headlineLarge
                ExtraLarge -> MaterialTheme.typography.headlineLarge
            }
        }

    val emojiPlaceHolderTextStyle: TextStyle
        @Composable
        get() {
            return when (this) {
                Tiny -> placeholderTextStyle.copy(fontSize = (iconSize * SmallEmojiRatio).nonScaledSp)
                Small -> placeholderTextStyle.copy(fontSize = (iconSize * SmallEmojiRatio).nonScaledSp)
                Regular -> placeholderTextStyle.copy(fontSize = (iconSize * RegularEmojiRatio).nonScaledSp)
                Large -> placeholderTextStyle.copy(fontSize = (iconSize * RegularEmojiRatio).nonScaledSp)
                ExtraLarge -> placeholderTextStyle.copy(fontSize = (iconSize * RegularEmojiRatio).nonScaledSp)
            }
        }
}

private const val SmallEmojiRatio: Float = 0.65f
private const val RegularEmojiRatio: Float = 0.7f
