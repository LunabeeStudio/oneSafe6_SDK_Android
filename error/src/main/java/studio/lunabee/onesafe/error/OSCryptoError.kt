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

package studio.lunabee.onesafe.error

data class OSCryptoError(
    val code: Code,
    override val message: String = code.message,
    override val cause: Throwable? = null,
) : OSError(message, cause) {

    enum class Code(val message: String) {
        MASTER_KEY_ALREADY_GENERATED("A master key have already been generated"),
        MASTER_KEY_NOT_LOADED("Master key not loaded in memory"),
        MASTER_KEY_ALREADY_LOADED("Master key already loaded in memory"),
        DECRYPTION_FAILED_WRONG_KEY("Unable to decrypt with the provided key"),
        ENCRYPTION_FAILED_BAD_KEY("Unable to encrypt with the provided key"),
        ILLEGAL_VALUE("Unexpected value"),
        MISSING_MAPPER("No ByteArray mapper found"),
        DECRYPTION_UNKNOWN_FAILURE("Unable to decrypt the data"),
        MASTER_KEY_WRONG_PASSWORD("Unable to load the master key with the provided password"),
        MASTER_KEY_NOT_GENERATED("No master key found"),
        MASTER_SALT_ALREADY_LOADED("Master salt already loaded in memory"),
        BIOMETRIC_KEY_NOT_GENERATED("No biometric key found"),
        BIOMETRIC_DECRYPTION_FAILED("Unable to decrypt masterKey with biometric"),
        BIOMETRIC_KEY_GENERATION_ERROR("Unable to generate key for biometric"),
        BIOMETRIC_KEY_INVALIDATE("Biometric Key permanently invalidated"),
        DATASTORE_KEY_PERMANENTLY_INVALIDATE("Datastore Key permanently invalidated"),
        BIOMETRIC_DECRYPTION_NOT_AUTHENTICATED("retrieve Key user not authenticated"),
        IV_ALREADY_USED("IV has already been used"),
        BIOMETRIC_MASTER_KEY_ALREADY_GENERATED("A master key have already been generated and encrypted for biometric"),
        BIOMETRIC_IV_ALREADY_GENERATED("A IV have already been generated for biometric"),
        KEYSTORE_KEY_NOT_GENERATED("try to retrieve Key not generated"),
        KEYSTORE_KEY_ERROR_CREATION_ERROR("error during key store key creation"),
        SALT_ALREADY_GENERATED("A Master salt has already beed generated"),
        PROTO_DATASTORE_READ_ERROR("Cannot read datastore proto"),
        SEARCH_INDEX_KEY_ALREADY_GENERATED("Search index key have already been generated"),
        SEARCH_INDEX_KEY_NOT_LOADED("Search index key not loaded in memory"),
        SEARCH_INDEX_KEY_ALREADY_LOADED("Search index key already loaded in memory"),
        DERIVATION_WITH_EMPTY_PASSWORD("Entered password is empty"),
        MIGRATION_KEYPAIR_NOT_LOADED("KeyPair for migration has not be generated"),
        ONBOARDING_SALT_NOT_LOADED("The salt has not been generated"),
        ONBOARDING_KEY_NOT_LOADED("The key has not been generated"),
        BUBBLES_CONTACT_KEY_ALREADY_GENERATED("A Bubble contact key have already been generated"),
        BUBBLES_MASTER_KEY_NOT_LOADED("Bubble contact key not loaded in memory"),
        BUBBLES_MASTER_KEY_ALREADY_LOADED("Bubble contact key already loaded in memory"),
        BUBBLES_ENCRYPTION_FAILED_BAD_KEY("Unable to encrypt with the provided key"),
        BUBBLES_DECRYPTION_FAILED_WRONG_KEY("Unable to decrypt with the provided key"),
        BUBBLES_ENCRYPTION_FAILED_BAD_CONTACT_KEY("Unable to encrypt with the provided key"),
        BUBBLES_DECRYPTION_FAILED_WRONG_CONTACT_KEY("Unable to decrypt with the provided key"),
    }
}
