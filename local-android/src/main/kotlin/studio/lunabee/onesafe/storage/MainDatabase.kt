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
import androidx.core.database.getBlobOrNull
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.storage.converter.FileConverter
import studio.lunabee.onesafe.storage.converter.InstantConverter
import studio.lunabee.onesafe.storage.dao.BackupDao
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
import studio.lunabee.onesafe.storage.dao.SafeItemRawDao
import studio.lunabee.onesafe.storage.dao.SentMessageDao
import studio.lunabee.onesafe.storage.model.RoomBackup
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
import studio.lunabee.onesafe.storage.model.RoomSentMessage
import studio.lunabee.onesafe.toByteArray
import javax.inject.Inject

@TypeConverters(InstantConverter::class, FileConverter::class)
@Database(
    version = 11,
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
    ],
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
        AutoMigration(from = 10, to = 11),
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

private fun SupportSQLiteDatabase.addRecursiveCheckTriggers() {
    execSQL(
        """
             CREATE TRIGGER IF NOT EXISTS recursive_item_insert
             BEFORE INSERT
             ON SafeItem
             WHEN NEW.id = NEW.parent_id OR NEW.id = NEW.deleted_parent_id
             BEGIN
                 SELECT RAISE(ABORT, 'Recursive item forbidden');
             END;
             """,
    )
    execSQL(
        """
             CREATE TRIGGER IF NOT EXISTS recursive_item_update
             BEFORE UPDATE OF parent_id, deleted_parent_id
             ON SafeItem
             WHEN NEW.id = NEW.parent_id OR NEW.id = NEW.deleted_parent_id
             BEGIN
                 SELECT RAISE(ABORT, 'Recursive item forbidden');
             END;
             """,
    )
}

class MainDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        db.addRecursiveCheckTriggers()
    }
}

class Migration3to4 @Inject constructor(private val idProvider: MessageIdProvider) : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE `TEMP_Message` (`id` BLOB NOT NULL, `contact_id` BLOB NOT NULL, `enc_sent_at` BLOB NOT NULL, " +
                "`enc_content` BLOB NOT NULL, `direction` TEXT NOT NULL, `order` REAL NOT NULL, `enc_channel` BLOB, " +
                "PRIMARY KEY(`id`), FOREIGN KEY(`contact_id`) REFERENCES `Contact`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )

        val cursor = db.query("SELECT * FROM Message")
        while (cursor.moveToNext()) {
            val id = idProvider().toByteArray()
            db.execSQL(
                "INSERT INTO TEMP_Message (id,contact_id,enc_sent_at,enc_content,direction,`order`,enc_channel) " +
                    "VALUES (" +
                    "${id.toSqlBlobString()}," +
                    "${cursor.getBlob(1).toSqlBlobString()}," +
                    "${cursor.getBlob(2).toSqlBlobString()}," +
                    "${cursor.getBlob(3).toSqlBlobString()}," +
                    "'${cursor.getString(4)}'," +
                    "${cursor.getFloat(5)}," +
                    "${cursor.getBlobOrNull(6)?.toSqlBlobString()}" +
                    ")",
            )
        }

        db.execSQL("DROP TABLE `Message`")
        db.execSQL("DROP INDEX IF EXISTS `index_Message_order_contact_id`")
        db.execSQL("DROP INDEX IF EXISTS `index_Message_contact_id`")
        db.execSQL("ALTER TABLE TEMP_Message RENAME TO Message")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Message_order_contact_id` ON Message (`order` DESC, `contact_id` DESC)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Message_contact_id` ON Message (`contact_id`)")
    }
}

class Migration8to9 @Inject constructor() : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE SafeItem ADD created_at INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_SafeItem_created_at` ON `SafeItem` (`created_at`)")
        db.execSQL("UPDATE SafeItem SET created_at = updated_at")
    }
}

class Migration9to10 @Inject constructor() : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE SafeItem SET parent_id = NULL WHERE id = parent_id")
        db.execSQL("UPDATE SafeItem SET deleted_parent_id = NULL WHERE id = deleted_parent_id")
        db.addRecursiveCheckTriggers()
    }
}

private fun ByteArray.toSqlBlobString() = "X'${joinToString(separator = "") { byte -> "%02x".format(byte) }}'"
