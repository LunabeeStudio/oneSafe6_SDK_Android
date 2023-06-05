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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 11:21 AM
 */

package studio.lunabee.onesafe.bubbles.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import studio.lunabee.onesafe.bubbles.local.dao.BubblesContactDao
import studio.lunabee.onesafe.bubbles.local.model.RoomBubblesContact
import studio.lunabee.onesafe.storage.converter.InstantConverter

@TypeConverters(InstantConverter::class)
@Database(
    version = 1,
    entities = [
        RoomBubblesContact::class,
    ],
)
abstract class BubblesDatabase : RoomDatabase() {
    abstract fun bubblesContactDao(): BubblesContactDao
}
