package studio.lunabee.onesafe.feature.settings.bubbles

import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.feature.settings.bubbles.model.BubblesResendMessageDelay

data class BubblesSettingsUiState(
    val hasFinishOneSafeKOnBoarding: Boolean,
    val isBubblesPreviewActivated: Boolean,
    val bubblesResendMessageDelay: BubblesResendMessageDelay,
    val inactivityDelay: AutoLockInactivityDelay,
    val hiddenDelay: AutoLockBackgroundDelay,
) {
    companion object {
        fun default(
            hasFinishOneSafeKOnBoarding: Boolean = false,
            isBubblesPreviewActivated: Boolean = false,
            bubblesResendMessageDelay: BubblesResendMessageDelay = BubblesResendMessageDelay.NEVER,
            inactivityDelay: AutoLockInactivityDelay = AutoLockInactivityDelay.NEVER,
            hiddenDelay: AutoLockBackgroundDelay = AutoLockBackgroundDelay.NEVER,
        ): BubblesSettingsUiState = BubblesSettingsUiState(
            hasFinishOneSafeKOnBoarding = hasFinishOneSafeKOnBoarding,
            isBubblesPreviewActivated = isBubblesPreviewActivated,
            bubblesResendMessageDelay = bubblesResendMessageDelay,
            inactivityDelay = inactivityDelay,
            hiddenDelay = hiddenDelay,
        )
    }
}
