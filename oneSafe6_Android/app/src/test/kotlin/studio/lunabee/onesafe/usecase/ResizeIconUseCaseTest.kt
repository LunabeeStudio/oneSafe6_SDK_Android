package studio.lunabee.onesafe.usecase

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import studio.lunabee.compose.androidtest.helper.LbcResourcesHelper
import studio.lunabee.di.FrameworkTestModule
import studio.lunabee.onesafe.common.extensions.createTempFile
import studio.lunabee.onesafe.commonui.utils.ImageHelper
import studio.lunabee.onesafe.domain.usecase.ResizeIconUseCase
import studio.lunabee.onesafe.extension.iconSample
import studio.lunabee.onesafe.test.InitialTestState
import studio.lunabee.onesafe.test.OSHiltUnitTest
import java.io.File
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class ResizeIconUseCaseTest : OSHiltUnitTest() {

    @get:Rule
    override val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    override val initialTestState: InitialTestState = InitialTestState.Home()

    @Inject
    lateinit var resizeIconUseCase: ResizeIconUseCase

    @Inject
    lateinit var imageHelper: ImageHelper

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun setUp() {
        File(context.cacheDir, FrameworkTestModule.ICON_DIR).deleteRecursively()
    }

    @Test
    fun resize_image_bytes_test(): TestResult = runTest {
        val result = resizeIconUseCase(srcData = iconSample)
        val resultBitmap = imageHelper.byteArrayToBitmap(result)
        assertEquals(FrameworkTestModule.RESIZE_ICON_SIZE, resultBitmap?.width)
        assertEquals(FrameworkTestModule.RESIZE_ICON_SIZE, resultBitmap?.height)
    }

    @Test
    fun resize_image_file_test(): TestResult = runTest {
        val srcFile = context.createTempFile("icon_259_194.jpeg")
        LbcResourcesHelper.copyResourceToDeviceFile("icon_259_194.jpeg", srcFile)
        val result = resizeIconUseCase(srcFile = srcFile)
        val resultBitmap = imageHelper.byteArrayToBitmap(result)
        assertEquals(FrameworkTestModule.RESIZE_ICON_SIZE, resultBitmap?.width)
        assertEquals(FrameworkTestModule.RESIZE_ICON_SIZE, resultBitmap?.height)
        srcFile.delete()
    }
}
