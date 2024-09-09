package studio.lunabee.onesafe.help.lostkey

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSRegularSpacer
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.button.OSTextButton
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.importexport.ImportExportAndroidConstants
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.organism.card.OSMessageCard
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.LocalDesignSystem
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

context(LostKeyNavigation)
@Composable
fun LostKeyRoute(
    viewModel: LostKeyViewModel = hiltViewModel(),
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is LostKeyUiState.ExitToMain -> {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.setData(state.backupUri)
                ?.let { intent ->
                    context.startActivity(intent)
                    context.findFragmentActivity().finish()
                }
        }
        LostKeyUiState.Idle -> {
            /* no-op */
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarState by viewModel.snackbarState.collectAsStateWithLifecycle(null)
    val snackBarVisual = snackbarState?.snackbarVisuals
    LaunchedEffect(snackbarState) {
        snackBarVisual?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val folderUri by viewModel.folderUri.collectAsStateWithLifecycle()

    val pickFileLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        uri?.let { // uri can be null if user cancel the file selection (not an error)
            viewModel.cacheBackupFile(uri)
        }
    }

    LostKeyScreen(
        navigateBack = navigateBack,
        openDiscord = { uriHandler.openUri(CommonUiConstants.ExternalLink.Discord) },
        openFileManager = { viewModel.openInternalBackupStorage(context) },
        openDrive = folderUri?.let { { context.startActivity(Intent.parseUri(it.toString(), 0)) } },
        launchFilePicker = { pickFileLauncher.launch(ImportExportAndroidConstants.MimeTypeOs6lsb) },
        snackbarHostState = snackbarHostState,
    )
}

@Composable
private fun LostKeyScreen(
    navigateBack: () -> Unit,
    openDiscord: () -> Unit,
    openFileManager: () -> Unit,
    openDrive: (() -> Unit)?,
    launchFilePicker: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.LostKeyScreen,
        background = LocalDesignSystem.current.warningBackgroundGradient(),
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .verticalScroll(scrollState)
                .padding(OSDimens.SystemSpacing.Regular),
        ) {
            OSMessageCard(
                description = LbcTextSpec.StringResource(OSString.lostKey_explanationCard_message),
                action = {
                    OSTextButton(
                        text = LbcTextSpec.StringResource(OSString.common_askForHelpOndiscord),
                        onClick = openDiscord,
                        modifier = Modifier.minTouchVerticalButtonOffset(),
                    )
                },
            )
            OSRegularSpacer()
            LostKeyAccessBackupCard(
                onAccessLocalClick = openFileManager,
                onAccessRemoteClick = openDrive,
                onImportFileClick = launchFilePicker,
            )
        }

        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.lostKey_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = scrollState.topAppBarElevation,
        )

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .zIndex(UiConstants.SnackBar.ZIndex),
        )
    }
}

@Composable
@OsDefaultPreview
private fun LostKeyScreenPreview() {
    OSTheme {
        LostKeyScreen(
            navigateBack = {},
            openDiscord = {},
            openFileManager = {},
            openDrive = {},
            launchFilePicker = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
