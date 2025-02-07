package studio.lunabee.onesafe.feature.forceupgrade

sealed interface ForceUpgradeState {
    data class Screen(
        val isForced: Boolean = false,
        val title: String = "",
        val description: String = "",
        val buttonLabel: String = "",
    ) : ForceUpgradeState

    object Exit : ForceUpgradeState
}
