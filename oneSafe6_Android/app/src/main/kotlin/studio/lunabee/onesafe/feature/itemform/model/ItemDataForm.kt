package studio.lunabee.onesafe.feature.itemform.model

/**
 * @property data The actual item data
 * @property isUserPicked True if the user explicitly picked the image, so it should not be replaced automatically
 */
data class ItemDataForm<T>(
    val data: T,
    val isUserPicked: Boolean,
)

val <T> ItemDataForm<T>?.canAutoOverride: Boolean
    get() = this?.isUserPicked != true
