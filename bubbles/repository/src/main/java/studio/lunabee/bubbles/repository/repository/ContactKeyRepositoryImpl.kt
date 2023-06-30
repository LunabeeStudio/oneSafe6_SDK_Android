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
 * Created by Lunabee Studio / Date - 6/22/2023 - for the oneSafe6 SDK.
 * Last modified 6/22/23, 10:52 AM
 */

package studio.lunabee.bubbles.repository.repository

import studio.lunabee.bubbles.repository.datasource.ContactKeyLocalDataSource
import studio.lunabee.onesafe.bubbles.domain.repository.ContactKeyRepository
import studio.lunabee.onesafe.bubbles.domain.model.ContactLocalKey
import java.util.UUID
import javax.inject.Inject

class ContactKeyRepositoryImpl @Inject constructor(
    private val localDataSource: ContactKeyLocalDataSource,
) : ContactKeyRepository {

    override suspend fun getContactLocalKey(contactId: UUID): ContactLocalKey =
        localDataSource.getContactLocalKey(contactId)
}
