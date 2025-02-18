package studio.lunabee.onesafe.feature.itemform.model.uifield.cleaner

object IbanValueCleaner : ValueCleaner {
    override fun invoke(value: String): String = value.filter { it != ' ' }.map { it.uppercaseChar() }.joinToString("")
}
