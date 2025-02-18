package studio.lunabee.onesafe.usecase

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.onesafe.common.model.FileThumbnailData
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltTest
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class AndroidGetThumbnailFromFileUseCaseTest : OSHiltTest() {
    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var useCase: AndroidGetThumbnailFromFileUseCase

    @Test
    fun protected_pdf_test(): TestResult = runTest {
        val pdfFile = File.createTempFile("protected_pdf", ".pdf")
        LbcResourcesHelper.copyResourceToDeviceFile("protected_pdf.pdf", pdfFile)
        val expected = FileThumbnailData.FileThumbnailPlaceholder.File
        val actual = useCase(pdfFile)
        assertEquals(expected, actual)
    }
}
