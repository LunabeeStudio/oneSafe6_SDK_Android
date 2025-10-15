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
 * Created by Lunabee Studio / Date - 8/22/2023 - for the oneSafe6 SDK.
 * Last modified 22/08/2023 09:50
 */

package studio.lunabee.onesafe.bubbles.ui.contact.form.common

import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.LoadingManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import studio.lunabee.bubbles.domain.model.MessageSharingMode
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import java.io.Closeable

interface ContactFormDelegate : Closeable {
    val createInvitationResult: StateFlow<LBResult<DoubleRatchetUUID>?>

    fun saveContact(
        contactName: String,
        sharingMode: MessageSharingMode,
    )
}

abstract class DefaultContactFormDelegate(
    private val loadingManager: LoadingManager,
) : ContactFormDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {

    final override fun saveContact(
        contactName: String,
        sharingMode: MessageSharingMode,
    ) {
        if (createInvitationResult.value !is LBResult.Success) {
            coroutineScope.launch {
                loadingManager.withLoading {
                    doSaveContact(contactName, sharingMode)
                }
            }
        }
    }

    protected abstract suspend fun doSaveContact(contactName: String, sharingMode: MessageSharingMode)
}
