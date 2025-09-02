/*
 * Copyright (c) 2025 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/16/2025 - for the oneSafe6 SDK.
 * Last modified 6/16/25, 9:29 AM
 */

package studio.lunabee.onesafe.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import javax.inject.Inject

class RoomMigration24to25 @Inject constructor() : Migration(24, 25) {

    /**
     * Add missing index on foreign keys (safe_id)
     */
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Backup_safe_id` ON `Backup` (`safe_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_RecentSearch_safe_id` ON `RecentSearch` (`safe_id`)")
    }
}
