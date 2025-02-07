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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.OSSwitchRow
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(OverEncryptionBackupNavigation)
@Composable
fun OverEncryptionBackupRoute() {
    OverEncryptionBackupScreen(
        navigateBack = navigateBack,
        onNextClick = navigateToOverEncryptionKey,
    )
}

@Composable
private fun OverEncryptionBackupScreen(
    navigateBack: () -> Unit,
    onNextClick: (Boolean) -> Unit,
) {
    var isChecked: Boolean by rememberSaveable { mutableStateOf(true) }
    val lazyListState = rememberLazyListState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OverEncryptionBackupScreen,
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
                OSTopImageBox(imageRes = OSDrawable.character_jamy_cool) {
                    OSMessageCard(
                        description = LbcTextSpec.StringResource(id = OSString.overEncryptionBackup_mainCard_message),
                        action = null,
                        modifier = Modifier
                            .accessibilityMergeDescendants(),
                    )
                }
            }
            item {
                OSCard(
                    modifier = Modifier
                        .accessibilityMergeDescendants(),
                    content = {
                        OSSwitchRow(
                            modifier = Modifier
                                .padding(all = OSDimens.SystemSpacing.Regular),
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            label = LbcTextSpec.StringResource(OSString.overEncryptionBackup_toggleCard_message_enable),
                        )
                    },
                )
            }
            item {
                Box(Modifier.fillMaxWidth()) {
                    OSFilledButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        text = LbcTextSpec.StringResource(OSString.common_next),
                        onClick = { onNextClick(isChecked) },
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
private fun OverEncryptionBackupScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionBackupScreen(
            navigateBack = {},
            onNextClick = {},
        )
    }
}
