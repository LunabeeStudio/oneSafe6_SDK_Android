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
 * Created by Lunabee Studio / Date - 7/30/2024 - for the oneSafe6 SDK.
 * Last modified 7/30/24, 9:51 AM
 */

package studio.lunabee.onesafe.debug

import android.os.Build
import android.os.FileObserver
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

interface FlowFileCounter {
    val flow: StateFlow<Int>
}

@RequiresApi(Build.VERSION_CODES.Q)
class FlowFileCounterObserver(private val file: File) : FileObserver(file), FlowFileCounter {

    init {
        file.mkdirs()
        startWatching()
    }

    private val _flow: MutableStateFlow<Int> = MutableStateFlow(0)
    override val flow: StateFlow<Int> = _flow.asStateFlow()

    override fun onEvent(event: Int, path: String?) {
        if (event >= MOVED_FROM) {
            _flow.value = file.list()?.size ?: 0
        }
    }
}

fun flowFileCounter(file: File): FlowFileCounter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    FlowFileCounterObserver(file)
} else {
    object : FlowFileCounter {
        override val flow: StateFlow<Int> = MutableStateFlow(-1)
    }
}
