package studio.lunabee.onesafe.feature.itemform.viewmodel.delegate

import androidx.compose.ui.graphics.Color
import com.lunabee.lbcore.model.LBResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField

/**
 * @property isFromCamera indicate that the form has been initialized from an image capture
 */
data class ItemFormInitialInfo(
    val name: String,
    val color: Color?,
    val icon: ByteArray?,
    val fields: List<UiField>,
    val isFromCamera: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemFormInitialInfo

        if (name != other.name) return false
        if (color != other.color) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false
        if (fields != other.fields) return false
        if (isFromCamera != other.isFromCamera) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + fields.hashCode()
        result = 31 * result + isFromCamera.hashCode()
        return result
    }
}

interface PopulateScreenDelegate {
    suspend fun getInitialInfo(): LBResult<ItemFormInitialInfo>
    fun CoroutineScope.loadInitialInfo(info: ItemFormInitialInfo): Job
}
