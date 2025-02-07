package studio.lunabee.onesafe.model

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import studio.lunabee.compose.foundation.haptic.LbcHapticEffect
import studio.lunabee.compose.foundation.haptic.LbcHapticFeedback
import studio.lunabee.compose.foundation.haptic.rememberLbcHapticFeedback

enum class OSHapticEffect(val hapticEffect: LbcHapticEffect, val fallback: LbcHapticEffect? = LbcHapticEffect.Compose.LongPress) {
    Primary(hapticEffect = LbcHapticEffect.Predefined.Tick),
    LongPressed(hapticEffect = LbcHapticEffect.Compose.LongPress, fallback = null),
    ;

    fun perform(hapticFeedback: LbcHapticFeedback) {
        hapticFeedback.perform(hapticEffect = hapticEffect, fallback = fallback)
    }
}

fun Modifier.clickableWithHaptic(
    onClick: () -> Unit,
    onClickLabel: String? = null,
    osHapticEffect: OSHapticEffect = OSHapticEffect.Primary,
): Modifier {
    return this.composed {
        val hapticFeedback: LbcHapticFeedback = rememberLbcHapticFeedback()
        clickable(
            onClickLabel = onClickLabel,
        ) {
            onClick()
            osHapticEffect.perform(hapticFeedback)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.combinedClickableWithHaptic(
    onLongClick: (() -> Unit)?,
    onClick: (() -> Unit) = { },
    onLongClickLabel: String?,
    onClickLabel: String?,
    osLongClickHapticEffect: OSHapticEffect = OSHapticEffect.LongPressed,
    osClickHapticEffect: OSHapticEffect? = null,
    enabled: Boolean = true,
): Modifier {
    return this.composed {
        val hapticFeedback: LbcHapticFeedback = rememberLbcHapticFeedback()
        combinedClickable(
            enabled = enabled,
            onLongClick = onLongClick?.let {
                {
                    osLongClickHapticEffect.perform(hapticFeedback)
                    onLongClick()
                }
            },
            onClick = {
                osClickHapticEffect?.perform(hapticFeedback)
                onClick()
            },
            onLongClickLabel = onLongClickLabel,
            onClickLabel = onClickLabel,
        )
    }
}
