package studio.lunabee.onesafe.feature.multisafe

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun MultiSafePresentationRoute(
    navigateBack: () -> Unit,
    createNewSafe: () -> Unit,
) {
    MultiSafePresentationScreen(
        navigateBack = navigateBack,
        createNewSafe = createNewSafe,
    )
}

@Composable
fun MultiSafePresentationScreen(
    navigateBack: () -> Unit,
    createNewSafe: () -> Unit,
) {
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.CongratulationOnBoarding,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(
                    top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                ),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            MultiSafeAnimation()
            OSMessageCard(
                title = LbcTextSpec.StringResource(OSString.multiSafeOnBoarding_presentation_sectionTitle),
                description = LbcTextSpec.StringResource(OSString.multiSafeOnBoarding_presentation_message),
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(bottom = OSDimens.SystemSpacing.Regular, top = OSDimens.SystemSpacing.Large),
            )

            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.multiSafeOnBoarding_presentation_button),
                onClick = createNewSafe,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .fillMaxWidth(),
            )
            OSRegularSpacer()
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(options = listOf(topAppBarOptionNavBack(navigateBack)))
        }
    }
}

@Composable
private fun MultiSafeAnimation() {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.multisafe),
    )
    val progress by animateLottieCompositionAsState(
        reverseOnRepeat = true,
        composition = composition,
        iterations = LottieConstants.IterateForever,
    )
    LottieAnimation(
        modifier = Modifier.fillMaxWidth(),
        composition = composition,
        enableMergePaths = true,
        progress = { progress },
    )
}

@OsDefaultPreview
@Composable
private fun MultiSafeScreenPreview() {
    OSPreviewOnSurfaceTheme {
        MultiSafePresentationScreen(navigateBack = {}, createNewSafe = {})
    }
}
