package studio.lunabee.onesafe.model

enum class OSSwitchState(val checked: Boolean) {
    True(true),
    False(false),
    Loading(false),
    ;

    companion object {
        fun fromChecked(checked: Boolean): OSSwitchState = if (checked) True else False
    }
}
