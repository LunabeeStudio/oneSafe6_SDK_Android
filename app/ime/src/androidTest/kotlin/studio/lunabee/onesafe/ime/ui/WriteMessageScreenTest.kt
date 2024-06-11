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
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.messaging.writemessage.model.BubblesWritingMessage
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageRoute
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageUiState
import studio.lunabee.onesafe.messaging.writemessage.viewmodel.WriteMessageViewModel
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.testUUIDs
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID
import kotlin.test.Test

class WriteMessageScreenTest : LbcComposeTest() {

    private val plainMessage = "Plain Message"
    private val preview = testUUIDs[0].toString()

    private val mockkVm: WriteMessageViewModel = mockk {
        every { uiState } returns MutableStateFlow(
            WriteMessageUiState.Data(
                contactId = testUUIDs.random(OSTestConfig.random),
                nameProvider = DefaultNameProvider("Florian"),
                message = BubblesWritingMessage(TextFieldValue(plainMessage), preview),
                isUsingDeepLink = false,
                isConversationReady = true,
                isCorrupted = false,
            ),
        )
        every { conversation } returns emptyFlow()
        every { dialogState } returns MutableStateFlow(null)
        every { isMaterialYouSettingsEnabled } returns flowOf(false)
        every { snackbarState } returns MutableStateFlow(null)
        every { onPlainMessageChange(any()) } returns Unit
    }
    private val onClickOnChangeContact: () -> Unit = spyk({})

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun change_contact_test() {
        setScreen {
            hasText(plainMessage)
            hasText(preview)

            // Test change contact interaction
            hasTestTag(UiConstants.TestTag.Item.WriteMessageTopBar)
                .waitUntilExactlyOneExists()
                .performClick()
            verify(exactly = 1) { onClickOnChangeContact.invoke() }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun corrupted_test() {
        every { mockkVm.uiState } returns MutableStateFlow(
            WriteMessageUiState.Data(
                contactId = testUUIDs.random(OSTestConfig.random),
                nameProvider = DefaultNameProvider("Florian"),
                message = BubblesWritingMessage(TextFieldValue(""), ""),
                isUsingDeepLink = false,
                isConversationReady = true,
                isCorrupted = true,
            ),
        )
        setScreen {
            hasContentDescription(getString(OSString.accessibility_oneSafeK_sendAction))
                .waitUntilExactlyOneExists()
                .assertIsNotEnabled()

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
