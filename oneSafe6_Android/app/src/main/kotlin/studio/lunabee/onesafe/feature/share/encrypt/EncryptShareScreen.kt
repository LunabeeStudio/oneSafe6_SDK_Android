package studio.lunabee.onesafe.feature.share.encrypt

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.animation.OSTopBarAnimatedVisibility
import studio.lunabee.onesafe.animation.rememberOSTopBarVisibilityNestedScrollConnection
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSPlurals
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.extension.markdownToAnnotatedString
import studio.lunabee.onesafe.domain.model.share.SharingData
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.organism.card.OSTopImageLoadingCard
import studio.lunabee.onesafe.organism.card.param.OSCardImageParam
import studio.lunabee.onesafe.organism.card.param.OSCardProgressParam
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun EncryptShareRoute(
    navigateBack: () -> Unit,
    onSuccess: (SharingData) -> Unit,
    viewModel: EncryptShareViewModel = hiltViewModel(),
) {
    val uiState: EncryptShareUIState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState is EncryptShareUIState.ReadyToShare) {
        onSuccess((uiState as EncryptShareUIState.ReadyToShare).sharingData)
    }

    EncryptShareScreen(
        navigateBack = navigateBack,
        uiState = uiState,
    )
}

@Composable
fun EncryptShareScreen(
    navigateBack: () -> Unit,
    uiState: EncryptShareUIState,
) {
    val scrollState = rememberScrollState()
    val nestedScrollConnection = rememberOSTopBarVisibilityNestedScrollConnection(scrollState)

    OSScreen(
        testTag = UiConstants.TestTag.Screen.EncryptShareScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(scrollState)
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                ),
        ) {
            when (uiState) {
                is EncryptShareUIState.Encrypting,
                is EncryptShareUIState.ReadyToShare,
                -> {
                    val itemsNbr = (uiState as? EncryptShareUIState.Encrypting)?.itemsNbr
                        ?: (uiState as? EncryptShareUIState.ReadyToShare)?.sharingData?.itemsNbr
                    OSTopImageLoadingCard(
                        title = LbcTextSpec.StringResource(OSString.share_encryptCard_title),
                        description = LbcTextSpec.Annotated(
                            pluralStringResource(
                                OSPlurals.share_encryptCard_message,
                                itemsNbr ?: 0,
                                itemsNbr ?: 0,
                            ).markdownToAnnotatedString(),
                        ),
                        cardProgress = OSCardProgressParam.UndeterminedProgress(),
                        cardImage = OSCardImageParam(OSDrawable.character_jamy_cool, OSDimens.Card.OffsetJamyCoolImage),
                    )
                }

                is EncryptShareUIState.Error -> {
                    OSTopImageLoadingCard(
                        title = LbcTextSpec.StringResource(OSString.share_encryptCard_title),
                        description = LbcTextSpec.StringResource(
                            OSString.error_defaultMessage,
                        ),
                        cardProgress = OSCardProgressParam.DeterminedProgress(
                            progress = ErrorProgress,
                        ),
                        cardImage = OSCardImageParam(OSDrawable.character_sabine_oups_right, null),
                        progressColor = MaterialTheme.colorScheme.error,
                        progressTrackColor = MaterialTheme.colorScheme.errorContainer,
                    )
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

const val ErrorProgress: Float = 0.33f

@OsDefaultPreview
@Composable
fun EncryptShareScreenPreview() {
    OSPreviewBackgroundTheme {
        EncryptShareScreen(
            navigateBack = { },
            uiState = EncryptShareUIState.Encrypting(12),
        )
    }
}

@OsDefaultPreview
@Composable
fun EncryptShareScreenErrorPreview() {
    OSPreviewBackgroundTheme {
        EncryptShareScreen(
            navigateBack = { },
            uiState = EncryptShareUIState.Error,
        )
    }
}
