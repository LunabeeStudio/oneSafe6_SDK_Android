package studio.lunabee.onesafe.debug.item

data class DebugOneTimeActionData(
    val closeDrawer: () -> Unit,
    val resetBackupCta: () -> Unit,
    val resetBubblesCta: () -> Unit,
    val resetSafeCta: () -> Unit,
    val forceShowSupportOs: () -> Unit,
    val resetOSKTutorial: () -> Unit,
    val resetOSKOnboarding: () -> Unit,
    val resetCameraTips: () -> Unit,
    val resetTips: () -> Unit,
    val resetLastExportDate: () -> Unit,
)
