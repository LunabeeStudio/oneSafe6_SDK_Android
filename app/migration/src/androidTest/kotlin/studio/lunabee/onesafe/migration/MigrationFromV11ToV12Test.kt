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
 * Created by Lunabee Studio / Date - 3/12/2024 - for the oneSafe6 SDK.
 * Last modified 12/03/2024 09:05
 */

package studio.lunabee.onesafe.migration

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import studio.lunabee.onesafe.cryptography.PasswordHashEngine
import studio.lunabee.onesafe.cryptography.SaltProvider
import studio.lunabee.onesafe.domain.model.crypto.DecryptEntry
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemKeyRepository
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.migration.migration.MigrationFromV11ToV12
import studio.lunabee.onesafe.test.CommonTestUtils
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@HiltAndroidTest
class MigrationFromV11ToV12Test : OSHiltTest() {

    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var migrationFromV11ToV12: MigrationFromV11ToV12

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var fieldRepository: SafeItemFieldRepository

    @Inject lateinit var safeItemKeyRepository: SafeItemKeyRepository

    private val salt: ByteArray = OSTestConfig.random.nextBytes(32)

    @Inject lateinit var hashEngine: PasswordHashEngine

    @BindValue
    val saltProvider: SaltProvider = mockk {
        every { this@mockk.invoke(any()) } returns salt.copyOf()
    }

    private suspend fun masterKey(): ByteArray = hashEngine.deriveKey(testPassword.toCharArray(), salt)

    @Test
    fun run_migrationFromV11ToV12_test(): TestResult = runTest {
        val item = createItemUseCase.test()
        val field = addFieldUseCase(
            itemId = item.id,
            itemFieldData = CommonTestUtils.createItemFieldData(item.id),
        ).data!!
        fieldRepository.saveThumbnailFileName(field.id, null)
        val updatedField = fieldRepository.getSafeItemField(field.id)
        assertNull(updatedField.encThumbnailFileName)
        migrationFromV11ToV12(masterKey(), firstSafeId)
        val updatedMigrationField = fieldRepository.getSafeItemField(field.id)
        val encThumbnailFileName = updatedMigrationField.encThumbnailFileName
        assertNotNull(encThumbnailFileName)

        val key = safeItemKeyRepository.getSafeItemKey(field.id)
        val thumbnailId = cryptoRepository.decrypt(key, DecryptEntry(encThumbnailFileName, UUID::class))
        assertEquals(UUID.fromString("2ae4e851-508b-456c-93b6-dd4236d6b6a1"), thumbnailId)
    }
}
