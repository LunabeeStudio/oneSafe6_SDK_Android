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
 * Created by Lunabee Studio / Date - 12/8/2023 - for the oneSafe6 SDK.
 * Last modified 12/8/23, 11:36 AM
 */

package studio.lunabee.onesafe.migration

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.test.platform.app.InstrumentationRegistry
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
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.item.AddAndRemoveFileUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.migration.migration.MigrationFromV9ToV10
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.colorInt
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
class MigrationFromV9ToV10Test : OSHiltTest() {
    @get:Rule override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)
    override val initialTestState: InitialTestState = InitialTestState.Home()

    private val salt: ByteArray = OSTestConfig.random.nextBytes(32)

    @Inject lateinit var hashEngine: PasswordHashEngine

    @BindValue
    val saltProvider: SaltProvider = mockk {
        every { this@mockk.invoke(any()) } returns salt.copyOf()
    }

    private suspend fun masterKey(): ByteArray = hashEngine.deriveKey(testPassword.toCharArray(), salt)

    @Inject lateinit var migrationFromV9ToV10: MigrationFromV9ToV10

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var addAndRemoveFileUseCase: AddAndRemoveFileUseCase

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun run_migration_v9_v10_test(): TestResult = runTest {
        val item = createItemUseCase.test()
        val image = createRandomImage()
        val filename = "field_file"
        val pngFile = File(context.cacheDir, filename)
        pngFile.outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val arrayOutputStream = ByteArrayOutputStream()
        arrayOutputStream.use {
            image.compress(Bitmap.CompressFormat.JPEG, 95, it)
        }
        val expectedJpegData = arrayOutputStream.toByteArray()

        val beforeMimetype = getMimetype(pngFile)
        val beforeSize = pngFile.length()
        assertEquals("image/png", beforeMimetype)

        val filedId = UUID.randomUUID()
        addAndRemoveFileUseCase(item, listOf(FileSavingData.ToSave(filedId) { pngFile.inputStream() }))

        val fieldId = UUID.randomUUID()
        val itemFieldData = createItemFieldData(
            id = fieldId,
            kind = SafeItemFieldKind.Photo,
            value = "$filedId|jpeg",
        )
        addFieldUseCase(item.id, itemFieldData)

        val result = migrationFromV9ToV10(masterKey(), firstSafeId)
        assertSuccess(result)

        val encFile = File(context.filesDir, "files/$filedId")
        val plainFileData = decryptUseCase(encFile.readBytes(), item.id, ByteArray::class).data!!

        val afterMimetype = getMimetype(plainFileData)
        val afterSize = plainFileData.size
        assertEquals("image/jpeg", afterMimetype)
        assertTrue(afterSize < beforeSize)
        assertContentEquals(expectedJpegData, plainFileData)
    }

    @Test
    fun run_migration_keep_real_png_v9_v10_test(): TestResult = runTest {
        val item = createItemUseCase.test()
        val image = createRandomImage()
        val filename = "field_file"
        val pngFile = File(context.cacheDir, filename)
        pngFile.outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        val expectedPngData = pngFile.readBytes()

        val beforeMimetype = getMimetype(pngFile)
        assertEquals("image/png", beforeMimetype)

        val filedId = UUID.randomUUID()
        addAndRemoveFileUseCase(item, listOf(FileSavingData.ToSave(filedId) { pngFile.inputStream() }))

        val fieldId = UUID.randomUUID()
        val itemFieldData = createItemFieldData(
            id = fieldId,
            kind = SafeItemFieldKind.Photo,
            value = "$filedId|png",
        )
        addFieldUseCase(item.id, itemFieldData)

        val result = migrationFromV9ToV10(masterKey(), firstSafeId)
        assertSuccess(result)

        val encFile = File(context.filesDir, "files/$filedId")
        val plainFileData = decryptUseCase(encFile.readBytes(), item.id, ByteArray::class).data!!

        val afterMimetype = getMimetype(plainFileData)
        assertEquals("image/png", afterMimetype)
        assertContentEquals(expectedPngData, plainFileData)
    }

    @Test
    fun run_migration_v9_v10_crypto_wrong_key_error_test(): TestResult = runTest {
        val item = createItemUseCase.test()
        val image = createRandomImage()
        val filename = "field_file"
        val pngFile = File(context.cacheDir, filename)
        pngFile.outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val beforeMimetype = getMimetype(pngFile)
        assertEquals("image/png", beforeMimetype)

        val filedId = UUID.randomUUID()
        val encFile = File(context.filesDir, "files/$filedId")
        addAndRemoveFileUseCase(item, listOf(FileSavingData.ToSave(filedId) { pngFile.inputStream() }))

        val fieldId = UUID.randomUUID()
        val itemFieldData = createItemFieldData(
            id = fieldId,
            kind = SafeItemFieldKind.Photo,
            value = "$filedId|jpeg",
        )
        addFieldUseCase(item.id, itemFieldData)

        // Makes the crypto fail with wrong key
        val resultWrongKey = migrationFromV9ToV10(OSTestConfig.random.nextBytes(32), firstSafeId)
        assertSuccess(resultWrongKey)

        val plainFileData = decryptUseCase(encFile.readBytes(), item.id, ByteArray::class).data!!

        // no change expected as migration failed
        val afterMimetype = getMimetype(plainFileData)
        assertEquals(beforeMimetype, afterMimetype)
        assertContentEquals(pngFile.readBytes(), plainFileData)
    }

    @Test
    fun run_migration_v9_v10_crypto_corrupted_file_error_test(): TestResult = runTest {
        val item = createItemUseCase.test()
        val image = createRandomImage()
        val filename = "field_file"
        val pngFile = File(context.cacheDir, filename)
        pngFile.outputStream().use {
            image.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val beforeMimetype = getMimetype(pngFile)
        assertEquals("image/png", beforeMimetype)

        val filedId = UUID.randomUUID()
        val encFile = File(context.filesDir, "files/$filedId")
        addAndRemoveFileUseCase(item, listOf(FileSavingData.ToSave(filedId) { pngFile.inputStream() }))

        val fieldId = UUID.randomUUID()
        val itemFieldData = createItemFieldData(
            id = fieldId,
            kind = SafeItemFieldKind.Photo,
            value = "$filedId|jpeg",
        )
        addFieldUseCase(item.id, itemFieldData)

        // Makes the crypto fail with corrupted file (keep crypto nonce and add random bytes)
        val pngData = pngFile.readBytes()
        val corruptedData = pngData.copyOfRange(0, 12) + OSTestConfig.random.nextBytes(50)

        encFile.writeBytes(corruptedData)
        val resultBadFile = migrationFromV9ToV10(masterKey(), firstSafeId)
        assertSuccess(resultBadFile)
        val decryptResult = decryptUseCase(encFile.readBytes(), item.id, ByteArray::class)
        assertFailure(decryptResult)

        assertContentEquals(encFile.readBytes(), corruptedData)
    }

    private fun createRandomImage(): Bitmap {
        val pixels = IntArray(100 * 100) {
            OSTestConfig.random.colorInt()
        }
        return Bitmap.createBitmap(pixels, 100, 100, Bitmap.Config.ARGB_8888)
    }

    private fun getMimetype(file: File): String? {
        val options = BitmapFactory.Options()
        file.inputStream().use {
            BitmapFactory.decodeStream(it, null, options)
        }
        return options.outMimeType
    }

    private fun getMimetype(data: ByteArray): String? {
        val options = BitmapFactory.Options()
        data.inputStream().use {
            BitmapFactory.decodeStream(it, null, options)
        }
        return options.outMimeType
    }
}
