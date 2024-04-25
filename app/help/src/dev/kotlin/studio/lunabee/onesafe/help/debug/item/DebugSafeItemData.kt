package studio.lunabee.onesafe.help.debug.item

internal class DebugSafeItemData(
    val createRecursiveItem: () -> Unit,
    val removeAllItems: () -> Unit,
    val corruptFile: () -> Unit,
)
