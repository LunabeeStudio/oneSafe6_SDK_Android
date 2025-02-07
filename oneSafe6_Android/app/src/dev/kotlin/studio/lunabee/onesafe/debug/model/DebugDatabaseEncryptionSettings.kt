package studio.lunabee.onesafe.debug.model

enum class DebugDatabaseEncryptionSettings(val enabled: Boolean, val error: Boolean) {
    Enabled(true, false),
    EnabledWithError(true, true),
    Disabled(false, false),
    DisabledWithError(false, true),
    ;

    companion object {
        fun fromBoolean(enabled: Boolean): DebugDatabaseEncryptionSettings = if (enabled) Enabled else Disabled
    }
}
