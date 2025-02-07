package studio.lunabee.onesafe.feature.share.file

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.common.extensions.byteToHumanReadable
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.extension.loremIpsum
import studio.lunabee.onesafe.feature.share.composable.ShareFileCard
import studio.lunabee.onesafe.feature.share.composable.SharePasswordCard
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview
import java.io.File

@Composable
fun ShareFileRoute(
    navigateBack: () -> Unit,
    viewModel: ShareFileViewModel = hiltViewModel(),
) {
    val uiState: ShareFileUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()

    dialogState?.DefaultAlertDialog()

    ShareFileScreen(
        navigateBack = navigateBack,
        uiState = uiState,
        copyText = viewModel::copyText,
        shareFile = { file -> viewModel.shareFile(context, file) },
    )
}

@Composable
fun ShareFileScreen(
    navigateBack: () -> Unit,
    uiState: ShareFileUiState,
    copyText: (label: String, value: String, isSecured: Boolean) -> Unit,
    shareFile: (File) -> Unit,
) {
    val scrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.ShareFileScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                ),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            when (uiState) {
                is ShareFileUiState.Data -> {
                    SharePasswordCard(
                        uiState.password,
                        copyText = copyText,
                    )

                    uiState.file?.let {
                        ShareFileCard(
                            fileSizeInfo = uiState.file.length().byteToHumanReadable(),
                            itemsNbr = uiState.itemsNbr,
                            onClickOnShare = { shareFile(uiState.file) },
                        )
                    }
                }
            }
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
private fun ShareFileScreenPreview() {
    OSPreviewBackgroundTheme {
        ShareFileScreen(
            navigateBack = {},
            uiState = ShareFileUiState.Data(
                password = loremIpsum(2),
                file = File(""),
                itemsNbr = 6,
            ),
            copyText = { _, _, _ -> },
            shareFile = {},
        )
    }
}
