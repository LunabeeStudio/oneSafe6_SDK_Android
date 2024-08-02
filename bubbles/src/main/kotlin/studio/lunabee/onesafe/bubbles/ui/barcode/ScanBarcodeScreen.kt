/*
 * Copyright (c) 2023 Lunabee Studio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Lunabee Studio / Date - 7/13/2023 - for the oneSafe6 SDK.
 * Last modified 13/07/2023 15:57
 */

package studio.lunabee.onesafe.bubbles.ui.barcode

import android.Manifest
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.BarcodeView
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.messaging.domain.model.DecryptResult
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.snackbar.ErrorSnackbarState
import studio.lunabee.onesafe.model.OSActionState
import studio.lunabee.onesafe.model.TopAppBarOptionNav
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

context(ScanBarcodeNavScope)
@Composable
fun ScanBarcodeRoute(
    viewModel: ScanBarcodeViewModel = hiltViewModel(),
) {
    CameraPermission(navigateBack, viewModel, showSnackbar)

    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiResultState.collectAsStateWithLifecycle()

    dialogState?.DefaultAlertDialog()
    when (val safeUiState = uiState) {
        is ScanBarcodeUiState.Idle -> {}
        is ScanBarcodeUiState.NavigateToConversation -> {
            LaunchedEffect(Unit) { navigateToConversationPopToHome(safeUiState.decryptResult) }
        }
        is ScanBarcodeUiState.NavigateToCreateContact -> {
            LaunchedEffect(Unit) { navigateToCreateContactPopToHome(safeUiState.messageString) }
        }
    }
}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
private fun CameraPermission(
    navigateBack: () -> Unit,
    viewModel: ScanBarcodeViewModel,
    showSnackbar: (visuals: SnackbarVisuals) -> Unit,
) {
    var permissionDialogState by rememberDialogState()
    permissionDialogState?.DefaultAlertDialog()
    val deniedFeedbackSnackbar =
        ErrorSnackbarState(
            message = LbcTextSpec.StringResource(OSString.bubbles_scanbarcodeScreen_permission_deniedFeedback),
            onClick = {},
        ).snackbarVisuals

    var hasRequested: Int by remember {
        mutableIntStateOf(0)
    }

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA) {
        hasRequested++ // re-trigger permission state check & request
    }

    if (cameraPermissionState.status.isGranted) {
        ScanBarcodeScreen(
            onCloseClick = navigateBack,
            onBarcodeScan = viewModel::handleQrCode,
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        )
        LaunchedEffect(hasRequested) {
            when {
                cameraPermissionState.status.shouldShowRationale -> {
                    permissionDialogState = CameraScanPermissionRationaleDialogState(
                        launchPermissionRequest = {
                            cameraPermissionState.launchPermissionRequest()
                            permissionDialogState = null
                        },
                        retry = { permissionDialogState = null },
                        dismiss = navigateBack,
                    )
                }
                hasRequested == 0 -> cameraPermissionState.launchPermissionRequest()
                else -> {
                    showSnackbar(deniedFeedbackSnackbar)
                    navigateBack()
                }
            }
        }
    }
}

@Composable
fun ScanBarcodeScreen(
    onCloseClick: () -> Unit,
    onBarcodeScan: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(UiConstants.TestTag.Screen.ScanBarCodeScreen),
    ) {
        BarcodeAndroidView(
            onBarcodeScan = onBarcodeScan,
        )
        Column {
            Column(
                modifier = Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black,
                            Color.Transparent,
                        ),
                    ),
                ),
            ) {
                OSTopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    options = listOf(
                        TopAppBarOptionNav(
                            image = OSImageSpec.Drawable(OSDrawable.ic_close),
                            contentDescription = LbcTextSpec.StringResource(OSString.common_accessibility_back),
                            onClick = onCloseClick,
                            state = OSActionState.Enabled,
                        ),
                    ),
                )
                OSText(
                    text = LbcTextSpec.StringResource(OSString.bubbles_scanbarcodeScreen_description),
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .align(Alignment.CenterHorizontally)
                        .padding(
                            horizontal = OSDimens.SystemSpacing.Regular,
                            vertical = OSDimens.SystemSpacing.Large,
                        ),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Image(
                painter = painterResource(id = OSDrawable.ic_qr_indicator),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(
                        vertical = OSDimens.SystemSpacing.Large,
                        horizontal = OSDimens.SystemSpacing.Huge,
                    ),
                contentScale = ContentScale.FillWidth,
            )
        }
    }
}

@Composable
fun BarcodeAndroidView(
    onBarcodeScan: (String) -> Unit,
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val barcodeView = BarcodeView(context)
            barcodeView.resume()
            var buffer: String? = null
            var bufferCounter = 0
            barcodeView.decodeContinuous { result: BarcodeResult? ->
                val text = result?.text
                // Wait for 5 consecutive equal results before calling onBarcodeScan (only once)
                when {
                    text == null -> bufferCounter = 0
                    buffer == text && bufferCounter == ScanBufferSize -> {
                        onBarcodeScan(text)
                        bufferCounter++
                    }
                    buffer == text -> bufferCounter++
                    else -> bufferCounter = 0
                }
                buffer = text
            }
            barcodeView
        },
        onRelease = { barcodeView ->
            barcodeView.pause()
            barcodeView.stopDecoding()
        },
    )
}

interface ScanBarcodeNavScope {
    val navigateBack: () -> Unit
    val navigateToCreateContactPopToHome: (String) -> Unit
    val navigateToConversationPopToHome: (DecryptResult) -> Unit
    val showSnackbar: (visuals: SnackbarVisuals) -> Unit
}

private const val ScanBufferSize = 4
