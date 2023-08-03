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
 * Last modified 13/07/2023 10:03
 */

package studio.lunabee.onesafe.bubbles.ui.invitation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSForcedLightScreen
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.atom.lazyVerticalOSRegularSpacer
import studio.lunabee.onesafe.bubbles.ui.commoninvitation.CommonInvitationFactory
import studio.lunabee.onesafe.bubbles.ui.extension.getDeepLinkFromMessage
import studio.lunabee.onesafe.bubbles.ui.extension.toBarcodeBitmap
import studio.lunabee.onesafe.commonui.OSNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.commonui.action.TopAppBarOptionNavBack
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.extension.getTextSharingIntent
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun InvitationRoute(
    navigateToQrScan: () -> Unit,
    navigateToBubbleScreen: () -> Unit,
    navigateBack: () -> Unit,
    viewModel: InvitationViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val hasClickedShare = rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val isMaterialYouEnabled by viewModel.isMaterialYouEnabled.collectAsStateWithLifecycle(initialValue = false)

    OSForcedLightScreen(
        isMaterialYouEnabled = isMaterialYouEnabled,
    ) {
        dialogState?.DefaultAlertDialog()
        when (val safeState = uiState) {
            InvitationUiState.Exit -> {
                LaunchedEffect(Unit) { navigateBack() }
                OSScreen(testTag = "") { Box(modifier = Modifier.fillMaxSize()) }
            }
            null -> OSScreen(testTag = "") { Box(modifier = Modifier.fillMaxSize()) }
            is InvitationUiState.Data -> {
                val invitationLink = safeState.invitationString.getDeepLinkFromMessage()
                InvitationScreen(
                    onBackClick = navigateBack,
                    invitationLink = invitationLink,
                    onShareInvitationClick = {
                        hasClickedShare.value = true
                        val intent = context.getTextSharingIntent(invitationLink)
                        context.startActivity(intent)
                    },
                    contactName = safeState.contactName,
                    onFinishClick = {
                        if (hasClickedShare.value) {
                            navigateToBubbleScreen()
                        } else {
                            navigateToQrScan()
                        }
                    },
                )
            }
        }
    }
}

@Composable
fun InvitationScreen(
    onBackClick: () -> Unit,
    onShareInvitationClick: () -> Unit,
    contactName: OSNameProvider,
    invitationLink: String,
    onFinishClick: () -> Unit,
) {
    val invitationQr = remember(invitationLink) { invitationLink.toBarcodeBitmap() }
    OSScreen(
        testTag = UiConstants.TestTag.Screen.InvitationScreen,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            OSTopAppBar(
                title = LbcTextSpec.StringResource(R.string.bubbles_invitationScreen_title),
                options = listOf(TopAppBarOptionNavBack(onBackClick)),
            )
            LazyColumn(
                contentPadding = PaddingValues(OSDimens.SystemSpacing.Regular),
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .testTag(UiConstants.TestTag.Item.InvitationList),
            ) {
                InvitationScreenFactory.explanationCard(contactName, this)
                lazyVerticalOSRegularSpacer()
                invitationQr?.let {
                    CommonInvitationFactory.invitationBarcodeCard(invitationQr = invitationQr, lazyListScope = this)
                }
                lazyVerticalOSRegularSpacer()
                InvitationScreenFactory.sharedCard(lazyListScope = this, onShareClick = onShareInvitationClick)
                lazyVerticalOSRegularSpacer()
                CommonInvitationFactory.finishButtonScreen(lazyListScope = this, onClick = onFinishClick)
            }
        }
    }
}
