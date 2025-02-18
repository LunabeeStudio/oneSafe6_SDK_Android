package studio.lunabee.onesafe.commonui.animation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry

val slideVerticalEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    slideInVertically(
        initialOffsetY = { fullHeight -> (fullHeight * SlideOffsetRatio).toInt() },
        animationSpec = tween(SlideDurationMs, SlideDelayMs),
    ) + fadeIn(animationSpec = tween(FadeDurationMs))
}

val slideVerticalExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    slideOutVertically(
        targetOffsetY = { fullHeight -> -(fullHeight * SlideOffsetRatio).toInt() },
        animationSpec = tween(SlideDurationMs),
    ) + fadeOut(animationSpec = tween(FadeDurationMs))
}

val slideVerticalPopEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
    slideInVertically(
        initialOffsetY = { fullHeight -> -(fullHeight * SlideOffsetRatio).toInt() },
        animationSpec = tween(SlideDurationMs),
    ) + fadeIn(animationSpec = tween(FadeDurationMs))
}

val slideVerticalPopExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
    slideOutVertically(
        targetOffsetY = { fullHeight -> (fullHeight * SlideOffsetRatio).toInt() },
        animationSpec = tween(SlideDurationMs),
    ) + fadeOut(animationSpec = tween(FadeDurationMs))
}
