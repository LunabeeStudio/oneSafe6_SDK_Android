/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Last modified 5/30/23, 2:18 PM
 */

package studio.lunabee.onesafe.ime.ui

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.bubbles.ui.model.UIBubblesContactInfo
import studio.lunabee.onesafe.commonui.DefaultNameProvider
import studio.lunabee.onesafe.commonui.R
import studio.lunabee.onesafe.ime.ui.contact.ImeContactRoute
import studio.lunabee.onesafe.ime.ui.contact.ImeContactUiState
import studio.lunabee.onesafe.ime.ui.contact.ImeContactViewModel
import studio.lunabee.onesafe.messaging.domain.model.ConversationState
import studio.lunabee.onesafe.ui.UiConstants
import java.util.UUID

@OptIn(ExperimentalTestApi::class)
class ImeContactScreenTest : LbcComposeTest() {

    private val mockkVM: ImeContactViewModel = mockk()
    private val onClickOnContact: (UUID) -> Unit = spyk({})

    @Test
    fun test_empty_select_contact_screen() {
        every { mockkVM.uiState } returns MutableStateFlow(ImeContactUiState.Empty)
        setScreen(mockkVM) {
            hasTestTag(UiConstants.TestTag.Item.BubblesNoContactCard)
        }
    }

    @Test
    fun test_full_select_contact_screen() {
        val contact1Name = "toto"
        val contact2Name = "tata"
        val contact1 = UIBubblesContactInfo(
            UUID.randomUUID(),
            nameProvider = DefaultNameProvider(contact1Name),
            ConversationState.FullySetup,
        )
        val contact2 = UIBubblesContactInfo(
            UUID.randomUUID(),
            nameProvider = DefaultNameProvider(contact2Name),
            ConversationState.FullySetup,
        )

        every { mockkVM.uiState } returns MutableStateFlow(ImeContactUiState.Data(listOf(contact1, contact2)))
        setScreen(mockkVM) {
            onNodeWithTag(UiConstants.TestTag.Item.BubblesNoContactCard).assertDoesNotExist()
            hasText(contact1Name).waitUntilExactlyOneExists(this).performClick()
            verify(exactly = 1) { onClickOnContact.invoke(contact1.id) }

            hasText(contact2Name).waitUntilExactlyOneExists(this).performClick()
            verify(exactly = 1) { onClickOnContact.invoke(contact2.id) }
        }
    }

    private fun setScreen(
        viewModel: ImeContactViewModel,
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                ImeContactRoute(
                    navigateToWriteMessage = onClickOnContact,
                    viewModel = viewModel,
                    navigateBack = {},
                    exitIcon = R.drawable.ic_close,
                    deeplinkBubblesHomeContact = {},
                    deeplinkBubblesWriteMessage = {},
                )
            }
            block()
        }
    }
}
