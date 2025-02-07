package studio.lunabee.onesafe.feature.itemform.model

import studio.lunabee.onesafe.domain.model.safeitem.SafeItemFieldKind
import studio.lunabee.onesafe.feature.itemform.model.uifield.UiField
import studio.lunabee.onesafe.feature.itemform.model.uifield.file.FileUiField

enum class ReorderOption(
    val filterFieldToReorder: (UiField) -> Boolean,
) {
    TextFields(
        filterFieldToReorder = { field -> field !is FileUiField },
    ),
    MediaFields(
        filterFieldToReorder = { field ->
            field.safeItemFieldKind == SafeItemFieldKind.Photo || field.safeItemFieldKind == SafeItemFieldKind.Video
        },
    ),
    FileField(
        filterFieldToReorder = { field -> field.safeItemFieldKind == SafeItemFieldKind.File },
    ),
}
