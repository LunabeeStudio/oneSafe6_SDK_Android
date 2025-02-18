package studio.lunabee.onesafe.feature.itemform.model

import androidx.compose.ui.graphics.Color

data class ItemInitialData(
    val name: String,
    val color: Color?,
    val icon: ByteArray?,
    val fieldValues: List<InitialUiField>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ItemInitialData

        if (name != other.name) return false
        if (color != other.color) return false
        if (icon != null) {
            if (other.icon == null) return false
            if (!icon.contentEquals(other.icon)) return false
        } else if (other.icon != null) return false
        if (fieldValues != other.fieldValues) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (color?.hashCode() ?: 0)
        result = 31 * result + (icon?.contentHashCode() ?: 0)
        result = 31 * result + fieldValues.hashCode()
        return result
    }
}
