package studio.lunabee.onesafe.model

enum class OSActionState(val enabled: Boolean) {
    Enabled(true), Disabled(false), DisabledWithAction(true)
}
