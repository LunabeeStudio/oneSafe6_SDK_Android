package studio.lunabee.onesafe.commonui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry

val slideHorizontalEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(animationSpec = tween(FadeDurationMs)) +
        slideInHorizontally(
            initialOffsetX = { fullWidth -> (fullWidth * SlideOffsetRatio).toInt() },
            animationSpec = tween(
                durationMillis = SlideDurationMs,
                delayMillis = SlideDelayMs,
            ),
        )
}

val slideHorizontalExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -(fullWidth * SlideOffsetRatio).toInt() },
        animationSpec = tween(
            durationMillis = SlideDurationMs,
            delayMillis = SlideDelayMs,
        ),
    )
}

val slideHorizontalPopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    fadeIn(animationSpec = tween(FadeDurationMs)) +
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -(fullWidth * SlideOffsetRatio).toInt() },
            animationSpec = tween(
                durationMillis = SlideDurationMs,
                delayMillis = SlideDelayMs,
            ),
        )
}

val slideHorizontalPopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> (fullWidth * SlideOffsetRatio).toInt() },
        animationSpec = tween(
            durationMillis = SlideDurationMs,
            delayMillis = SlideDelayMs,
        ),
    )
}

const val SlideDurationMs: Int = 200
const val SlideDelayMs: Int = 100
const val SlideOffsetRatio: Float = 1f
const val FadeDurationMs: Int = 100
