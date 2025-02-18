package studio.lunabee.onesafe.feature.itemform.screen

import studio.lunabee.onesafe.domain.model.safeitem.FieldMask

class MaskVisualTransformation(private val mask: String) : GenericSeparatorVisualTransformation() {

    private val spacerSymbolsIndices = mask.indices
        .filter { maskIndex -> !FieldMask.SpecialSymbols.map { symbols -> symbols.second }.contains(mask[maskIndex]) }
    private val spacerSymbols: List<Char> = mask.toList()
        .filter { maskChar -> !FieldMask.SpecialSymbols.map { it.second }.contains(maskChar) }

    override fun transform(input: CharSequence): CharSequence {
        var out = ""
        var maskIndex = 0
        input.forEach { char ->
            while (spacerSymbolsIndices.contains(maskIndex)) {
                out += mask[maskIndex]
                maskIndex++
            }
            if (char != mask.getOrNull(maskIndex - 1)) {
                out += char
                maskIndex++
            }
        }
        return out
    }

    override fun isSeparator(char: Char): Boolean {
        return spacerSymbols.contains(char)
    }
}
