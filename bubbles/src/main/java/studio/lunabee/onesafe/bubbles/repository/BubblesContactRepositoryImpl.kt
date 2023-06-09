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
 * Created by Lunabee Studio / Date - 5/22/2023 - for the oneSafe6 SDK.
 * Last modified 5/22/23, 3:14 PM
 */

package studio.lunabee.onesafe.bubbles.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.invoke
import studio.lunabee.onesafe.bubbles.domain.model.BubblesContact
import studio.lunabee.onesafe.bubbles.domain.model.EncBubblesContactInfo
import studio.lunabee.onesafe.bubbles.domain.model.EncBubblesKey
import studio.lunabee.onesafe.bubbles.domain.model.PlainBubblesContact
import studio.lunabee.onesafe.bubbles.domain.repository.BubblesContactRepository
import studio.lunabee.onesafe.bubbles.repository.datasource.BubblesContactLocalDataSource
import studio.lunabee.onesafe.domain.repository.MainCryptoRepository
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class BubblesContactRepositoryImpl @Inject constructor(
    private val localDataSource: BubblesContactLocalDataSource,
    private val cryptoRepository: MainCryptoRepository,
) : BubblesContactRepository {

    override suspend fun storeContactsList(contacts: List<BubblesContact>) {
        localDataSource.storeContactsList(contacts)
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun encryptPlainContact(contact: PlainBubblesContact): BubblesContact =
        BubblesContact(
            id = contact.id,
            encName = cryptoRepository.encryptForBubblesContact(contact.name.encodeToByteArray()),
            encKey = cryptoRepository.encryptForBubblesContact(Base64.decode(contact.key)),
            updatedAt = Instant.now(),
        )

    override fun getAllContactsFlow(): Flow<List<EncBubblesContactInfo>> =
        localDataSource.getAllContactsFlow()

    override suspend fun getEncKeysList(): List<EncBubblesKey> = localDataSource.getEncKeysList()

    override suspend fun getContact(id: UUID): EncBubblesContactInfo? = localDataSource.getContact(id)

    override suspend fun getEncContactKey(id: UUID): ByteArray? = localDataSource.getEncContactKey(id)
}
