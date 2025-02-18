package studio.lunabee.onesafe.feature.itemfielddetail.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.extension.replaceSpaceWithAsciiChar
import studio.lunabee.onesafe.commonui.extension.revertAsciiCharIntoSpace
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalColorPalette
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTypography
import java.lang.Integer.min

@Composable
fun ItemFieldDetailsPagerScreen(
    fieldValue: LbcTextSpec,
    navigateBack: () -> Unit,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val fieldToDisplay = fieldValue.string.replaceSpaceWithAsciiChar()
    val chunkedText = remember { fieldToDisplay.chunked(DisplayedCharPerPage) }
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f,
    ) {
        chunkedText.size
    }
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ItemDetailsFieldFullScreen,
        background = LocalDesignSystem.current.simpleBackground(),
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                val text = chunkedText[page]
                repeat(DisplayedCharPerPage) { index ->
                    val char = text.getOrNull(index)?.toString().orEmpty()
                    OSText(
                        text = LbcTextSpec.Raw(text.getOrNull(index)?.toString().orEmpty()),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = OSTypography.Legibility,
                            fontSize = dpToSp(dp = screenWidth / DisplayedCharPerPage),
                            color = if (index % 2 != 0) MaterialTheme.colorScheme.primary else Color.Unspecified,
                        ),
                        modifier = Modifier.semantics {
                            contentDescription = char.revertAsciiCharIntoSpace()
                        },
                    )
                }
            }
        }

        PagerIndicator(
            fieldValue = fieldToDisplay,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(OSDimens.SystemSpacing.Large)
                .clearAndSetSemantics {},
            visiblePage = pagerState.currentPage,
        )

        OSTopAppBar(
            options = listOf(
                topAppBarOptionNavBack(navigateBack = navigateBack),
            ),
        )
    }
}

@Composable
private fun PagerIndicator(
    visiblePage: Int,
    fieldValue: String,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.titleMedium.copy(
        fontFamily = OSTypography.Legibility,
        color = LocalColorPalette.current.Neutral60,
        fontWeight = FontWeight.Normal,
    )

    val starIndex = visiblePage * 4
    val endIndex = min((starIndex + DisplayedCharPerPage), fieldValue.length)
    val builder = AnnotatedString.Builder(fieldValue)
    builder.addStyle(style.toSpanStyle().copy(color = MaterialTheme.colorScheme.primary), starIndex, endIndex)
    OSText(
        text = LbcTextSpec.Annotated(builder.toAnnotatedString()),
        modifier = modifier,
        style = style,
        textAlign = TextAlign.Center,
    )
}

private const val DisplayedCharPerPage: Int = 4

@Composable
private fun dpToSp(dp: Dp) = with(LocalDensity.current) { dp.toSp() }
