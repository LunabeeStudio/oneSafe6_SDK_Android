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
 * Created by Lunabee Studio / Date - 6/12/2024 - for the oneSafe6 SDK.
 * Last modified 6/12/24, 9:39 AM
 */

package studio.lunabee.onesafe.storage.migration

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.core.database.getBlobOrNull
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import studio.lunabee.onesafe.domain.common.MessageIdProvider
import studio.lunabee.onesafe.jvm.toByteArray
import javax.inject.Inject

class RoomMigration3to4 @Inject constructor(private val idProvider: MessageIdProvider) : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE `TEMP_Message` (`id` BLOB NOT NULL, `contact_id` BLOB NOT NULL, `enc_sent_at` BLOB NOT NULL, " +
                "`enc_content` BLOB NOT NULL, `direction` TEXT NOT NULL, `order` REAL NOT NULL, `enc_channel` BLOB, " +
                "PRIMARY KEY(`id`), FOREIGN KEY(`contact_id`) REFERENCES `Contact`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )

        val cursor = db.query("SELECT * FROM Message")
        while (cursor.moveToNext()) {
            val id = idProvider().toByteArray()
            val values = ContentValues().apply {
                put("id", id)
                put("contact_id", cursor.getBlob(1))
                put("enc_sent_at", cursor.getBlob(2))
                put("enc_content", cursor.getBlob(3))
                put("direction", cursor.getString(4))
                put("`order`", cursor.getFloat(5))
                put("enc_channel", cursor.getBlobOrNull(6))
            }
            db.insert("TEMP_Message", SQLiteDatabase.CONFLICT_FAIL, values)
        }

        db.execSQL("DROP TABLE `Message`")
        db.execSQL("DROP INDEX IF EXISTS `index_Message_order_contact_id`")
        db.execSQL("DROP INDEX IF EXISTS `index_Message_contact_id`")
        db.execSQL("ALTER TABLE TEMP_Message RENAME TO Message")
        db
            .execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_Message_order_contact_id` ON Message (`order` DESC, `contact_id` DESC)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Message_contact_id` ON Message (`contact_id`)")
    }
}
