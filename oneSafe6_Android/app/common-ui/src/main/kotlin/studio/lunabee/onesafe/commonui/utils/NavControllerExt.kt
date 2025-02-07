package studio.lunabee.onesafe.commonui.utils

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.navOptions
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private val logger = LBLogger.get("NavControllerExt")

/**
 * Wait the current backstack entry lifecycle to be in [Lifecycle.State.RESUMED] state before navigating. This allow to skip multi navigate
 * call in case of user flooding buttons. No need to call [safeNavigate] if the navigation is programmatically triggered and/or protected by
 * a LaunchedEffect.
 *
 * @param bypassLifecycleCheck don't wait the current entry lifecycle to be [Lifecycle.State.RESUMED]
 *
 * @see [NavController.navigate]
 */
fun NavController.safeNavigate(
    route: String,
    bypassLifecycleCheck: Boolean = false,
    builder: NavOptionsBuilder.() -> Unit = { },
) {
    val currentLifecycleState = this.currentBackStackEntry?.lifecycle?.currentState
    when {
        // Handle case where there is no back stack entry yet in the nav controller or the there is a destroyed one. It happens on activity
        // re-creation because the graph might not be initialized (null) or after an autolock (destroyed)
        // Wait until we have a non-null & non destroyed state before navigating
        currentLifecycleState == null || currentLifecycleState == Lifecycle.State.DESTROYED -> {
            CoroutineScope(Dispatchers.Main.immediate).launch {
                this@safeNavigate.currentBackStackEntryFlow.first { it.lifecycle.currentState != Lifecycle.State.DESTROYED }
                navigate(route, navOptions(builder))
            }
        }
        // Allow navigation only if the state is at least resumed or if bypassLifecycleCheck is set
        bypassLifecycleCheck || currentLifecycleState.isAtLeast(Lifecycle.State.RESUMED) -> {
            navigate(route, navOptions(builder))
        }
        else -> {
            logger.v("Navigation to $route is not possible. Current backstack entry lifecycle is $currentLifecycleState")
        }
    }
}
