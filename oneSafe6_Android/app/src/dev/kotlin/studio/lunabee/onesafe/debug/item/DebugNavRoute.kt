package studio.lunabee.onesafe.debug.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.debug.extension.resolveArgsToString

@Composable
fun DbgNavRoute(title: String, entry: NavBackStackEntry, modifier: Modifier = Modifier) {
    OSText(
        text = LbcTextSpec.Raw("$title: ${entry.resolveArgsToString()}"),
        modifier = modifier,
    )
}
