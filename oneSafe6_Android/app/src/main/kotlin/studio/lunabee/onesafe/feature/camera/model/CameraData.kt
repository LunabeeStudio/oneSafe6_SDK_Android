package studio.lunabee.onesafe.feature.camera.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import studio.lunabee.onesafe.domain.model.camera.CameraSystem
import studio.lunabee.onesafe.feature.itemform.manager.ExternalPhotoCapture
import studio.lunabee.onesafe.feature.itemform.manager.InAppMediaCapture

/**
 * Common holder to store URIs for both internal and external camera uses
 */
@Serializable
sealed interface CameraData {
    val cameraSystem: CameraSystem

    @Serializable
    data class InApp(
        val photoCapture: InAppMediaCapture,
    ) : CameraData {
        override val cameraSystem: CameraSystem = CameraSystem.InApp
    }

    @Serializable
    data class External(
        @Serializable(with = LazyExternalPhotoCaptureSerializer::class)
        val photoCapture: Lazy<ExternalPhotoCapture>,
    ) : CameraData {
        override val cameraSystem: CameraSystem = CameraSystem.External
    }
}

class LazyExternalPhotoCaptureSerializer : KSerializer<Lazy<ExternalPhotoCapture>> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ExternalPhotoCaptureAsString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Lazy<ExternalPhotoCapture> {
        val externalPhotoCapture = decoder.decodeSerializableValue(ExternalPhotoCapture.serializer())
        return lazy { externalPhotoCapture }.also { it.value }
    }

    override fun serialize(encoder: Encoder, value: Lazy<ExternalPhotoCapture>) {
        encoder.encodeSerializableValue(ExternalPhotoCapture.serializer(), value.value)
    }
}
