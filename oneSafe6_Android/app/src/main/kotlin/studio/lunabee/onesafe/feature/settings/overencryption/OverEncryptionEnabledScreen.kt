package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.FinishSetupDatabaseActivity
import studio.lunabee.onesafe.accessibility.accessibilityMergeDescendants
import studio.lunabee.onesafe.atom.OSCard
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.error.codeText
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.molecule.OSSwitchRow
import studio.lunabee.onesafe.molecule.OSTopImageBox
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(OverEncryptionSettingEnabledNavigation)
@Composable
fun OverEncryptionEnabledRoute(
    viewModel: OverEncryptionEnabledViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    val uiData = remember(uiState) {
        when (val state = uiState) {
            is OverEncryptionEnabledUiState.Idle -> OverEncryptionEnabledData.Idle(
                showBackupSwitch = !state.isBackupEnabled,
                onDisableOverEncryptionClick = { doBackup ->
                    dialogState = OverEncryptionEnabledConfirmDialogState(
                        dismiss = { dialogState = null },
                        onConfirm = { viewModel.disableOverEncryption(doBackup) },
                    )
                },
            )
            is OverEncryptionEnabledUiState.Loading -> OverEncryptionEnabledData.Loading(state.step)
            is OverEncryptionEnabledUiState.Error -> OverEncryptionEnabledData.Error(
                error = state.error,
                openDiscord = { uriHandler.openUri(CommonUiConstants.ExternalLink.Discord) },
            )
            OverEncryptionEnabledUiState.Done -> {
                FinishSetupDatabaseActivity.launch(context)
                OverEncryptionEnabledData.Loading(OverEncryptionEnabledUiState.Loading.Step.entries.last())
            }
        }
    }

    OverEncryptionEnabledScreen(
        uiData = uiData,
        navigateBack = navigateBack,
    )
}

@Stable
internal sealed interface OverEncryptionEnabledData {
    data class Idle(
        val showBackupSwitch: Boolean,
        val onDisableOverEncryptionClick: (Boolean) -> Unit,
    ) : OverEncryptionEnabledData

    data class Loading(val step: OverEncryptionEnabledUiState.Loading.Step) : OverEncryptionEnabledData

    data class Error(
        val error: Throwable?,
        val openDiscord: () -> Unit,
    ) : OverEncryptionEnabledData
}

@Composable
private fun OverEncryptionEnabledScreen(
    uiData: OverEncryptionEnabledData,
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.OverEncryptionEnabledScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .verticalScroll(scrollState)
                .padding(top = OSDimens.ItemTopBar.Height)
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            when (uiData) {
                is OverEncryptionEnabledData.Idle -> IdleItems(uiData)
                is OverEncryptionEnabledData.Loading -> LoadingCard(uiData.step)
                is OverEncryptionEnabledData.Error -> OverEncryptionFailureCard(
                    description = LbcTextSpec.StringResource(
                        OSString.overEncryptionEnabled_errorCard_message,
                        uiData.error.codeText(),
                    ),
                    openDiscord = uiData.openDiscord,
                )
            }
            Spacer(modifier = Modifier.size(OSDimens.SystemSpacing.Regular))
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.overEncryption_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = scrollState.topAppBarElevation,
        )
    }
}

@Composable
private fun IdleItems(
    uiData: OverEncryptionEnabledData.Idle,
) {
    var isChecked: Boolean by rememberSaveable { mutableStateOf(true) }

    OSTopImageBox(imageRes = OSDrawable.character_jamy_cool) {
        OSMessageCard(
            description = LbcTextSpec.StringResource(id = OSString.overEncryptionEnabled_mainCard_message),
            action = null,
            modifier = Modifier
                .accessibilityMergeDescendants(),
        )
    }
    if (uiData.showBackupSwitch) {
        OSCard(
            modifier = Modifier
                .accessibilityMergeDescendants(),
            content = {
                OSSwitchRow(
                    modifier = Modifier
                        .padding(all = OSDimens.SystemSpacing.Regular),
                    checked = isChecked,
                    onCheckedChange = { isChecked = it },
                    label = LbcTextSpec.StringResource(OSString.overEncryptionBackup_toggleCard_message_disable),
                )
            },
        )
    }
    Box(Modifier.fillMaxWidth()) {
        OSFilledButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            text = LbcTextSpec.StringResource(OSString.common_disable),
            onClick = { uiData.onDisableOverEncryptionClick(isChecked) },
        )
    }
}

@Composable
private fun LoadingCard(step: OverEncryptionEnabledUiState.Loading.Step) {
    val progressText = when (step) {
        OverEncryptionEnabledUiState.Loading.Step.Backup ->
            LbcTextSpec.StringResource(OSString.common_loading_backupinProgress)
        OverEncryptionEnabledUiState.Loading.Step.Decryption ->
            LbcTextSpec.StringResource(OSString.overEncryptionEnabled_loadingCard_decryptionProgress)
    }
    OSTopImageLoadingCard(
        title = LbcTextSpec.StringResource(OSString.overEncryptionEnabled_loadingCard_title),
        description = LbcTextSpec.StringResource(OSString.overEncryptionEnabled_loadingCard_message),
        cardProgress = OSCardProgressParam.UndeterminedProgress(progressText),
        cardImage = OSCardImageParam(OSDrawable.character_jamy_cool, OSDimens.Card.OffsetJamyCoolImage),
    )
}

@Composable
@OsDefaultPreview
fun IdleOverEncryptionEnabledScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionEnabledScreen(
            navigateBack = {},
            uiData = OverEncryptionEnabledData.Idle(
                showBackupSwitch = true,
                onDisableOverEncryptionClick = { },
            ),
        )
    }
}

@Composable
@OsDefaultPreview
fun LoadingOverEncryptionEnabledScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionEnabledScreen(
            navigateBack = {},
            uiData = OverEncryptionEnabledData.Loading(OverEncryptionEnabledUiState.Loading.Step.Decryption),
        )
    }
}

@Composable
@OsDefaultPreview
fun ErrorOverEncryptionEnabledScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionEnabledScreen(
            navigateBack = {},
            uiData = OverEncryptionEnabledData.Error(
                error = OSStorageError.Code.UNKNOWN_DATABASE_ERROR.get(),
                openDiscord = {},
            ),
        )
    }
}
