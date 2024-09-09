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

package studio.lunabee.onesafe.storage.utils

import android.database.SQLException
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import studio.lunabee.onesafe.error.OSStorageError

private val logger = LBLogger.get("SQLHelper")

internal inline fun <R> runSQL(block: () -> R): R {
    return try {
        block()
    } catch (e: SQLException) {
        logger.e(e)
        throw OSStorageError(OSStorageError.Code.UNKNOWN_DATABASE_ERROR, cause = e)
    }
}

fun queryNumEntries(db: SupportSQLiteDatabase, table: String): Int {
    return db.query("SELECT COUNT(*) FROM $table").apply {
        moveToFirst()
    }.getInt(0)
}
