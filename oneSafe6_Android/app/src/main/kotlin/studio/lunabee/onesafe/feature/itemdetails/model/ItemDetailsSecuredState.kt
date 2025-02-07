package studio.lunabee.onesafe.feature.itemdetails.model

enum class ItemDetailsSecuredState(val showValue: Boolean, val showToggle: Boolean) {
    NONE(true, false), SHOW(true, true), HIDE(false, true)
}
