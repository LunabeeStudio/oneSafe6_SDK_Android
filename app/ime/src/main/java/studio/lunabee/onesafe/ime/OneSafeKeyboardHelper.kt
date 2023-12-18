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
 * Created by Lunabee Studio / Date - 6/30/2023 - for the oneSafe6 SDK.
 * Last modified 6/5/23, 4:13 PM
 */

package studio.lunabee.onesafe.ime

import android.content.ComponentName
import android.content.Context

object OneSafeKeyboardHelper {

    fun parseIsOneSafeKeyboardEnabled(context: Context, activeImeIds: String): Boolean {
        return activeImeIds.split(Delimiter).map { componentStr ->
            ComponentName.unflattenFromString(componentStr)
        }.any { it?.packageName == context.packageName && it?.className == OSFlorisImeService::class.qualifiedName }
    }

    fun parseIsOneSafeKeyboardSelected(context: Context, selectedImeId: String): Boolean {
        val component = ComponentName.unflattenFromString(selectedImeId)
        return component?.packageName == context.packageName && component?.className == OSFlorisImeService::class.qualifiedName
    }

    const val Delimiter: Char = ':'
}
