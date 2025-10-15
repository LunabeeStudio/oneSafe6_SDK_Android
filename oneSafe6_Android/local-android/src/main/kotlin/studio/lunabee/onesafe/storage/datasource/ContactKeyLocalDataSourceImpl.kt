/*
 * Copyright (c) 2023-2023 Lunabee Studio
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
 * Created by Lunabee Studio / Date - 6/14/2023 - for the oneSafe6 SDK.
 * Last modified 6/14/23, 12:19 PM
 */

package studio.lunabee.onesafe.storage.datasource

import studio.lunabee.bubbles.domain.model.contactkey.ContactLocalKey
import studio.lunabee.bubbles.repository.datasource.ContactKeyLocalDataSource
import studio.lunabee.doubleratchet.model.DoubleRatchetUUID
import studio.lunabee.onesafe.error.OSStorageError
import studio.lunabee.onesafe.storage.dao.ContactKeyDao
import javax.inject.Inject

class ContactKeyLocalDataSourceImpl @Inject constructor(
    private val dao: ContactKeyDao,
) : ContactKeyLocalDataSource {
    override suspend fun getContactLocalKey(contactId: DoubleRatchetUUID): ContactLocalKey = dao
        .getById(contactId.uuid)
        ?.let(::ContactLocalKey)
        ?: throw OSStorageError(OSStorageError.Code.CONTACT_KEY_NOT_FOUND)
}
