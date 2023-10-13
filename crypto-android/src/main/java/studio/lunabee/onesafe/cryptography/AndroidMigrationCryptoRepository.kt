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

package studio.lunabee.onesafe.cryptography

import studio.lunabee.onesafe.cryptography.extension.use
import studio.lunabee.onesafe.domain.repository.MigrationCryptoRepository
import studio.lunabee.onesafe.error.OSCryptoError
import studio.lunabee.onesafe.toCharArray
import studio.lunabee.onesafe.use
import java.security.KeyPair
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidMigrationCryptoRepository @Inject constructor(
    private val cryptoEngine: RsaCryptoEngine,
) : MigrationCryptoRepository {
    private var keyPair: KeyPair? = null

    override fun getMigrationPubKey(): ByteArray {
        return cryptoEngine.getKeyPair().also {
            keyPair = it
        }.public.encoded
    }

    @Throws(OSCryptoError::class)
    override fun decryptMigrationArchivePassword(encPassword: ByteArray): CharArray {
        val privateKey = keyPair?.private ?: throw OSCryptoError(OSCryptoError.Code.MIGRATION_KEYPAIR_NOT_LOADED)
        return privateKey.use {
            encPassword.use {
                cryptoEngine.decrypt(privateKey, encPassword).toCharArray()
            }
        }
    }
}
