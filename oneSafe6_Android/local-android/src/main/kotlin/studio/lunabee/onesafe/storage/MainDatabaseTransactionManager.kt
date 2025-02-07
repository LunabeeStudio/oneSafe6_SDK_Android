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
import androidx.room.withTransaction
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import studio.lunabee.onesafe.domain.qualifier.FileDispatcher
import studio.lunabee.onesafe.domain.repository.StorageManager
import javax.inject.Inject

class MainDatabaseTransactionManager @Inject constructor(
    private val mainDatabase: MainDatabase,
    @ApplicationContext private val context: Context,
    @FileDispatcher private val dispatcher: CoroutineDispatcher,
) : StorageManager {
    override suspend fun withTransaction(block: suspend () -> Unit) {
        mainDatabase.withTransaction(block)
    }

    override suspend fun deleteStorage() {
        withContext(dispatcher) {
            context.deleteDatabase(mainDatabase.openHelper.databaseName)
        }
    }
}
