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
 * Created by Lunabee Studio / Date - 7/30/2024 - for the oneSafe6 SDK.
 * Last modified 7/30/24, 11:08 AM
 */

package studio.lunabee.onesafe.usecase.authentication

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.domain.model.safe.SafeId
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.domain.usecase.authentication.DeleteSafeUseCase
import studio.lunabee.onesafe.domain.usecase.authentication.IsSafeReadyUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddAndRemoveFileUseCase
import studio.lunabee.onesafe.domain.usecase.item.AddFieldUseCase
import studio.lunabee.onesafe.domain.usecase.item.CreateItemUseCase
import studio.lunabee.onesafe.domain.utils.CrossSafeData
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.importexport.repository.LocalBackupRepository
import studio.lunabee.onesafe.importexport.usecase.LocalAutoBackupUseCase
import studio.lunabee.onesafe.test.CommonTestUtils.createItemFieldData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.OSTestConfig
import studio.lunabee.onesafe.test.StaticIdProvider
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.firstSafeId
import studio.lunabee.onesafe.test.test
import studio.lunabee.onesafe.test.testUUIDs
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@HiltAndroidTest
class DeleteSafeUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject lateinit var createItemUseCase: CreateItemUseCase

    @Inject lateinit var addFieldUseCase: AddFieldUseCase

    @Inject lateinit var addAndRemoveFileUseCase: AddAndRemoveFileUseCase

    @Inject lateinit var deleteSafeUseCase: DeleteSafeUseCase

    @Inject lateinit var isSafeReadyUseCase: IsSafeReadyUseCase

    @Inject lateinit var localAutoBackupUseCase: LocalAutoBackupUseCase

    @Inject lateinit var localBackupRepository: LocalBackupRepository

    private val passwordB = charArrayOf('b')
        get() = field.copyOf()

    @Test
    fun `Simple delete safe test`(): TestResult = runTest {
        assertSuccess(deleteSafeUseCase())
        assertFalse(isSafeReadyUseCase())
        assertEquals(OSTestConfig.extraSafeIds.size, safeRepository.getAllSafeOrderByLastOpenAsc().count())
    }

    @OptIn(CrossSafeData::class)
    @Test
    fun `Delete safe check files, icons and backups deleted test`(): TestResult = runTest {
        val icon = LbcResourcesHelper.readResourceAsBytes("icon_259_194.jpeg")
        val itemA = createItemUseCase.test(name = "a", icon = icon)

        val newFileId = testUUIDs[1]
        val newField = createItemFieldData(
            kind = SafeItemFieldKind.Photo,
            name = "newFile.jpeg",
            value = "$newFileId|jpeg",
        )
        addFieldUseCase(itemId = itemA.id, itemFieldData = newField)

        val savingData = FileSavingData.ToSave(
            fileId = newFileId,
            getStream = { iconSample.inputStream() },
        )
        addAndRemoveFileUseCase(
            item = itemA,
            fileSavingData = listOf(savingData),
        )
        localAutoBackupUseCase(firstSafeId).collect()

        // Create second safe
        lockAppUseCase(false)
        val secondSafeId = SafeId(testUUIDs[2])
        StaticIdProvider.id = secondSafeId.id
        generateCryptoForNewSafeUseCase(passwordB)
        finishSafeCreationUseCase()
        loginUseCase(passwordB)

        val itemB = createItemUseCase.test(name = "b", icon = icon)
        val newFileIdB = testUUIDs[3]
        val newFieldB = createItemFieldData(
            kind = SafeItemFieldKind.Photo,
            name = "newFile.jpeg",
            value = "$newFileIdB|jpeg",
        )
        addFieldUseCase(itemId = itemB.id, itemFieldData = newFieldB)

        val savingDataB = FileSavingData.ToSave(
            fileId = newFileIdB,
            getStream = { iconSample.inputStream() },
        )
        addAndRemoveFileUseCase(
            item = itemB,
            fileSavingData = listOf(savingDataB),
        )
        testClock.add(1, ChronoUnit.DAYS) // avoid backup filename collision
        localAutoBackupUseCase(secondSafeId).collect()

        assertEquals(1, fileRepository.getFiles(firstSafeId).size)
        assertEquals(1, fileRepository.getFiles(secondSafeId).size)

        assertEquals(1, iconRepository.getIcons(firstSafeId).size)
        assertEquals(1, iconRepository.getIcons(secondSafeId).size)
        assertEquals(2, iconRepository.getAllIcons().size)

        assertEquals(1, localBackupRepository.getBackups(firstSafeId).size)
        assertEquals(1, localBackupRepository.getBackups(secondSafeId).size)

        deleteSafeUseCase()

        assertEquals(1, fileRepository.getFiles(firstSafeId).size)
        assertEquals(0, fileRepository.getFiles(secondSafeId).size)

        assertEquals(1, iconRepository.getIcons(firstSafeId).size)
        assertEquals(0, iconRepository.getIcons(secondSafeId).size)
        assertEquals(1, iconRepository.getAllIcons().size)

        assertEquals(1, localBackupRepository.getBackups(firstSafeId).size)
        assertEquals(0, localBackupRepository.getBackups(secondSafeId).size)
    }
}
