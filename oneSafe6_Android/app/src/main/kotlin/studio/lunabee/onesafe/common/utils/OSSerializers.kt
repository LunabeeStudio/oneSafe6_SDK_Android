package studio.lunabee.onesafe.common.utils

import android.net.Uri
import androidx.core.net.toFile
import androidx.core.net.toUri
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.io.File

class NullableFileSerializer : KSerializer<File?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("NullFileAsUri", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): File? {
        return decoder.decodeString().takeIf { it.isNotEmpty() }?.toUri()?.toFile()
    }

    override fun serialize(encoder: Encoder, value: File?) {
        encoder.encodeString(value?.toUri()?.toString().orEmpty())
    }
}

class FileSerializer : KSerializer<File> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FileAsUri", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): File {
        return decoder.decodeSerializableValue(UriSerializer()).toFile()
    }

    override fun serialize(encoder: Encoder, value: File) {
        encoder.encodeSerializableValue(UriSerializer(), value.toUri())
    }
}

class UriSerializer : KSerializer<Uri> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UriAsString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Uri {
        return decoder.decodeString().toUri()
    }

    override fun serialize(encoder: Encoder, value: Uri) {
        encoder.encodeString(value.toString())
    }
}
