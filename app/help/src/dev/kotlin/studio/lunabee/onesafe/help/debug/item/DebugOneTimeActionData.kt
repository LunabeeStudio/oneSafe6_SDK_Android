package studio.lunabee.onesafe.help.debug.item

internal class DebugOneTimeActionData(
    val closeDrawer: () -> Unit,
    val resetBackupCta: () -> Unit,
    val forceShowSupportOs: () -> Unit,
    val resetOSKTutorial: () -> Unit,
    val resetOSKOnboarding: () -> Unit,
    val resetCameraTips: () -> Unit,
    val resetTips: () -> Unit,
)
