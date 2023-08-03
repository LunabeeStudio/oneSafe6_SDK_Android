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
 * Created by Lunabee Studio / Date - 6/30/2023 - for the oneSafe6 SDK.
 * Last modified 6/30/23, 11:35 AM
 */

package studio.lunabee.onesafe.ime

import android.content.ClipboardManager
import android.content.Context
import com.lunabee.lbcore.model.LBResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.messaging.domain.repository.MessageChannelRepository
import studio.lunabee.onesafe.messaging.domain.usecase.HandleIncomingMessageUseCase
import studio.lunabee.onesafe.messaging.domain.usecase.IncomingMessageState
import javax.inject.Inject
import kotlin.math.abs

class DecryptClipboardListener @Inject constructor(
    @ApplicationContext context: Context,
    private val handleIncomingMessageUseCase: HandleIncomingMessageUseCase,
    private val channelRepository: MessageChannelRepository,
) : ClipboardManager.OnPrimaryClipChangedListener {
    private val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    private var lastClipCall: Long = 0L
    private val lifecycleScope = MainScope()

    private val _result: MutableSharedFlow<LBResult<IncomingMessageState>> = MutableSharedFlow()
    val result: SharedFlow<LBResult<IncomingMessageState>> = _result.asSharedFlow()

    override fun onPrimaryClipChanged() {
        // Avoid potential multiple call
        if (abs(lastClipCall - System.currentTimeMillis()) < 500) {
            return
        }
        lastClipCall = System.currentTimeMillis()

        val primaryClip = clipboard.primaryClip
        primaryClip?.getItemAt(0)?.text?.toString()?.let { clipText ->
            lifecycleScope.launch {
                _result.emit(handleIncomingMessageUseCase(clipText, channelRepository.channel))
            }
        }
    }
}
