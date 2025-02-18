package studio.lunabee.onesafe.feature.camera.model

import android.net.Uri
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test
import studio.lunabee.onesafe.feature.itemform.manager.ExternalPhotoCapture
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs

class CameraDataTest {
    @Test
    fun serialize_cameraData_inApp_test() {
        val expected = CameraData.InApp(InAppMediaCapture(File("/a"), File("/b"), OSMediaType.PHOTO))
        val serialized = Json.encodeToString<CameraData>(expected)
        val actual = Json.decodeFromString<CameraData>(serialized)
        assertIs<CameraData.InApp>(actual)
        assertEquals(expected.photoCapture, actual.photoCapture)
    }

    @Test
    fun serialize_cameraData_external_test() {
        val expected = CameraData.External(lazy { ExternalPhotoCapture(File("/a"), Uri.fromParts("scheme", "ssp", "frag")) })
        val serialized = Json.encodeToString<CameraData>(expected)
        val actual = Json.decodeFromString<CameraData>(serialized)
        assertIs<CameraData.External>(actual)
        assertEquals(expected.photoCapture.value, actual.photoCapture.value)
    }
}
