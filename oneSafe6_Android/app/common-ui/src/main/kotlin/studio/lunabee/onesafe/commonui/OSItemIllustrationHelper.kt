package studio.lunabee.onesafe.commonui

import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.atom.OSImageSpec
import studio.lunabee.onesafe.model.OSItemIllustration

object OSItemIllustrationHelper {
    fun get(name: OSNameProvider, icon: ByteArray? = null, color: Color? = null): OSItemIllustration = when {
        icon != null -> OSItemIllustration.Image(OSImageSpec.Data(icon))
        name is EmojiNameProvider -> OSItemIllustration.Emoji(name.placeholderName, color)
        else -> OSItemIllustration.Text(name.placeholderName, color)
    }
}
