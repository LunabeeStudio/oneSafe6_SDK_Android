package studio.lunabee.onesafe.feature.itemfielddetail.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.utils.OsDefaultPreview
import studio.lunabee.onesafe.extension.loremIpsumSpec
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme

@Composable
fun ItemFieldDetailsTextScreen(
    fieldValue: LbcTextSpec,
    fieldName: LbcTextSpec,
    navigateBack: () -> Unit,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(lazyListState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ItemDetailsFieldFullScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        LazyColumn(
            modifier = Modifier
                .nestedScroll(connection = nestedScrollConnection),
            state = lazyListState,
        ) {
            item {
                OSCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = OSDimens.SystemSpacing.Regular + OSDimens.ItemTopBar.Height,
                            start = OSDimens.SystemSpacing.Regular,
                            end = OSDimens.SystemSpacing.Regular,
                            bottom = OSDimens.SystemSpacing.Regular,
                        ),
                ) {
                    SelectionContainer(
                        modifier = Modifier
                            .fillMaxWidth(),
                    ) {
                        OSText(
                            text = fieldValue,
                            modifier = Modifier
                                .padding(all = OSDimens.SystemSpacing.Regular),
                        )
                    }
                }
            }
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                title = fieldName,
                options = listOf(
                    topAppBarOptionNavBack(navigateBack = navigateBack),
                ),
            )
        }
    }
}

@Composable
@OsDefaultPreview
fun ItemDetailsFieldFullScreenTextPreview() {
    OSTheme {
        ItemFieldDetailsTextScreen(
            fieldName = loremIpsumSpec(1),
            fieldValue = loremIpsumSpec(200),
            navigateBack = { },
        )
    }
}
