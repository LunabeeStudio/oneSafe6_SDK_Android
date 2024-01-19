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
 * Created by Lunabee Studio / Date - 10/3/2023 - for the oneSafe6 SDK.
 * Last modified 10/3/23, 4:02 PM
 */

package studio.lunabee.onesafe.storage.utils

import android.os.Build
import android.os.FileObserver
import java.io.File

object FileObserverCompat {
    fun get(file: File, mask: Int, onEvent: (event: Int, path: String?) -> Unit): FileObserver {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(file, mask) {
                override fun onEvent(event: Int, path: String?) {
                    onEvent(event, path)
                }
            }
        } else {
            @Suppress("DEPRECATION")
            object : FileObserver(file.path, mask) {
                override fun onEvent(event: Int, path: String?) {
                    onEvent(event, path)
                }
            }
        }
    }
}
