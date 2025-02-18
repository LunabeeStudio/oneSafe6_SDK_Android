package studio.lunabee.onesafe.model

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.button.OSIconButton
import studio.lunabee.onesafe.atom.button.defaults.OSIconButtonDefaults
import studio.lunabee.onesafe.tooltip.OSRichTooltip
import studio.lunabee.onesafe.tooltip.OSTooltipAccessibility
import studio.lunabee.onesafe.tooltip.OSTooltipBox
import studio.lunabee.onesafe.tooltip.OSTooltipContent
import studio.lunabee.onesafe.tooltip.OSTooltipDefaults
import studio.lunabee.onesafe.tooltip.OSTooltipDefaults.accessibilityTooltip
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Stable
interface OSTopAppBarOption

class TopAppBarOptionNav(
    val image: OSImageSpec,
    val onClick: () -> Unit,
    val state: OSActionState,
    val contentDescription: LbcTextSpec? = null,
    val color: @Composable (OSActionState) -> ButtonColors = {
        OSIconButtonDefaults.secondaryIconButtonColors(state = it)
    },
) : OSTopAppBarOption

class TopAppBarOptionTrailing(
    val content: @Composable () -> Unit,
) : OSTopAppBarOption {
    companion object {
        fun primaryIconAction(
            image: OSImageSpec,
            onClick: () -> Unit,
            modifier: Modifier = Modifier,
            state: OSActionState = OSActionState.Enabled,
            contentDescription: LbcTextSpec? = null,
            tag: String = UiConstants.TestTag.OSAppBarMenu,
        ): TopAppBarOptionTrailing = TopAppBarOptionTrailing(
            content = {
                OSIconButton(
                    image = image,
                    onClick = onClick,
                    state = state,
                    modifier = modifier.testTag(tag),
                    buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                    contentDescription = contentDescription,
                )
            },
        )

        @OptIn(ExperimentalMaterial3Api::class)
        fun primaryTooltipIconAction(
            image: OSImageSpec,
            onClick: () -> Unit,
            tooltipContent: OSTooltipContent,
            tooltipAccessibility: OSTooltipAccessibility?,
            tooltipState: TooltipState,
            modifier: Modifier = Modifier,
            state: OSActionState = OSActionState.Enabled,
            contentDescription: LbcTextSpec? = null,
            tag: String = UiConstants.TestTag.OSAppBarMenu,
        ): TopAppBarOptionTrailing = TopAppBarOptionTrailing(
            content = {
                val coroutineScope: CoroutineScope = rememberCoroutineScope()
                OSTooltipBox(
                    modifier = modifier,
                    positionProvider = OSTooltipDefaults.rememberAnchorCenteredProvider(
                        spacingBetweenTooltipAndAnchor = OSDimens.SystemSpacing.Medium,
                    ),
                    state = tooltipState,
                    tooltip = {
                        OSRichTooltip(
                            title = tooltipContent.title,
                            text = tooltipContent.description,
                            actions = tooltipContent.actions,
                            dismissRequest = { coroutineScope.launch { tooltipState.dismiss() } },
                            modifier = if (tooltipAccessibility == null) {
                                Modifier
                            } else {
                                Modifier
                                    .accessibilityTooltip(tooltipAccessibility = tooltipAccessibility)
                            },
                        )
                    },
                    content = {
                        primaryIconAction(
                            image = image,
                            onClick = onClick,
                            modifier = Modifier,
                            state = state,
                            contentDescription = contentDescription,
                            tag = tag,
                        ).content()
                    },
                )
            },
        )

        fun secondaryIconAction(
            image: OSImageSpec,
            onClick: () -> Unit,
            modifier: Modifier = Modifier,
            state: OSActionState = OSActionState.Enabled,
            contentDescription: LbcTextSpec? = null,
        ): TopAppBarOptionTrailing = TopAppBarOptionTrailing(
            content = {
                OSIconButton(
                    image = image,
                    onClick = onClick,
                    buttonSize = OSDimens.SystemButtonDimension.NavBarAction,
                    state = state,
                    modifier = modifier
                        .testTag(tag = UiConstants.TestTag.OSAppBarMenu),
                    contentDescription = contentDescription,
                    colors = OSIconButtonDefaults.secondaryIconButtonColors(state = state),
                )
            },
        )
    }
}
