package studio.lunabee.onesafe.help.debug.model

internal enum class DebugDatabaseEncryptionSettings {
    Enabled,
    Disabled,
    ;

    companion object {
        fun fromBoolean(enabled: Boolean): DebugDatabaseEncryptionSettings = if (enabled) Enabled else Disabled
    }
}
