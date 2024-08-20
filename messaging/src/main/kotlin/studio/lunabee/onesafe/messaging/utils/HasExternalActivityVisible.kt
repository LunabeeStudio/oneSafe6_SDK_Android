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
 * Created by Lunabee Studio / Date - 8/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/08/2024 14:30
 */

package studio.lunabee.onesafe.messaging.utils

import android.app.ActivityManager
import android.os.Build

fun hasExternalActivityVisible(activityManager: ActivityManager): Boolean {
    val taskInfo = activityManager.appTasks.firstOrNull()?.taskInfo ?: return false
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 -> taskInfo.isVisible
        // isVisible seems to exist but cannot be access (tested on API 31)
        taskInfo.toString().contains("isVisible=") -> taskInfo.toString().contains("isVisible=true")
        // Only check if we have an external activity opened (= not the MainActivity)
        taskInfo.topActivity?.className != "studio.lunabee.onesafe.MainActivity" -> true
        else -> false
    }
}
