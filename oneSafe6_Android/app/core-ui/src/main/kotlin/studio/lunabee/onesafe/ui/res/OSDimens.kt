package studio.lunabee.onesafe.ui.res

import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times

@Suppress("unused")
object OSDimens {
    /**
     * OneSafe spacings are available at https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-Design-System?node-id=909%3A16403
     */
    object SystemSpacing {
        val None: Dp = 0.dp
        val Hairline: Dp = 0.5.dp
        val ExtraSmall: Dp = 4.dp
        val Small: Dp = 8.dp
        val Medium: Dp = 12.dp
        val Regular: Dp = 16.dp
        val Large: Dp = 20.dp
        val ExtraLarge: Dp = 32.dp
        val Huge: Dp = 56.dp
    }

    object AlternativeSpacing {
        val Dimens12: Dp = 12.dp
        val ElementRowMinSpacing: Dp = 24.dp
        val ReorderButtonSpacing: Dp = 10.dp
    }

    /**
     * OneSafe text size are available at https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-Design-System?node-id=3%3A2
     */
    object SystemTextSize {
        val HeadlineLarge: TextUnit = 40.sp
        val TitleLarge: TextUnit = 24.sp
        val TitleMedium: TextUnit = 20.sp
        val LabelLarge: TextUnit = 16.sp
        val LabelMedium: TextUnit = 14.sp
        val LabelSmall: TextUnit = 13.sp
        val LabelXSmall: TextUnit = 11.sp
        val BodyLarge: TextUnit = 16.sp
        val BodyMedium: TextUnit = 15.sp
        val BodySmall: TextUnit = 13.sp
    }

    object SystemLineHeight {
        val BodyLarge: TextUnit = 24.sp
        val BodyMedium: TextUnit = 20.sp
        val BodySmall: TextUnit = 15.23.sp
        val HeadlineLarge: TextUnit = 36.sp
        val TitleLarge: TextUnit = 28.sp
        val TitleLargeBlack: TextUnit = 36.sp
        val LabelLarge: TextUnit = 20.sp
        val LabelMedium: TextUnit = 16.sp
        val LabelSmall: TextUnit = 16.sp
    }

    object LayoutSize {
        val LoginLogoTextWidth: Dp = 160.dp
        val PaddingTopLoginScreen: Dp = 75.dp
        val HomeLogoText: Dp = 200.dp
    }

    /**
     * OneSafe corner radius are available at https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-Design-System?node-id=909%3A16431
     */
    object SystemCornerRadius {
        val None: Dp = 0.dp
        val Small: Dp = 4.dp
        val Regular: Dp = 8.dp
        val Large: Dp = 12.dp
        val ExtraLarge: Dp = 20.dp
    }

    object Elevation {
        val None: Dp = 0.dp
        val TopButtonElevation: Dp = 1.dp
        val TopAppBarElevation: Dp = 4.dp
        val FloatingButton: Dp = 6.dp // copied from FabPrimaryTokens.ContainerElevation
    }

    /**
     * OneSafe button's size are described at https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-Design-System?node-id=47%3A2332
     */
    object SystemButton {
        val ExtraSmall: Dp = 32.dp
        val Small: Dp = 40.dp
        val Regular: Dp = 54.dp
        val Large: Dp = 90.dp

        @Composable
        fun minTouchPaddingOffset(targetSpacing: Dp, buttonHeight: Dp = ButtonDefaults.MinHeight): Dp {
            val minimumTouchTargetSize = LocalViewConfiguration.current.minimumTouchTargetSize
            val minTouchPadding = (minimumTouchTargetSize.height - buttonHeight).coerceAtLeast(0.dp) / 2
            return targetSpacing - minTouchPadding
        }
    }

    object SystemDialog {
        val ButtonPadding: Dp = 32.dp
        val DefaultPadding: Dp = 24.dp
    }

    // TODO @hbernardi following object should be removed soon (more PR to come)
    //  -> we should use [SystemImageDimension] and [SystemContainerDimension] instead.
    object SystemRoundImage {
        val tinySize: Dp = 32.dp
        val SmallSize: Dp = 44.dp
        val RegularSize: Dp = 56.dp
        val LargeSize: Dp = 90.dp
        val ExtraLargeSize: Dp = 108.dp

        val TinyIconSize: Dp = 24.dp
        val SmallIconSize: Dp = 33.dp
        val RegularIconSize: Dp = 40.dp
        val LargeIconSize: Dp = 67.5.dp
        val ExtraLargeIconSize: Dp = 81.dp
    }

    object Breadcrumb {
        val Height: Dp = 80.dp
        val TutorialHeight: Dp = 48.dp
        val SeparatorSize: Dp = 14.dp
        val Divider: Dp = 1.dp
    }

    object ItemTopBar {
        val Height: Dp = 56.dp
        val TopBarScrollVisibilityThreshold: Dp = 40.dp
    }

    object TabBar {
        val IndicatorRadius: Dp = 3.dp
    }

    object DividerThickness {
        val Small: Dp = 0.3.dp
        val Regular: Dp = 0.5.dp
        val Large: Dp = 1.5.dp
    }

    object Row {
        val InnerSpacing: Dp = 12.dp
    }

    object Card {
        val ItemSpacing: Dp = 12.dp
        val DefaultImageCardOffset: Dp = 17.dp
        val ExtraTopImageCardPadding: Dp = 8.dp
        val OffsetJamyCoolImage: Dp = 11.54.dp
        val OffsetHelloColored: Dp = 24.dp
    }

