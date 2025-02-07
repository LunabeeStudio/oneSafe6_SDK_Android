package studio.lunabee.onesafe.usecase.importexport

import android.content.Context
import com.lunabee.lbcore.model.LBFlowResult
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.test.TestConstants
import studio.lunabee.onesafe.domain.model.importexport.OSArchiveKind
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.error.OSDomainError
import studio.lunabee.onesafe.error.OSImportExportError
import studio.lunabee.onesafe.importexport.usecase.GetMetadataFromArchiveUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@HiltAndroidTest
class GetMetadataFromArchiveUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject
    lateinit var getMetadataFromArchiveUseCase: GetMetadataFromArchiveUseCase

    @Inject
    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import)
    lateinit var unzipFolderDestination: File

    @Test
    fun extract_metadata_from_valid_archive_test() {
        runTest {
            val oneSafeArchive = File(context.cacheDir, TestConstants.ResourcesFile.ArchiveValid)
            try {
                LbcResourcesHelper.copyResourceToDeviceFile(TestConstants.ResourcesFile.ArchiveValid, oneSafeArchive)
                oneSafeArchive.inputStream().use {
                    val result = getMetadataFromArchiveUseCase(
                        inputStream = it,
                        archiveExtractedDirectory = unzipFolderDestination,
                    ).last()
                    assertSuccess(result)
                    assertEquals(expected = false, actual = result.successData.isFromOldOneSafe)
                    assertEquals(expected = OSArchiveKind.Backup, actual = result.successData.archiveKind)
                    assertEquals(expected = 3, actual = result.successData.archiveVersion)
                    assertTrue { result.successData.fromPlatform.isNotBlank() }
                }
            } finally {
                oneSafeArchive.delete()
            }
        }
    }

    @Test
    fun extract_metadata_archive_no_metadata_test() {
        runTest {
            val oneSafeArchive = File(context.cacheDir, TestConstants.ResourcesFile.ArchiveNoMetadata)
            try {
                LbcResourcesHelper.copyResourceToDeviceFile(TestConstants.ResourcesFile.ArchiveNoMetadata, oneSafeArchive)
                oneSafeArchive.inputStream().use {
                    getMetadataFromArchiveUseCase(
                        inputStream = it,
                        archiveExtractedDirectory = unzipFolderDestination,
                    ).collect { result ->
                        when (result) {
                            is LBFlowResult.Failure -> assertEquals(
                                expected = OSImportExportError.Code.METADATA_FILE_NOT_FOUND,
                                actual = (result.throwable as OSImportExportError).code,
                            )
                            is LBFlowResult.Loading -> Unit
                            is LBFlowResult.Success -> {
                                assert(false)
                            }
                        }
                    }
                }
            } finally {
                oneSafeArchive.delete()
            }
        }
    }

    @Test
    fun extract_metadata_archive_not_zip_test() {
        runTest {
            val oneSafeArchive = File(context.cacheDir, TestConstants.ResourcesFile.ArchiveNoZip)
            try {
                LbcResourcesHelper.copyResourceToDeviceFile(TestConstants.ResourcesFile.ArchiveNoZip, oneSafeArchive)
                oneSafeArchive.inputStream().use {
                    getMetadataFromArchiveUseCase(
                        inputStream = it,
                        archiveExtractedDirectory = unzipFolderDestination,
                    ).collect { result ->
                        when (result) {
                            is LBFlowResult.Failure -> assertEquals(
                                expected = OSDomainError.Code.UNZIP_FAILURE,
                                actual = (result.throwable as OSDomainError).code,
                            )
                            is LBFlowResult.Loading -> Unit
                            is LBFlowResult.Success -> {
                                assert(false)
                            }
                        }
                    }
                }
            } finally {
                oneSafeArchive.delete()
            }
        }
    }
}
