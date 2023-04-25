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
 * Created by Lunabee Studio / Date - 4/7/2023 - for the oneSafe6 SDK.
 * Last modified 4/7/23, 12:24 AM
 */

package studio.lunabee.onesafe.storage

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import studio.lunabee.onesafe.storage.converter.InstantConverter
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.model.RoomIndexWordEntry
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.storage.model.RoomSafeItemKey

@TypeConverters(InstantConverter::class)
@Database(
    version = 2,
    entities = [
        RoomSafeItem::class,
        RoomSafeItemField::class,
        RoomIndexWordEntry::class,
        RoomSafeItemKey::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun safeItemDao(): SafeItemDao
    abstract fun safeItemFieldDao(): SafeItemFieldDao
    abstract fun searchIndexDao(): IndexWordEntryDao
    abstract fun safeItemKeyDao(): SafeItemKeyDao
}
