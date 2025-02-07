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
 * Last modified 6/12/24, 9:38 AM
 */

package studio.lunabee.onesafe.storage.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import studio.lunabee.onesafe.domain.usecase.settings.DefaultSafeSettingsProvider
import studio.lunabee.onesafe.storage.model.RoomSafe
import studio.lunabee.onesafe.storage.model.RoomSafeSettings
import javax.inject.Inject

class RoomMigration13to14 @Inject constructor(
    private val defaultSafeSettingsProvider: DefaultSafeSettingsProvider,
) : Migration(13, 14) {

    /**
     * Add a column [RoomSafeSettings.shakeToLock] to the [RoomSafeSettings] embedded in [RoomSafe]
     */
    override fun migrate(db: SupportSQLiteDatabase) {
        val default = defaultSafeSettingsProvider()
        db.execSQL("ALTER TABLE Safe ADD COLUMN setting_shake_to_lock INTEGER NOT NULL DEFAULT ${default.shakeToLock.compareTo(false)}")
    }
}
