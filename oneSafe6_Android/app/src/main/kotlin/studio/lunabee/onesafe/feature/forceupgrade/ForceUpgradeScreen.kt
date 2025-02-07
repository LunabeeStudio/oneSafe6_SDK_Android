package studio.lunabee.onesafe.feature.forceupgrade

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.atom.button.defaults.OSTextButtonDefaults
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ForceUpgradeRoute(
    onSkipClick: () -> Unit,
    viewModel: ForceUpgradeViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val state by viewModel.state.collectAsStateWithLifecycle(initialValue = ForceUpgradeState.Screen())

    LaunchedEffect(null) {
        viewModel.splashScreenManager.isAppReady = true
    }

    ForceUpgradeScreen(
        onDoUpgradeClick = { uriHandler.openUri(CommonUiConstants.ExternalLink.Playstore) },
        onSkipClick = onSkipClick,
        state = state as? ForceUpgradeState.Screen,
    )
    if (state == ForceUpgradeState.Exit) onSkipClick()
}

@Composable
fun ForceUpgradeScreen(
    onSkipClick: () -> Unit,
    onDoUpgradeClick: () -> Unit,
    state: ForceUpgradeState.Screen?,
) {
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ForceUpgradeScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState),
        ) {
            Image(
                painter = painterResource(id = OSDrawable.ic_onesafe_text),
                contentDescription = null,
                modifier = Modifier
                    .padding(OSDimens.SystemSpacing.Regular)
                    .width(width = OSDimens.LayoutSize.LoginLogoTextWidth)
                    .align(Alignment.CenterHorizontally),
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.primary),
            )
            OSTopImageBox(
                imageRes = OSDrawable.character_hello,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            ) {
                OSMessageCard(
                    title = state?.title?.let(LbcTextSpec::Raw),
                    description = LbcTextSpec.Raw(state?.description.orEmpty()),
                    action = {
                        OSFilledButton(
                            text = LbcTextSpec.Raw(state?.buttonLabel.orEmpty()),
                            onClick = onDoUpgradeClick,
                            modifier = Modifier.minTouchVerticalButtonOffset(),
                        )
                    },
                )
            }
            if (state?.isForced == false) {
                OSTextButton(
                    text = LbcTextSpec.Raw("Plus tard"),
                    onClick = onSkipClick,
                    modifier = Modifier
                        .padding(horizontal = OSDimens.SystemSpacing.ExtraSmall)
                        .align(Alignment.End),
                    buttonColors = OSTextButtonDefaults.primaryTextButtonColors(state = OSActionState.Enabled),
                )
                OSRegularSpacer()
            }
        }
    }
}
