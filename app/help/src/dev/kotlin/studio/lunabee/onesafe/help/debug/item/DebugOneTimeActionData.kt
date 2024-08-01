package studio.lunabee.onesafe.help.debug.item

internal class DebugOneTimeActionData(
    val closeDrawer: () -> Unit,
    val forceShowSupportOs: () -> Unit,
    val resetOSKTutorial: () -> Unit,
)
