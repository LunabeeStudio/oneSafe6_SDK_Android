package studio.lunabee.onesafe.feature.itemform.screen

import studio.lunabee.onesafe.commonui.extension.formatNumber

class NumberVisualTransformation : GenericSeparatorVisualTransformation() {

    override fun transform(input: CharSequence): CharSequence {
        return input.toString().trim().formatNumber()
    }

    override fun isSeparator(char: Char): Boolean = char == ' '
}
