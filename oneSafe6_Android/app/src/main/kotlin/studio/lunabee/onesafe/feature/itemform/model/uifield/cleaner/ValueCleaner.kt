package studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner

interface ValueCleaner {
    operator fun invoke(value: String): String
}
