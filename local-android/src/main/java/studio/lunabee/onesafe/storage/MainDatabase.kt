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
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetConversationDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetKeyDao
import studio.lunabee.onesafe.storage.dao.EnqueuedMessageDao
import studio.lunabee.onesafe.storage.dao.HandShakeDataDao
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.model.RoomContact
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetConversation
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetKey
import studio.lunabee.onesafe.storage.model.RoomEnqueuedMessage
import studio.lunabee.onesafe.storage.model.RoomHandShakeData
import studio.lunabee.onesafe.storage.model.RoomIndexWordEntry
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.storage.model.RoomSafeItemKey

@TypeConverters(InstantConverter::class)
@Database(
    version = 3,
    entities = [
        RoomSafeItem::class,
        RoomSafeItemField::class,
        RoomIndexWordEntry::class,
        RoomSafeItemKey::class,
        RoomContact::class,
        RoomMessage::class,
        RoomContactKey::class,
        RoomEnqueuedMessage::class,
        RoomDoubleRatchetKey::class,
        RoomDoubleRatchetConversation::class,
        RoomHandShakeData::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun safeItemDao(): SafeItemDao
    abstract fun safeItemFieldDao(): SafeItemFieldDao
    abstract fun searchIndexDao(): IndexWordEntryDao
    abstract fun safeItemKeyDao(): SafeItemKeyDao
    abstract fun contactDao(): ContactDao
    abstract fun contactKeyDao(): ContactKeyDao
    abstract fun messageDao(): MessageDao
    abstract fun enqueuedMessageDao(): EnqueuedMessageDao
    abstract fun doubleRatchetKeyDao(): DoubleRatchetKeyDao
    abstract fun doubleRatchetConversationDao(): DoubleRatchetConversationDao
    abstract fun handShakeDataDao(): HandShakeDataDao
}
