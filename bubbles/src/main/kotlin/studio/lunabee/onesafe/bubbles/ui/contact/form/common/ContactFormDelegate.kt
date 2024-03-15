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
import com.lunabee.lbloading.withLoading
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.commonui.utils.CloseableCoroutineScope
import studio.lunabee.onesafe.commonui.utils.CloseableMainCoroutineScope
import java.io.Closeable
import java.util.UUID

interface ContactFormDelegate : Closeable {
    val createInvitationResult: StateFlow<LBResult<UUID>?>
    fun saveContact(
        contactName: String,
        isUsingDeeplink: Boolean,
    )
}

abstract class DefaultContactFormDelegate(
    private val loadingManager: LoadingManager,
) : ContactFormDelegate, CloseableCoroutineScope by CloseableMainCoroutineScope() {

    final override fun saveContact(
        contactName: String,
        isUsingDeeplink: Boolean,
    ) {
        if (createInvitationResult.value !is LBResult.Success) {
            coroutineScope.launch {
                loadingManager.withLoading {
                    doSaveContact(contactName, isUsingDeeplink)
                }
            }
        }
    }

    protected abstract suspend fun doSaveContact(contactName: String, isUsingDeeplink: Boolean)
}
