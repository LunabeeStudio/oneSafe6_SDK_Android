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

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.storage.converter.DurationConverter
import studio.lunabee.onesafe.storage.converter.FileConverter
import studio.lunabee.onesafe.storage.converter.InstantConverter
import studio.lunabee.onesafe.storage.converter.ZonedDateTimeConverter
import studio.lunabee.onesafe.storage.dao.AutoBackupErrorDao
import studio.lunabee.onesafe.storage.dao.BackupDao
import studio.lunabee.onesafe.storage.dao.ContactDao
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetConversationDao
import studio.lunabee.onesafe.storage.dao.DoubleRatchetKeyDao
import studio.lunabee.onesafe.storage.dao.EnqueuedMessageDao
import studio.lunabee.onesafe.storage.dao.HandShakeDataDao
import studio.lunabee.onesafe.storage.dao.IndexWordEntryDao
import studio.lunabee.onesafe.storage.dao.MessageDao
import studio.lunabee.onesafe.storage.dao.SafeDao
import studio.lunabee.onesafe.storage.dao.SafeFileDao
import studio.lunabee.onesafe.storage.dao.SafeItemDao
import studio.lunabee.onesafe.storage.dao.SafeItemFieldDao
import studio.lunabee.onesafe.storage.dao.SafeItemKeyDao
import studio.lunabee.onesafe.storage.dao.SafeItemRawDao
import studio.lunabee.onesafe.storage.dao.SentMessageDao
import studio.lunabee.onesafe.storage.dao.SettingsDao
import studio.lunabee.onesafe.storage.migration.RoomMigrationSpec11to12
import studio.lunabee.onesafe.storage.model.RoomAutoBackupError
import studio.lunabee.onesafe.storage.model.RoomBackup
import studio.lunabee.onesafe.storage.model.RoomContact
import studio.lunabee.onesafe.storage.model.RoomContactKey
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetConversation
import studio.lunabee.onesafe.storage.model.RoomDoubleRatchetKey
import studio.lunabee.onesafe.storage.model.RoomEnqueuedMessage
import studio.lunabee.onesafe.storage.model.RoomHandShakeData
import studio.lunabee.onesafe.storage.model.RoomIndexWordEntry
import studio.lunabee.onesafe.storage.model.RoomMessage
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeFile
import studio.lunabee.onesafe.storage.model.RoomSafeItem
import studio.lunabee.onesafe.storage.model.RoomSafeItemField
import studio.lunabee.onesafe.storage.model.RoomSafeItemKey
import studio.lunabee.onesafe.storage.model.RoomSentMessage
import studio.lunabee.onesafe.storage.utils.addRecursiveCheckTriggers
import studio.lunabee.onesafe.storage.utils.addUniqueBiometricKeyTrigger

@TypeConverters(InstantConverter::class, FileConverter::class, DurationConverter::class, ZonedDateTimeConverter::class)
@Database(
    version = 17,
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
        RoomSentMessage::class,
        RoomBackup::class,
        RoomSafe::class,
        RoomAutoBackupError::class,
        RoomSafeFile::class,
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12, spec = RoomMigrationSpec11to12::class),
        AutoMigration(from = 14, to = 15),
    ],
)
abstract class MainDatabase : RoomDatabase() {
    abstract fun safeItemDao(): SafeItemDao
    abstract fun safeItemRawDao(): SafeItemRawDao
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
    abstract fun sentMessageDao(): SentMessageDao
    abstract fun backupDao(): BackupDao
    abstract fun safeDao(): SafeDao
    abstract fun settingsDao(): SettingsDao
    abstract fun autoBackupErrorDao(): AutoBackupErrorDao
    abstract fun safeFileDao(): SafeFileDao

    companion object {
        fun build(
            appContext: Context,
            dbKey: DatabaseKey?,
            mainDatabaseName: String,
            vararg migrations: Migration,
        ): MainDatabase {
            val builder = Room.databaseBuilder(
                appContext,
                MainDatabase::class.java,
                mainDatabaseName,
            )
                .addCallback(MainDatabaseCallback())
                .addMigrations(*migrations)
                .openHelperFactory(SupportOpenHelperFactory(dbKey?.raw))

            return builder.build()
        }
    }
}

class MainDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.addRecursiveCheckTriggers()
        db.addUniqueBiometricKeyTrigger()
    }
}
