/*
 * Copyright (c) 2024 Lunabee Studio
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
 */

package studio.lunabee.bubbles.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import studio.lunabee.bubbles.domain.usecase.ContactLocalDecryptUseCase
import studio.lunabee.bubbles.domain.usecase.CreateContactUseCase
import studio.lunabee.bubbles.domain.usecase.DecryptContactNameUseCase
import studio.lunabee.bubbles.domain.usecase.DecryptContactSharingModeUseCase
import studio.lunabee.bubbles.domain.usecase.GetAllContactsUseCase
import studio.lunabee.bubbles.domain.usecase.GetContactUseCase
import studio.lunabee.bubbles.domain.usecase.GetRecentContactsUseCase
import studio.lunabee.bubbles.domain.usecase.UpdateContactUseCase
import studio.lunabee.bubbles.domain.usecase.UpdateMessageSharingModeContactUseCase
import studio.lunabee.messaging.domain.usecase.CreateInvitationUseCase
import studio.lunabee.messaging.domain.usecase.GetInvitationMessageUseCase

val bubblesUseCaseModule: Module = module {
    singleOf(::ContactLocalDecryptUseCase)
    singleOf(::CreateContactUseCase)
    singleOf(::GetAllContactsUseCase)
    singleOf(::GetContactUseCase)
    singleOf(::GetRecentContactsUseCase)
    singleOf(::UpdateContactUseCase)
    singleOf(::UpdateMessageSharingModeContactUseCase)
    singleOf(::CreateInvitationUseCase)
    singleOf(::GetInvitationMessageUseCase)
    singleOf(::DecryptContactNameUseCase)
    singleOf(::DecryptContactSharingModeUseCase)
}

class BubblesUseCases : KoinComponent {
    val contactLocalDecryptUseCase: ContactLocalDecryptUseCase by inject()
    val createContactUseCase: CreateContactUseCase by inject()
    val getAllContactsUseCase: GetAllContactsUseCase by inject()
    val getContactUseCase: GetContactUseCase by inject()
    val getRecentContactsUseCase: GetRecentContactsUseCase by inject()
    val updateContactUseCase: UpdateContactUseCase by inject()
    val updateMessageSharingModeContactUseCase: UpdateMessageSharingModeContactUseCase by inject()
    val createInvitationUseCase: CreateInvitationUseCase by inject()
    val getInvitationMessageUseCase: GetInvitationMessageUseCase by inject()
    val decryptContactNameUseCase: DecryptContactNameUseCase by inject()
    val decryptContactSharingModeUseCase: DecryptContactSharingModeUseCase by inject()
}
