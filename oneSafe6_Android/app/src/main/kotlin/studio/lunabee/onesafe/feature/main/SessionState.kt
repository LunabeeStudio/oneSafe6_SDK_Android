package studio.lunabee.onesafe.feature.main

sealed class SessionState {
    data object Idle : SessionState()
    data class Broken(
        val reset: () -> Unit,
    ) : SessionState()
}
