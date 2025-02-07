package studio.lunabee.onesafe.feature.keyboard.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationAction
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationStep
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationStepLayout
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun KeyboardPresentationRoute(
    navigateBack: () -> Unit,
    onClickOnConfigure: () -> Unit,
) {
    KeyboardPresentationScreen(
        navigateBack = navigateBack,
        onClickOnConfigure = onClickOnConfigure,
    )
}

@Composable
fun KeyboardPresentationScreen(
    navigateBack: () -> Unit,
    onClickOnConfigure: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OneSafeKPresentationScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    bottom = OSDimens.SystemSpacing.Huge,
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            PresentationStepLayout(
                presentationStep = PresentationStep(
                    title = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_presentation_title),
                    description = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_presentation_description).markdown(),
                    imageRes = OSDrawable.illustration_bubbles_onboarding,
                    actions = listOf(
                        PresentationAction(
                            label = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_presentation_ctaButton),
                            action = onClickOnConfigure,
                        ),
                    ),
                ),
            )
        }
        OSTopAppBar(
            title = LbcTextSpec.Raw(""),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
        )
    }
}

@Composable
@OsDefaultPreview
fun KeyboardPresentationScreenPreview() {
    OSPreviewBackgroundTheme {
        KeyboardPresentationScreen(
            navigateBack = {},
            onClickOnConfigure = {},
        )
    }
}
