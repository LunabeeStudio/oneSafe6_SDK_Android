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
 * Created by Lunabee Studio / Date - 5/30/2023 - for the oneSafe6 SDK.
 * Last modified 5/30/23, 5:09 PM
 */

package studio.lunabee.onesafe.ime.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageUiState
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID

class WriteMessageScreenTest : LbcComposeTest() {

    private val plainMessage = "Plain Message"
    private val encryptedMessage = UUID.randomUUID().toString()

    private val mockkVm: WriteMessageViewModel = mockk {
        every { uiState } returns MutableStateFlow(
            WriteMessageUiState(
                currentContact = UIBubblesContactInfo(
                    id = UUID.randomUUID(),
                    nameProvider = DefaultNameProvider("Florian"),
                    conversationState = ConversationState.FullySetup,
                ),
                plainMessage = plainMessage,
                encryptedPreview = encryptedMessage,
            ),
        )
        every { conversation } returns emptyFlow()
        every { isPreviewEnabled } returns MutableStateFlow(true)
        every { dialogState } returns MutableStateFlow(null)
        every { isMaterialYouSettingsEnabled } returns flowOf(false)
        every { snackbarState } returns MutableStateFlow(null)
    }
    private val onClickOnChangeContact: () -> Unit = spyk({})

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_write_message_screen_ui() {
        setScreen {
            hasText(plainMessage)
            hasText(encryptedMessage)

            // Test change contact interaction
            hasTestTag(UiConstants.TestTag.Item.WriteMessageTopBar)
                .waitUntilExactlyOneExists()
                .performClick()
            verify(exactly = 1) { onClickOnChangeContact.invoke() }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    private fun setScreen(
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                with(object : WriteMessageNavScope {
                    override val navigationToInvitation: (UUID) -> Unit = {}
                    override val navigateToContactDetail: (UUID) -> Unit = {}
                    override val navigateBack: () -> Unit = {}
                    override val deeplinkBubblesWriteMessage: ((contactId: UUID) -> Unit) = { _ -> }
                }) {
                    WriteMessageRoute(
                        onChangeRecipient = onClickOnChangeContact,
                        sendMessage = { _, _ -> },
                        contactIdFlow = MutableStateFlow(null),
                        sendIcon = OSImageSpec.Drawable(OSDrawable.ic_share),
                        viewModel = mockkVm,
                        hideKeyboard = null,
                        resendMessage = {},
                    )
                }
            }
            block()
        }
    }
}
