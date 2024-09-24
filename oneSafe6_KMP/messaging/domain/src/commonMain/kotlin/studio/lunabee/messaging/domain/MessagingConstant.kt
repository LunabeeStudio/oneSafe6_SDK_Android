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

package studio.lunabee.messaging.domain

object MessagingConstant {
    // Used to determine that the message is the invitation response message
    const val FirstMessageData: String = "c08b1cb8-3a94-4e9a-af30-6677053b7a60"
    const val SafeItemMessageData: String = "388188db-8d10-41d3-94a7-7fb8e821fa59"
    const val ResetConversationMessageData: String = "d8eaf592-9737-4b0d-bda1-b1c96d000aaf"
    const val MessageFileName: String = "message"
    const val AttachmentFileName: String = "attachment"
}
