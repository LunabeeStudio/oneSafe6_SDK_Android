package studio.lunabee.onesafe.debug.item

class DebugSafeItemData(
    val createRecursiveItem: () -> Unit,
    val corruptFile: () -> Unit,
)
