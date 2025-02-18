package studio.lunabee.onesafe.atom

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.core.net.toUri
import kotlin.test.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import kotlin.test.assertNotNull

@OptIn(ExperimentalTestApi::class)
class OSImageTest : LbcComposeTest() {
    @Test
    fun osimage_uri_error_test(): Unit = invoke {
        val osImageState = OSImageState()
        val uri = "file://no_image.png".toUri()

        setContent {
            OSImage(image = OSImageSpec.Uri(uri), imageState = osImageState)
        }

        waitUntil { osImageState.error != null }
        assertNotNull(osImageState.error)
    }

    @Test
    fun osimage_data_error_test(): Unit = invoke {
        val osImageState = OSImageState()
        val data = byteArrayOf(1, 2, 3)

        setContent {
            OSImage(image = OSImageSpec.Data(data), imageState = osImageState)
        }

        waitUntil { osImageState.error != null }
        assertNotNull(osImageState.error)
    }
}
