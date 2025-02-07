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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
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
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSFilledButton
import studio.lunabee.onesafe.common.extensions.showCopyToast
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.SharePasswordLayout
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.error.codeText
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.jvm.get
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
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

context(OverEncryptionKeyNavigation)
@Composable
fun OverEncryptionKeyRoute(
    databaseKey: DatabaseKey,
    viewModel: OverEncryptionKeyViewModel = hiltViewModel(
        creationCallback = { factory: OverEncryptionKeyViewModelFactory ->
            factory.create(databaseKey.raw)
        },
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var dialogState by rememberDialogState()
    dialogState?.DefaultAlertDialog()

    val overEncryptionKeyCardData = when (val state = uiState) {
        is OverEncryptionKeyUiState.Idle -> OverEncryptionKeyCardData.Key(
            onEnableOverEncryptionClick = {
                dialogState = OverEncryptionKeyConfirmDialogState(
                    dismiss = { dialogState = null },
                    onConfirm = viewModel::enabledOverEncryption,
                )
            },
            onKeyClick = {
                viewModel.copyText(
                    context.getString(OSString.overEncryptionKey_keyCard_keyLabel),
                    state.key,
                    true,
                )
                context.showCopyToast(
                    context.getString(OSString.overEncryptionKey_keyCard_keyLabel),
                )
            },
            key = state.key,
        )
        is OverEncryptionKeyUiState.Loading -> OverEncryptionKeyCardData.Loading(state.step)
        OverEncryptionKeyUiState.Done -> {
            FinishSetupDatabaseActivity.launch(context)
            OverEncryptionKeyCardData.Loading(OverEncryptionKeyUiState.Loading.Step.entries.last())
        }
        is OverEncryptionKeyUiState.Error -> OverEncryptionKeyCardData.Error(
            error = state.error,
            openDiscord = { uriHandler.openUri(CommonUiConstants.ExternalLink.Discord) },
        )
    }

    OverEncryptionKeyScreen(
        navigateBack = navigateBack,
        uiData = overEncryptionKeyCardData,
    )
}

@Stable
internal sealed interface OverEncryptionKeyCardData {
    data class Key(
        val onEnableOverEncryptionClick: () -> Unit,
        val onKeyClick: () -> Unit,
        val key: String,
    ) : OverEncryptionKeyCardData

    data class Loading(val step: OverEncryptionKeyUiState.Loading.Step) : OverEncryptionKeyCardData

    data class Error(
        val error: Throwable?,
        val openDiscord: () -> Unit,
    ) : OverEncryptionKeyCardData
}

@Composable
private fun OverEncryptionKeyScreen(
    navigateBack: () -> Unit,
    uiData: OverEncryptionKeyCardData,
) {
    val lazyListState = rememberLazyListState()

    OSScreen(
        testTag = UiConstants.TestTag.Screen.OverEncryptionKeyScreen,
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
                when (uiData) {
                    is OverEncryptionKeyCardData.Key -> ShowKeyCard(uiData)
                    is OverEncryptionKeyCardData.Loading -> LoadingCard(uiData.step)
                    is OverEncryptionKeyCardData.Error -> OverEncryptionFailureCard(
                        description = LbcTextSpec.StringResource(
                            OSString.overEncryptionKey_errorCard_message,
                            uiData.error.codeText(),
                        ),
                        uiData.openDiscord,
                    )
                }
            }
            when (uiData) {
                is OverEncryptionKeyCardData.Key -> {
                    item {
                        Box(Modifier.fillMaxWidth()) {
                            OSFilledButton(
                                modifier = Modifier.align(Alignment.CenterEnd),
                                text = LbcTextSpec.StringResource(OSString.overEncryptionKey_enableButton),
                                onClick = uiData.onEnableOverEncryptionClick,
                            )
                        }
                    }
                }
                is OverEncryptionKeyCardData.Error,
                is OverEncryptionKeyCardData.Loading,
                -> {
                    /* no-op */
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
private fun LoadingCard(step: OverEncryptionKeyUiState.Loading.Step) {
    val progressText = when (step) {
        OverEncryptionKeyUiState.Loading.Step.Backup ->
            LbcTextSpec.StringResource(OSString.common_loading_backupinProgress)
        OverEncryptionKeyUiState.Loading.Step.Encryption ->
            LbcTextSpec.StringResource(OSString.overEncryptionKey_loadingCard_encryptionProgress)
    }
    OSTopImageLoadingCard(
        title = LbcTextSpec.StringResource(OSString.overEncryptionKey_loadingCard_title),
        description = LbcTextSpec.StringResource(OSString.overEncryptionKey_loadingCard_message),
        cardProgress = OSCardProgressParam.UndeterminedProgress(progressText),
        cardImage = OSCardImageParam(OSDrawable.character_jamy_cool, OSDimens.Card.OffsetJamyCoolImage),
    )
}

@Composable
private fun ShowKeyCard(overEncryptionKeyCardData: OverEncryptionKeyCardData.Key) {
    OSTopImageBox(imageRes = OSDrawable.character_jamy_cool) {
        OSMessageCard(
            description = LbcTextSpec.StringResource(OSString.overEncryptionKey_keyCard_message),
            action = { cardPadding ->
                SharePasswordLayout(
                    password = overEncryptionKeyCardData.key,
                    modifier = Modifier
                        .minTouchVerticalButtonOffset()
                        .padding(cardPadding)
                        .padding(top = OSDimens.SystemSpacing.Regular),
                    onClick = overEncryptionKeyCardData.onKeyClick,
                )
            },
            modifier = Modifier
                .accessibilityMergeDescendants(),
        )
    }
}

@Composable
@OsDefaultPreview
fun IdleOverEncryptionKeyScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionKeyScreen(
            navigateBack = {},
            uiData = OverEncryptionKeyCardData.Key(
                onEnableOverEncryptionClick = {},
                onKeyClick = { },
                key = "0x1234567890ABCDEF",
            ),
        )
    }
}

@Composable
@OsDefaultPreview
fun LoadingOverEncryptionKeyScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionKeyScreen(
            navigateBack = {},
            uiData = OverEncryptionKeyCardData.Loading(OverEncryptionKeyUiState.Loading.Step.Encryption),
        )
    }
}

@Composable
@OsDefaultPreview
fun ErrorOverEncryptionKeyScreenPreview() {
    OSPreviewBackgroundTheme {
        OverEncryptionKeyScreen(
            navigateBack = {},
            uiData = OverEncryptionKeyCardData.Error(
                error = OSStorageError.Code.UNKNOWN_DATABASE_ERROR.get(),
                openDiscord = {},
            ),
        )
    }
}
