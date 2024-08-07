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
 * Created by Lunabee Studio / Date - 10/2/2023 - for the oneSafe6 SDK.
 * Last modified 10/2/23, 11:15 AM
 */

package studio.lunabee.onesafe.importexport.model

import studio.lunabee.onesafe.domain.model.safe.SafeId
import java.time.Instant

sealed class Backup : Comparable<Backup> {
    abstract val date: Instant
    abstract val id: String
    abstract val safeId: SafeId?
    override fun compareTo(other: Backup): Int {
        val result = date.compareTo(other.date)
        return when {
            result == 0 && other is LocalBackup && this !is LocalBackup -> -1
            else -> result
        }
    }
}
