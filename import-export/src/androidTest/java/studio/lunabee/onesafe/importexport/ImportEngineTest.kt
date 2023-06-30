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

package studio.lunabee.onesafe.importexport

import android.content.Context
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import studio.lunabee.compose.androidtest.helper.LbcFolderResource
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.onesafe.cryptography.CryptoConstants
import studio.lunabee.onesafe.cryptography.qualifier.PBKDF2Iterations
import studio.lunabee.onesafe.domain.engine.ImportEngine
import studio.lunabee.onesafe.domain.model.importexport.ImportMetadata
import studio.lunabee.onesafe.domain.model.importexport.ImportMode
import studio.lunabee.onesafe.domain.model.search.IndexWordEntry
import studio.lunabee.onesafe.domain.model.search.ItemFieldDataToIndex
import studio.lunabee.onesafe.domain.repository.IconRepository
import studio.lunabee.onesafe.domain.repository.IndexWordEntryRepository
import studio.lunabee.onesafe.domain.repository.SafeItemFieldRepository
import studio.lunabee.onesafe.domain.repository.SafeItemRepository
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemFieldUseCase
import studio.lunabee.onesafe.domain.usecase.search.CreateIndexWordEntriesFromItemUseCase
import studio.lunabee.onesafe.domain.usecase.item.ItemDecryptUseCase
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.test.ConsoleTree
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import studio.lunabee.onesafe.test.testUUIDs
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class)
class ImportEngineTest : OSHiltTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    @PBKDF2Iterations
    val iterationNumber: Int = CryptoConstants.PBKDF2Iterations

    @Inject @ApplicationContext
    lateinit var context: Context

    @Inject lateinit var importEngine: ImportEngine

    @Inject lateinit var safeItemRepository: SafeItemRepository

    @Inject lateinit var iconRepository: IconRepository

    @Inject lateinit var decryptUseCase: ItemDecryptUseCase

    @Inject lateinit var safeItemFieldRepository: SafeItemFieldRepository

    override val initialTestState: InitialTestState = InitialTestState.LoggedIn

    @Inject lateinit var createIndexWordEntriesFromItemUseCase: CreateIndexWordEntriesFromItemUseCase

    @Inject lateinit var createIndexWordEntriesFromItemFieldUseCase: CreateIndexWordEntriesFromItemFieldUseCase

    @Inject lateinit var indexWordEntryRepository: IndexWordEntryRepository

    init {
        Timber.plant(ConsoleTree())
    }

    @Test
    fun import_data_test() {
        runTest {
            val testFolder: File
            val copyResourceExecTime = measureTimeMillis {
                testFolder = File(context.cacheDir, "testArchiveExtracted")
                LbcResourcesHelper.copyFolderResourceToDeviceFile(
                    folderResources = ResourcesToCopy,
                    context = context,
                    deviceDestinationFile = testFolder,
                )
            }
            // TODO use benchmark for additional performance test
            Timber.tag(javaClass.simpleName).d("[copyLbcFolderResourceToDeviceFile] $copyResourceExecTime ms")

            val metadata: ImportMetadata
            val metadataExecTime = measureTimeMillis {
                metadata = importEngine.getMetadata(testFolder)
            }
            Timber.tag(javaClass.simpleName).d("[getMetadata] $metadataExecTime ms")

            val authResult: LBFlowResult<Unit>
            val authExecTime = measureTimeMillis {
                authResult = importEngine.authenticateAndExtractData(
                    archiveExtractedDirectory = testFolder,
                    password = "a".toCharArray(),
                ).last()
            }
            Timber.tag(javaClass.simpleName).d("[authenticateAndExtractData] $authExecTime ms")
            assertSuccess(authResult)

            val prepareDataImportResult: LBFlowResult<Unit>
            val prepareDataExecTime = measureTimeMillis {
                prepareDataImportResult = importEngine.prepareDataForImport(archiveExtractedDirectory = testFolder).last()
            }
            Timber.tag(javaClass.simpleName).d("[prepareDataForImport] $prepareDataExecTime ms")
            assertSuccess(prepareDataImportResult)

            val saveImportDataResult: LBFlowResult<Unit>
            val saveExecTime = measureTimeMillis {
                saveImportDataResult = importEngine.saveImportData(mode = ImportMode.Replace).last()
            }
            Timber.tag(javaClass.simpleName).d("[saveImportData] $saveExecTime ms")
            assertSuccess(saveImportDataResult)

            val totalImportTime = metadataExecTime + authExecTime + prepareDataExecTime + saveExecTime
            Timber.tag(javaClass.simpleName).d("[Total import time for ${metadata.itemCount}] $totalImportTime ms")

            // Check that all elements are saved.
            assertEquals(expected = metadata.itemCount, actual = safeItemRepository.getAllSafeItemIds().size)

            val expectedSearchIndex = mutableListOf<IndexWordEntry>()
            // Check that all elements are readable.
            safeItemRepository.getAllSafeItems().forEach { safeItem ->
                // Check that name is readable.
                safeItem.encName?.let {
                    val nameResult = decryptUseCase(it, safeItem.id, String::class)
                    assertSuccess(nameResult)
                    nameResult.data?.let { name ->
                        expectedSearchIndex.addAll(createIndexWordEntriesFromItemUseCase(name, safeItem.id))
                    }
                }

                // Check that icon file exists.
                safeItem.iconId?.toString()?.let { iconId ->
                    val file = iconRepository.getIcon(iconId = iconId)
                    file.inputStream().use { inputStream ->
                        assertSuccess(decryptUseCase(inputStream.readBytes(), safeItem.id, ByteArray::class))
                    }
                    assertTrue(file.exists())
                }

                val itemFieldDataToIndex = mutableListOf<ItemFieldDataToIndex>()
                // Check that items fields are readable.
                safeItemFieldRepository.getSafeItemFields(itemId = safeItem.id).forEach { safeItemField ->
                    safeItemField.encName?.let {
                        val nameResult = decryptUseCase(it, safeItem.id, String::class)
                        assertSuccess(nameResult)
                    }

                    safeItemField.encValue?.let {
                        val valueResult = decryptUseCase(it, safeItem.id, String::class)
                        assertSuccess(valueResult)
                        valueResult.data?.let { value ->
                            itemFieldDataToIndex += ItemFieldDataToIndex(value, safeItemField.isSecured, safeItem.id, safeItemField.id)
                        }
                    }
                }
                expectedSearchIndex.addAll(createIndexWordEntriesFromItemFieldUseCase(itemFieldDataToIndex))
            }

            // Check index in database vs what we expect to get
            val expected = cryptoRepository.decryptIndexWord(expectedSearchIndex).toHashSet()
            val actual = cryptoRepository.decryptIndexWord(indexWordEntryRepository.getAll().first()).toHashSet()
            assertEquals(expected, actual)
        }
    }

    @Test
    fun authenticate_test() {
        runTest {
            val testFolder = File(context.cacheDir, "testArchiveExtracted")
            LbcResourcesHelper.copyFolderResourceToDeviceFile(
                folderResources = ResourcesToCopy,
                context = context,
                deviceDestinationFile = testFolder,
            )
            importEngine.getMetadata(testFolder)

            val authResult: LBFlowResult<Unit> = importEngine.authenticateAndExtractData(
                archiveExtractedDirectory = testFolder,
                password = "a".toCharArray(),
            ).last()

            assertSuccess(authResult)
        }
    }

    @Test
    fun authenticate_wrong_credentials_test() {
        runTest {
            val testFolder = File(context.cacheDir, "testArchiveExtracted")
            LbcResourcesHelper.copyFolderResourceToDeviceFile(
                folderResources = ResourcesToCopy,
                context = context,
                deviceDestinationFile = testFolder,
            )
            importEngine.getMetadata(testFolder)

            val authResultPassword: LBFlowResult<Unit> = importEngine.authenticateAndExtractData(
                archiveExtractedDirectory = testFolder,
                password = "b".toCharArray(),
            ).last()

            assertFailure(authResultPassword)
        }
    }

    @Test
    fun authenticate_no_data_test() {
        runTest {
            val testFolder = File(context.cacheDir, "testArchiveExtracted")
            LbcResourcesHelper.copyFolderResourceToDeviceFile(
                folderResources = ResourcesToCopy,
                context = context,
                deviceDestinationFile = testFolder,
            )
            File(testFolder, ArchiveConstants.DataFile).delete()
            importEngine.getMetadata(testFolder)

            val authResult = importEngine.authenticateAndExtractData(
                archiveExtractedDirectory = testFolder,
                password = "b".toCharArray(),
            ).last()

            val error = assertFailure(authResult).throwable as OSImportExportError
            assertEquals(OSImportExportError.Code.DATA_FILE_NOT_FOUND, error.code)
        }
    }

    @Test
    fun getMetadata_no_metadata_test() {
        runTest {
            val testFolder = File(context.cacheDir, "testArchiveExtracted")
            LbcResourcesHelper.copyFolderResourceToDeviceFile(
                folderResources = ResourcesToCopy,
                context = context,
                deviceDestinationFile = testFolder,
            )
            File(testFolder, ArchiveConstants.MetadataFile).delete()
            val error = assertThrows<OSImportExportError> {
                importEngine.getMetadata(testFolder)
            }
            assertEquals(OSImportExportError.Code.METADATA_FILE_NOT_FOUND, error.code)
        }
    }

    companion object {
        private const val ArchiveFolder: String = "testArchiveExtracted"
        private const val IconFolder: String = "icons"
        private const val DataFile: String = "data"
        private const val MetadataFile: String = "metadata"
        private val Icons: List<String> = testUUIDs.take(3).map { it.toString() }

        private val ArchiveResource = LbcFolderResource(
            resourceName = ArchiveFolder,
            isDirectory = true,
            parentResource = null,
        )
        private val IconResource = LbcFolderResource(
            resourceName = IconFolder,
            isDirectory = true,
            parentResource = ArchiveResource,
        )
        private val ResourcesToCopy: List<LbcFolderResource> = listOf(
            LbcFolderResource(resourceName = IconFolder, isDirectory = true, parentResource = ArchiveResource),
            LbcFolderResource(resourceName = DataFile, isDirectory = false, parentResource = ArchiveResource),
            LbcFolderResource(resourceName = MetadataFile, isDirectory = false, parentResource = ArchiveResource),
        ) + Icons.map { LbcFolderResource(resourceName = it, isDirectory = false, parentResource = IconResource) }
    }
}
