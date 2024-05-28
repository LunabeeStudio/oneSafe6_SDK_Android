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
 * Created by Lunabee Studio / Date - 5/23/2024 - for the oneSafe6 SDK.
 * Last modified 5/23/24, 8:40 AM
 */

package studio.lunabee.onesafe.migration

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lblogger.LBLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.model.LocalCtaState
import studio.lunabee.onesafe.model.LocalCtaStateMap
import studio.lunabee.onesafe.model.edit
import studio.lunabee.onesafe.storage.datastore.ProtoSerializer
import javax.inject.Inject

private val logger = LBLogger.get<MigrationFromV11ToV12>()

/**
 * Migrate single CTA (autobackup) to a map of CTA in datastore
 */
class MigrationFromV12ToV13 @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ctaMapDataStore: DataStore<LocalCtaStateMap>,
) {
    suspend operator fun invoke(): LBResult<Unit> = OSError.runCatching(logger) {
        val datastore = ProtoSerializer.dataStore<LocalCtaState>(context, LocalCtaState.Hidden, CtaDataStore)
        datastore.data.firstOrNull()?.let { backupCtaState ->
            ctaMapDataStore.edit { it[backupCtaKeyVal] = backupCtaState }
        }
        context.dataStoreFile(CtaDataStore).delete()
    }

    companion object {
        private const val CtaDataStore: String = "b0e3c5a2-7959-4121-b664-372e544252cd"
        private const val backupCtaKeyVal: String = "1aa3c807-e989-4b63-ae1c-e020daa4a569"
    }
}
