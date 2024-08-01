package studio.lunabee.onesafe.help.debug.item

internal class DebugSafeItemData(
    val removeAllItems: () -> Unit,
    val corruptFile: () -> Unit,
)