    object Chip {
        val IconSmall: Dp = 12.dp
        val IconRegular: Dp = 18.dp
        val ContainerSmall: Dp = 21.dp
        val ContainerRegular: Dp = 32.dp
    }

    object ActionButton {
        val IconRegular: Dp = 32.dp
        val IconSmall: Dp = 20.dp
        val PaddingRegular: Dp = SystemSpacing.Regular
        val PaddingSmall: Dp = 6.dp

        /**
         * Add extra padding (Start + End) when using a CircularProgress around a button.
         */
        val AdditionalPaddingWithCircularProgress: Dp = 2 * External.DefaultCircularStrokeWidth
    }

    object External {
        /**
         * @see [androidx.compose.material3.OutlinedTextFieldTopPadding]
         */
        val OutlinedTextFieldTopPadding: Dp = 8.dp

        /**
         *  see [androidx.compose.material3.tokens.CircularProgressIndicatorTokens.ActiveIndicatorWidth]
         */
        val DefaultCircularStrokeWidth: Dp = 4.dp
    }

    object ColorPicker {
        val size: Dp = 200.dp
    }

    object TopBar {
        val ShadowHeight: Dp = 0.2.dp
        val TitleMinFontSize: TextUnit = 16.sp
    }

    object OnBoarding {
        val paddingSecurityIcon: Dp = 60.dp
        val paddingCharacterHello: Dp = 14.dp
    }

    object Camera {
        val shutterButtonSize: Dp = 70.dp
        val optionButtonSize: Dp = 56.dp
        val recordIndicatorSize: Dp = 8.dp
        val optionButtonIconSize: Dp = 26.dp
        val loadingSize: Dp = 70.dp
    }

    /**
     * Icon size (i.e height + width) (not related to container size, see [SystemRoundImage]).
     * More: https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?node-id=47%3A2350&t=Gk7eyTM3rMAQFfTP-0
     */
    enum class SystemImageDimension(val dp: Dp) {
        Undefined(dp = Dp.Unspecified),
        Small(dp = 16.dp),
        FloatingAction(dp = 18.dp),
        NavBarAction(dp = 24.dp),
        Action(dp = 20.dp),
        Regular(dp = 33.dp),
        Large(dp = 40.dp),
        XLarge(dp = 67.5.dp),
    }

    /**
     * Dimension to apply to any rounded container like a [androidx.compose.material3.Button]
     * or a [studio.lunabee.onesafe.molecule.OSRoundImage].
     * More: https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-OneSafe-Design-System?node-id=47%3A2350&t=8SWIZdFdJSv16JKR-0
     */
    enum class SystemRoundContainerDimension(val dp: Dp) {
        ExtraSmall(dp = 24.dp),
        FloatingAction(dp = 30.dp),
        Small(dp = 32.dp),
        Medium(dp = 40.dp),
        Regular(dp = 44.dp),
        Large(dp = 56.dp),
        XLarge(dp = 90.dp),
    }

    enum class SystemButtonDimension(
        val container: SystemRoundContainerDimension,
        val image: SystemImageDimension,
    ) {
        Small(
            container = SystemRoundContainerDimension.Small,
            image = SystemImageDimension.Small,
        ),
        Regular(
            container = SystemRoundContainerDimension.Regular,
            image = SystemImageDimension.Regular,
        ),
        Large(
            container = SystemRoundContainerDimension.Large,
            image = SystemImageDimension.Large,
        ),
        XLarge(
            container = SystemRoundContainerDimension.XLarge,
            image = SystemImageDimension.XLarge,
        ),
        NavBarAction(
            container = SystemRoundContainerDimension.Medium,
            image = SystemImageDimension.NavBarAction,
        ),
        Action(
            container = SystemRoundContainerDimension.Medium,
            image = SystemImageDimension.Action,
        ),
        FloatingAction(
            container = SystemRoundContainerDimension.FloatingAction,
            image = SystemImageDimension.FloatingAction,
        ),
    }

    /**
     * Shortcut to map a [Float] to [Dp] with the current [Density].
     * This method is intended to be used in a [Composable].
     */
    @Composable
    @ReadOnlyComposable
    fun Float.toDensityDp(): Dp {
        return toDensityDp(density = LocalDensity.current, value = this)
    }

    /**
     * Shortcut to map a [Int] to [Dp] with the current [Density].
     * This method is intended to be used in a [Composable].
     */
    @Composable
    @ReadOnlyComposable
    fun Int.toDensityDp(): Dp {
        return toDensityDp(density = LocalDensity.current, value = this)
    }

    /**
     * Shortcut to map a [Float] to [Dp] with the current [Density].
     * This method is intended to be used outside of a [Composable] (i.e callback, onClick...).
     * [toDensityDp] should be preferred when called directly from a [Composable]
     * @param density current [Density] that has been previously extracted from LocalDensity.current for example.
     * @param value size as [Float]
     */
    fun toDensityDp(density: Density, value: Float): Dp {
        return with(density) { value.toDp() }
    }

    /**
     * Shortcut to map a [Int] to [Dp] with the current [Density].
     * This method is intended to be used outside of a [Composable] (i.e callback, onClick...).
     * [toDensityDp] should be preferred when called directly from a [Composable]
     * @param density current [Density] that has been previously extracted from LocalDensity.current for example.
     * @param value size as [Int]
     */
    fun toDensityDp(density: Density, value: Int): Dp {
        return with(density) { value.toDp() }
    }
}
