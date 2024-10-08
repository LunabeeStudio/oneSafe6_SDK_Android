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
 * Created by Lunabee Studio / Date - 8/30/2023 - for the oneSafe6 SDK.
 * Last modified 8/30/23, 12:21 PM
 */

package studio.lunabee.onesafe.ime.ui

import android.content.Context
import studio.lunabee.onesafe.ime.ImeDeeplinkHelper
import studio.lunabee.onesafe.messaging.writemessage.screen.WriteMessageNavScope
import java.util.UUID

class ImeWriteMessageNav(dismissUi: () -> Unit, context: Context) : WriteMessageNavScope {
    override val navigationToInvitation: (UUID) -> Unit = {
        /* no-op */
    }
    override val navigateToContactDetail: (UUID) -> Unit = {
        /* no-op */
    }
    override val navigateBack: () -> Unit = dismissUi

    override val deeplinkBubblesWriteMessage: ((contactId: UUID) -> Unit) = { contactId ->
        ImeDeeplinkHelper.deeplinkBubblesWriteMessage(context, contactId)
        dismissUi()
    }
    override val navigateToItemDetail: (UUID) -> Unit = {}
}
