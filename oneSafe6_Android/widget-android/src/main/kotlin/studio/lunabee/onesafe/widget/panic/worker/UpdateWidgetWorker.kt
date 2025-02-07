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
 *
 * Created by Lunabee Studio / Date - 9/26/2024 - for the oneSafe6 SDK.
 * Last modified 26/09/2024 15:47
 */

package studio.lunabee.onesafe.widget.panic.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.UpdatePanicButtonWidgetUseCase
import javax.inject.Inject

/**
 * Used to force update the widget when the app is updated from store.
 * The widget crashes on some android launcher otherwise
 */
@AndroidEntryPoint
class UpdateWidgetWorker : BroadcastReceiver() {
    @Inject lateinit var updatePanicButtonWidgetUseCase: UpdatePanicButtonWidgetUseCase

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_MY_PACKAGE_REPLACED -> CoroutineScope(Dispatchers.Default).launch { updatePanicButtonWidgetUseCase() }
            else -> {}
        }
    }
}
