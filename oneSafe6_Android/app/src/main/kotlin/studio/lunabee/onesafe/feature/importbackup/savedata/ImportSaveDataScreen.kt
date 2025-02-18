package studio.lunabee.onesafe.feature.importbackup.savedata

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.composed
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.accessibility.accessibilityClick
import studio.lunabee.onesafe.accessibility.accessibilityCustomAction
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.atom.button.defaults.OSFilledButtonDefaults
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ImportSaveDataScreen(
    currentSafeItemCount: Int,
    itemCount: Int?,
    importSaveDataState: ImportSaveDataUiState,
    navigateBack: () -> Unit,
    startImport: (mode: ImportMode) -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ImportSaveDataScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()
        val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)
        Column(
            modifier = Modifier
                .imePadding()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            val cardProgress = if (importSaveDataState is ImportSaveDataUiState.ImportInProgress) {
                OSCardProgressParam.UndeterminedProgress(
                    progressDescription = LbcTextSpec.StringResource(OSString.import_progressCard_title),
                )
            } else {
                null
            }
            OSTopImageLoadingCard(
                title = LbcTextSpec.StringResource(OSString.importSettings_card_title),
                description = itemCount?.let {
                    LbcTextSpec.Annotated(
                        pluralStringResource(OSPlurals.import_settings_description, it, it).markdownToAnnotatedString(),
                    )
                } ?: LbcTextSpec.StringResource(OSString.import_settings_description_fallback),
                cardProgress = cardProgress,
                cardImage = OSCardImageParam(
                    imageRes = OSDrawable.character_jamy_cool,
                    offset = OSDimens.Card.DefaultImageCardOffset,
                ),
                action = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        val buttonsState = if (importSaveDataState.isAllowingAction) OSActionState.Enabled else OSActionState.Disabled

                        if (currentSafeItemCount > 0) {
                            FilledOneSafeActions(
                                buttonsState = buttonsState,
                                importToFolder = { startImport(ImportMode.AppendInFolder) },
                                replaceContent = { startImport(ImportMode.Replace) },
                            )
                        } else {
                            EmptyOneSafeActions(
                                buttonsState = buttonsState,
                                importToFolder = { startImport(ImportMode.AppendInFolder) },
                                importToHome = { startImport(ImportMode.Replace) },
                            )
                        }
                    }
                },
                modifier = Modifier
                    .composed {
                        val importLabel = stringResource(id = OSString.importSettings_card_addButton)
                        val replaceLabel = stringResource(id = (OSString.importSettings_card_overrideButton))
                        val finishLabel = stringResource(id = OSString.import_button_complete)
                        val goBackToPreviousScreen =
                            stringResource(id = OSString.import_button_accessibility_complete_label)
                        if (importSaveDataState.isFinished) {
                            // override all content to indicates to user that import is over
                            clearAndSetSemantics {
                                accessibilityClick(label = finishLabel, action = navigateBack)
                                liveRegion = LiveRegionMode.Assertive
                                contentDescription = goBackToPreviousScreen
                            }
                        } else {
                            semantics(mergeDescendants = true) {
                                if (importSaveDataState.isAllowingAction) {
                                    customActions = listOfNotNull(
                                        accessibilityCustomAction(label = importLabel, action = { startImport(ImportMode.Append) }),
                                        accessibilityCustomAction(label = replaceLabel, action = { startImport(ImportMode.Replace) }),
                                    )
                                }
                            }
                        }
                    },
            )
        }

        OSTopBarAnimatedVisibility(visible = nestedScrollConnection.isTopBarVisible) {
            OSTopAppBar(
                modifier = Modifier
                    .statusBarsPadding()
                    .align(Alignment.TopCenter),
                options = listOf(topAppBarOptionNavBack(navigateBack, !importSaveDataState.isProcessing)),
            )
        }
    }
}

@Composable
fun EmptyOneSafeActions(
    buttonsState: OSActionState,
    importToFolder: () -> Unit,
    importToHome: () -> Unit,
) {
    OSFilledButton(
        text = LbcTextSpec.StringResource(OSString.importSettings_card_home),
        onClick = importToHome,
        state = buttonsState,
    )

    OSText(text = LbcTextSpec.StringResource(id = OSString.import_settings_or))

    OSFilledButton(
        text = LbcTextSpec.StringResource(OSString.importSettings_card_addButton),
        onClick = importToFolder,
        state = buttonsState,
        buttonColors = OSFilledButtonDefaults.secondaryButtonColors(state = buttonsState),
    )
}

@Composable
fun FilledOneSafeActions(
    buttonsState: OSActionState,
    importToFolder: () -> Unit,
    replaceContent: () -> Unit,
) {
    OSFilledButton(
        text = LbcTextSpec.StringResource(OSString.importSettings_card_addButton),
        onClick = importToFolder,
        state = buttonsState,
    )

    OSText(text = LbcTextSpec.StringResource(id = OSString.import_settings_or))

    OSFilledButton(
        text = LbcTextSpec.StringResource(OSString.importSettings_card_overrideButton),
        onClick = replaceContent,
        state = buttonsState,
        buttonColors = OSFilledButtonDefaults.secondaryButtonColors(state = buttonsState),
    )
}

@OsDefaultPreview
@Composable
private fun ImportSaveDataScreenNotStartedPreview() {
    OSTheme {
        ImportSaveDataScreen(
            itemCount = 125,
            importSaveDataState = ImportSaveDataUiState.WaitingForUserChoice,
            navigateBack = { },
            startImport = { },
            currentSafeItemCount = 0,
        )
    }
}

@OsDefaultPreview
@Composable
private fun ImportSaveDataScreenEndedPreview() {
    OSTheme {
        ImportSaveDataScreen(
            itemCount = 125,
            importSaveDataState = ImportSaveDataUiState.Success,
            navigateBack = { },
            startImport = { },
            currentSafeItemCount = 0,
        )
    }
}

@OsDefaultPreview
@Composable
private fun ImportSaveDataScreenProcessingPreview() {
    OSTheme {
        ImportSaveDataScreen(
            itemCount = 125,
            importSaveDataState = ImportSaveDataUiState.ImportInProgress(progress = .4f),
            navigateBack = { },
            startImport = { },
            currentSafeItemCount = 0,
        )
    }
}
