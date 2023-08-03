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
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.bubbles.ui.model.BubblesContactInfo
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.messaging.writemessage.composable.WriteMessageExitIcon
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
                currentContact = BubblesContactInfo(
                    id = UUID.randomUUID(),
                    nameProvider = DefaultNameProvider("Florian"),
                ),
                plainMessage = plainMessage,
                encryptedPreview = encryptedMessage,
                conversationError = null,
            ),
        )
        every { conversation } returns emptyFlow()
        every { dialogState } returns MutableStateFlow(null)
    }
    private val onClickOnChangeContact: () -> Unit = spyk({})

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun test_write_message_screen_ui() {
        setScreen {
            hasText(plainMessage)
            hasText(encryptedMessage)

            // Test change contact interaction
            hasTestTag(UiConstants.TestTag.Item.OneSafeKWriteMessageRecipientCard)
                .waitUntilExactlyOneExists(this)
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
                WriteMessageRoute(
                    onChangeRecipient = onClickOnChangeContact,
                    sendMessage = {},
                    exitIcon = WriteMessageExitIcon.WriteMessageCloseIcon {},
                    viewModel = mockkVm,
                    contactIdFlow = MutableStateFlow(null),
                    navigationToInvitation = {},
                    sendIcon = OSImageSpec.Drawable(R.drawable.ic_share),
                )
            }
            block()
        }
    }
}
