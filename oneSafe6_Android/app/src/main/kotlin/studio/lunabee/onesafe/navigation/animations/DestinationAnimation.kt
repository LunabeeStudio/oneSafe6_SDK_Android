package studio.lunabee.onesafe.navigation.animations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import studio.lunabee.onesafe.commonui.animation.slideHorizontalEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalExitTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideHorizontalPopExitTransition
import studio.lunabee.onesafe.commonui.animation.slideVerticalEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideVerticalExitTransition
import studio.lunabee.onesafe.commonui.animation.slideVerticalPopEnterTransition
import studio.lunabee.onesafe.commonui.animation.slideVerticalPopExitTransition
import studio.lunabee.onesafe.feature.move.movehost.MoveHostDestination

enum class DestinationAnimation(
    val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition),
    val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition),
    val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition),
    val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition),
) {

    VERTICAL(
        slideVerticalEnterTransition,
        slideVerticalExitTransition,
        slideVerticalPopEnterTransition,
        slideVerticalPopExitTransition,
    ),

    HORIZONTAL(
        slideHorizontalEnterTransition,
        slideHorizontalExitTransition,
        slideHorizontalPopEnterTransition,
        slideHorizontalPopExitTransition,
    ),
    ;

    companion object {
        private val destinationVerticalAnimation: List<String> = listOf(MoveHostDestination.route)

        fun getEnterTransitionFromRoute(
            route: String?,
            isInSearchMode: Boolean,
        ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) {
            return if (isInSearchMode) {
                { fadeIn(animationSpec = tween(700)) }
            } else {
                when {
                    destinationVerticalAnimation.contains(route) -> VERTICAL.enterTransition
                    // Add other animation list
                    else -> HORIZONTAL.enterTransition
                }
            }
        }

        fun getExitTransitionFromRoute(
            route: String?,
            isInSearchMode: Boolean,
        ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) {
            return if (isInSearchMode) {
                { fadeOut(animationSpec = tween(700)) }
            } else {
                when {
                    destinationVerticalAnimation.contains(route) -> VERTICAL.exitTransition
                    // Add other animation list
                    else -> HORIZONTAL.exitTransition
                }
            }
        }

        fun getPopEnterTransitionFromRoute(
            route: String?,
            isInSearchMode: Boolean,
        ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) {
            return if (isInSearchMode) {
                { fadeIn(animationSpec = tween(700)) }
            } else {
                when {
                    destinationVerticalAnimation.contains(route) -> VERTICAL.popEnterTransition
                    // Add other animation list
                    else -> HORIZONTAL.popEnterTransition
                }
            }
        }

        fun getPopExitTransitionFromRoute(
            route: String?,
            isInSearchMode: Boolean,
        ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) {
            return if (isInSearchMode) {
                { fadeOut(animationSpec = tween(700)) }
            } else {
                when {
                    destinationVerticalAnimation.contains(route) -> VERTICAL.popExitTransition
                    // Add other animation list
                    else -> HORIZONTAL.popExitTransition
                }
            }
        }
    }
}
