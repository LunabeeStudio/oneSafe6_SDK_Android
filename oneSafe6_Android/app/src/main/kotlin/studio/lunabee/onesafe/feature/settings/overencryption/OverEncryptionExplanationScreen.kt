package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(OverEncryptionExplanationNavigation)
@Composable
fun OverEncryptionExplanationRoute(
    viewModel: OverEncryptionExplanationViewModel = hiltViewModel(),
) {
    val isBackupEnabled by viewModel.isBackupEnabled.collectAsStateWithLifecycle()
    OverEncryptionExplanationScreen(
        navigateBack = navigateBack,
        onStartClick = {
            if (isBackupEnabled) {
                navigateToOverEncryptionKey(true)
            } else {
                navigateToOverEncryptionBackup()
            }
        },
    )
}

@Composable
private fun OverEncryptionExplanationScreen(
    navigateBack: () -> Unit,
    onStartClick: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OverEncryptionExplanationScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height),
            contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Large),
        ) {
            item {
                OSTopImageBox(imageRes = OSDrawable.character_hello) {
                    OSMessageCard(
                        description = LbcTextSpec.StringResource(id = OSString.overEncryptionExplanation_mainCard_message),
                        action = null,
                        modifier = Modifier
                            .accessibilityMergeDescendants(),
                    )
                }
            }
            item {
                OSMessageCard(
                    description = LbcTextSpec.StringResource(id = OSString.overEncryptionExplanation_warningCard_message),
                    action = null,
                    modifier = Modifier
                        .accessibilityMergeDescendants(),
                )
            }
            item {
                Box(Modifier.fillMaxWidth()) {
                    OSFilledButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        text = LbcTextSpec.StringResource(OSString.common_start),
                        onClick = onStartClick,
                    )
                }
            }
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.overEncryption_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = lazyListState.topAppBarElevation,
        )
    }
}

@Composable
@OsDefaultPreview
private fun OverEncryptionExplanationScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionExplanationScreen(
            navigateBack = {},
            onStartClick = {},
        )
    }
}
