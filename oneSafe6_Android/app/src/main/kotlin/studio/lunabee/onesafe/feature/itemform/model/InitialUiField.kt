package studio.lunabee.onesafe.feature.itemform.model

import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.domain.model.safeitem.FileSavingData
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.ExistingFileUiField
import java.util.UUID

/**
 * Store initial field data to compare with edited value during edition
 */
sealed class InitialUiField(field: UiField) {
    val id: UUID = field.id
    val isIdentifier: Boolean = field.isIdentifier
    val value: String = field.getDisplayedValue()
    val description: LbcTextSpec = field.fieldDescription.value

    companion object {
        fun fromUiField(field: UiField): InitialUiField {
            return if (field is ExistingFileUiField) {
                ExistingFileInitialUiField(field)
            } else {
                StandardInitialUiField(field)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InitialUiField

        if (id != other.id) return false
        if (isIdentifier != other.isIdentifier) return false
        if (value != other.value) return false
        if (description != other.description) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isIdentifier.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + description.hashCode()
        return result
    }
}

class StandardInitialUiField(field: UiField) : InitialUiField(field)

class ExistingFileInitialUiField(field: ExistingFileUiField) : InitialUiField(field) {
    val savingDataRemove: FileSavingData.ToRemove = FileSavingData.ToRemove(field.fileId, field.safeItemField.encThumbnailFileName)
}
