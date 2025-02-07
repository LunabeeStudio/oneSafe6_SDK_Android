package studio.lunabee.onesafe.feature.exportbackup.getarchive

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lunabee.lbloading.LoadingBackHandler
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSImage
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun ExportGetArchiveScreen(
    shareFile: () -> Unit,
    saveFile: () -> Unit,
    navigateBackToSettingsDestination: () -> Unit,
) {
    // Set to true when user clicks on at least one button. If true, "done" button will be automatically displayed.
    var hasUserInteract: Boolean by rememberSaveable { mutableStateOf(value = false) }
    LoadingBackHandler(onBack = navigateBackToSettingsDestination)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ExportGetArchiveScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Large),
        ) {
            OSTopImageBox(imageRes = OSDrawable.character_jamy_cool) {
                OSMessageCard(
                    title = LbcTextSpec.StringResource(OSString.backup_exportBackup_title),
                    description = LbcTextSpec.StringResource(OSString.backup_exportBackup_description),
                    action = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            OSRegularSpacer()

                            OSFilledButton(
                                text = LbcTextSpec.StringResource(OSString.backup_exportBackup_saveButton),
                                onClick = {
                                    hasUserInteract = true
                                    saveFile()
                                },
                                buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                                leadingIcon = {
                                    OSImage(image = OSImageSpec.Drawable(OSDrawable.ic_download))
                                },
                            )

                            OSText(text = LbcTextSpec.StringResource(id = OSString.import_settings_or))

                            OSFilledButton(
                                text = LbcTextSpec.StringResource(OSString.backup_exportBackup_shareButton),
                                onClick = {
                                    hasUserInteract = true
                                    shareFile()
                                },
                                buttonColors = OSFilledButtonDefaults.secondaryButtonColors(),
                                leadingIcon = {
                                    OSImage(image = OSImageSpec.Drawable(OSDrawable.ic_share))
                                },
                            )

                            OSRegularSpacer()
                        }
                    },
                )
            }

            OSRegularSpacer()

            if (hasUserInteract) {
                OSFilledButton(
                    text = LbcTextSpec.StringResource(OSString.backup_exportBackup_doneButton),
                    onClick = navigateBackToSettingsDestination,
                    modifier = Modifier
                        .align(Alignment.End),
                )
            }
        }

        OSTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            options = listOf(topAppBarOptionNavBack(navigateBackToSettingsDestination)),
        )
    }
}
