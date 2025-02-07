package studio.lunabee.onesafe.feature.itemform.model.uifield.file

import studio.lunabee.onesafe.atom.OSImageSpec

sealed interface ThumbnailState {
    data object Loading : ThumbnailState
    data class Finished(
        val thumbnail: OSImageSpec?,
    ) : ThumbnailState
}
