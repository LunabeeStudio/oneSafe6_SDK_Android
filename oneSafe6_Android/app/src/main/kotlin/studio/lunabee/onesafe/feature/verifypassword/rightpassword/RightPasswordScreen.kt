package studio.lunabee.onesafe.feature.verifypassword.rightpassword

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@Composable
fun RightPasswordScreen(navigateBack: () -> Unit) {
    val scrollState: ScrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.RightPasswordScreen,
        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = OSDimens.SystemSpacing.Regular),
            horizontalAlignment = Alignment.End,
        ) {
            OSTopImageBox(
                imageRes = OSDrawable.character_jamy_cool,
                offset = OSDimens.Card.DefaultImageCardOffset,
                modifier = Modifier
                    .padding(horizontal = OSDimens.SystemSpacing.Regular)
                    .padding(
                        top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                        bottom = OSDimens.SystemSpacing.Regular,
                    ),
            ) {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.rightPassword_card_title),
                    description = LbcTextSpec.StringResource(OSString.rightPassword_card_description),
                )
            }

            OSFilledButton(
                text = LbcTextSpec.StringResource(OSString.rightPassword_card_finishButton),
                onClick = navigateBack,
                modifier = Modifier.padding(end = OSDimens.SystemSpacing.Regular),
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack)),
            )
        }
    }
}

@OsDefaultPreview
@Composable
fun RightPasswordScreenPreview() {
    OSPreviewBackgroundTheme {
        RightPasswordScreen(navigateBack = {})
    }
}
