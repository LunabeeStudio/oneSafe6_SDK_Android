package studio.lunabee.onesafe.usecase.importexport

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.di.CryptoConstantsTestModule
import studio.lunabee.onesafe.test.TestConstants
import studio.lunabee.onesafe.cryptography.android.CryptoConstants
import studio.lunabee.onesafe.cryptography.android.PasswordHashEngine
import studio.lunabee.onesafe.cryptography.android.PBKDF2JceHashEngine
import studio.lunabee.onesafe.domain.qualifier.ArchiveCacheDir
import studio.lunabee.onesafe.importexport.usecase.ArchiveUnzipUseCase
import studio.lunabee.onesafe.importexport.usecase.ImportAuthUseCase
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import studio.lunabee.onesafe.test.assertFailure
import studio.lunabee.onesafe.test.assertSuccess
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(CryptoConstantsTestModule::class)
class ImportAuthUseCaseTest : OSHiltUnitTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @BindValue
    val hashEngine: PasswordHashEngine = PBKDF2JceHashEngine(Dispatchers.Default, CryptoConstants.PBKDF2Iterations)

    @Inject
    @ApplicationContext
    lateinit var context: Context

    @Inject lateinit var archiveUnzipUseCase: ArchiveUnzipUseCase

    @Inject lateinit var importAuthUseCase: ImportAuthUseCase

    @ArchiveCacheDir(type = ArchiveCacheDir.Type.Import)
    @Inject
    lateinit var archiveExtractedDirectory: File

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Test
    fun check_valid_password_test() {
        runTest {
            val oneSafeArchive = File(context.cacheDir, TestConstants.ResourcesFile.ArchiveValid)
            LbcResourcesHelper.copyResourceToDeviceFile(TestConstants.ResourcesFile.ArchiveValid, oneSafeArchive)

            oneSafeArchive.inputStream().use { inputStream ->
                archiveUnzipUseCase(inputStream, archiveExtractedDirectory).last()
            }

            val result = importAuthUseCase(
                password = TestConstants.ResourcesFile.PasswordValidArchive,
            ).last()
            assertSuccess(result)
        }
    }

    @Test
    fun check_wrong_password_test() {
        runTest {
            val oneSafeArchive = File(context.cacheDir, TestConstants.ResourcesFile.ArchiveNoMetadata)
            LbcResourcesHelper.copyResourceToDeviceFile(TestConstants.ResourcesFile.ArchiveNoMetadata, oneSafeArchive)

            oneSafeArchive.inputStream().use { inputStream ->
                archiveUnzipUseCase(inputStream, archiveExtractedDirectory).last()
            }

            val result = importAuthUseCase(
                password = "test".toCharArray(),
            ).last()
            assertFailure(result)
        }
    }
}
