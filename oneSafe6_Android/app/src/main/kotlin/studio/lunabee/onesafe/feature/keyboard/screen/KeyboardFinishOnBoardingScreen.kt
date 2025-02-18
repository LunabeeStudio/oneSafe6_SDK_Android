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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.feature.keyboard.viewmodel.KeyboardFinishOnBoardingViewModel
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun KeyboardFinishOnBoardingRoute(
    navigateBack: () -> Unit,
    @Suppress("UnusedPrivateMember", "UNUSED_PARAMETER")
    viewModel: KeyboardFinishOnBoardingViewModel = hiltViewModel(),
) {
    KeyboardFinishOnBoardingScreen(
        navigateBack = navigateBack,
    )
}

@Composable
fun KeyboardFinishOnBoardingScreen(
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.AboutScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    end = OSDimens.SystemSpacing.Regular,
                    start = OSDimens.SystemSpacing.Regular,
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_team,
                offset = null,
            ) {
                OSCard {
                    Column(
                        modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
                        verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
                    ) {
                        OSText(
                            text = LbcTextSpec.StringResource(id = OSString.oneSafeK_onboarding_finish_cardTitle),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        OSText(
                            text = LbcTextSpec.StringResource(id = OSString.oneSafeK_onboarding_finish_cardDescription).markdown(),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_finish_button),
                onClick = navigateBack,
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
fun KeyboardFinishOnBoardingScreenPreview() {
    OSPreviewBackgroundTheme {
        KeyboardFinishOnBoardingScreen(
            navigateBack = {},
        )
    }
}
