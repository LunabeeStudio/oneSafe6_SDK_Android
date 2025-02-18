package studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner

object NumberValueCleaner : ValueCleaner {
    override fun invoke(value: String): String = value.filter { it.isDigit() }
}
