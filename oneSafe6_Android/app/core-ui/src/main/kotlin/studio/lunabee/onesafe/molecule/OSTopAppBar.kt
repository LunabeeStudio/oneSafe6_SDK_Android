package studio.lunabee.onesafe.molecule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.text.OSResponsiveText
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.extension.drawableSample
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.OSTopAppBarOption
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.model.TopAppBarOptionTrailing
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ElevatedTopAppBar(
    title: LbcTextSpec,
    modifier: Modifier = Modifier,
    options: List<OSTopAppBarOption> = emptyList(),
    elevation: Dp = OSDimens.Elevation.None,
) {
    Column(
        modifier = modifier,
    ) {
        OSTopAppBar(
            title = title,
            options = options,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(OSDimens.TopBar.ShadowHeight)
                .shadow(elevation),
        )
    }
}

@Composable
fun OSTopAppBar(
    title: LbcTextSpec,
    modifier: Modifier = Modifier,
    options: List<OSTopAppBarOption> = emptyList(),
) {
    OSBasicTitleTopAppBar(title = title, modifier = modifier, options = options)
}

@Composable
fun OSTopAppBar(
    modifier: Modifier = Modifier,
    options: List<OSTopAppBarOption> = emptyList(),
) {
    OSBasicTitleTopAppBar(title = null, modifier = modifier, options = options)
}

@Composable
internal fun OSBasicTitleTopAppBar(
    title: LbcTextSpec?,
    modifier: Modifier,
    options: List<OSTopAppBarOption>,
) {
    OSBasicTopAppBar(
        modifier = modifier,
        options = options,
        horizontalArrangement = if (title != null) Arrangement.SpaceEvenly else Arrangement.SpaceBetween,
    ) {
        title?.let {
            Spacer(modifier = Modifier.width(width = OSDimens.AlternativeSpacing.Dimens12))

            OSResponsiveText(
                text = title,
                minFontSize = OSDimens.TopBar.TitleMinFontSize,
                modifier = Modifier
                    .weight(weight = 1f),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}

@Composable
fun OSBasicTopAppBar(
    modifier: Modifier = Modifier,
    options: List<OSTopAppBarOption>,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceEvenly,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .requiredHeight(OSDimens.ItemTopBar.Height)
            .fillMaxWidth()
            .padding(OSDimens.SystemSpacing.Small),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.filterIsInstance<TopAppBarOptionNav>().forEach { option ->
            OSIconButton(
                image = option.image,
                onClick = option.onClick,
                state = option.state,
                buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                colors = option.color(option.state),
                contentDescription = option.contentDescription,
            )
        }
        content()
        options.filterIsInstance<TopAppBarOptionTrailing>().forEach { action ->
            Spacer(modifier = Modifier.width(width = OSDimens.AlternativeSpacing.Dimens12))
            action.content()
        }
    }
}

@OsDefaultPreview
@Composable
private fun OSTopAppBarIconPreview() {
    OSPreviewBackgroundTheme {
        OSTopAppBar(
            title = loremIpsumSpec(2),
            options = listOf(
                TopAppBarOptionNav(
                    image = drawableSample,
                    onClick = { },
                    state = OSActionState.Enabled,
                ),
                TopAppBarOptionTrailing.secondaryIconAction(
                    image = drawableSample,
                    onClick = { },
                    state = OSActionState.Enabled,
                ),
                TopAppBarOptionTrailing.primaryIconAction(
                    image = drawableSample,
                    onClick = { },
                    state = OSActionState.Enabled,
                ),
            ),
        )
    }
}

@OsDefaultPreview
@Composable
private fun OSTopAppBarCustomPreview() {
    OSPreviewBackgroundTheme {
        OSTopAppBar(
            title = loremIpsumSpec(2),
            options = listOf(
                TopAppBarOptionTrailing {
                    Box(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(Color.Red, Color.Green)))
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        OSText(loremIpsumSpec(2))
                    }
                },
                TopAppBarOptionTrailing.secondaryIconAction(
                    image = drawableSample,
                    onClick = { },
                    state = OSActionState.Enabled,
                ),
            ),
        )
    }
}
