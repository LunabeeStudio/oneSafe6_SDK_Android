package studio.lunabee.onesafe.atom

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

sealed class OSImageSpec private constructor(val data: Any) {
    class Data(
        data: ByteArray,
    ) : OSImageSpec(data = data)

    /**
     * @property key Use to inform that the image data have changed (even if the Uri does not)
     */
    class Uri(
        uri: android.net.Uri,
        val key: String? = null,
    ) : OSImageSpec(data = uri) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            if (!super.equals(other)) return false

            other as Uri

            return key == other.key
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + (key?.hashCode() ?: 0)
            return result
        }

        override fun toString(): String {
            return (data as android.net.Uri).toString()
        }
    }

    class Bitmap(
        bitmap: android.graphics.Bitmap,
    ) : OSImageSpec(data = bitmap)

    class Drawable(
        @DrawableRes drawable: Int,
        val tintColor: Color? = null,
        val isIcon: Boolean = true,
    ) : OSImageSpec(data = drawable)

    @Suppress("UNCHECKED_CAST")
    fun <T> getAs(): T {
        return data as T
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OSImageSpec) return false

        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        return data.hashCode()
    }

    companion object {
        val Saver: Saver<OSImageSpec, Any> = mapSaver(
            save = { state ->
                val mapToSave: MutableMap<String, Any?> = mutableMapOf()
                mapToSave[OSImageDataKey] = state.data
                when (state) {
                    is Data,
                    is Uri,
                    is Bitmap,
                    -> {
                        // No additional content to save.
                    }
                    is Drawable -> {
                        mapToSave[OSTintColorKey] = state.tintColor?.toArgb()
                        mapToSave[OSIsIcon] = state.isIcon
                    }
                }
                mapToSave.toMap()
            },
            restore = { restoredMap ->
                when (val data = restoredMap[OSImageDataKey]!!) {
                    is ByteArray -> Data(data = data)
                    is android.net.Uri -> Uri(uri = data)
                    is Int -> Drawable(
                        drawable = data,
                        tintColor = (restoredMap[OSTintColorKey] as Int?)?.let { Color(it) },
                        isIcon = restoredMap[OSIsIcon] as Boolean,
                    )
                    else -> error("Data of type ${data::class.simpleName} is not handled")
                }
            },
        )

        private const val OSImageDataKey: String = "OSImageDataKey"
        private const val OSTintColorKey: String = "OSTintColorKey"
        private const val OSIsIcon: String = "OSIsIcon"
    }
}
