package studio.lunabee.onesafe.commonui.snackbar

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.platform.LocalContext
import com.lunabee.lblogger.LBLogger
import studio.lunabee.compose.core.LbcTextSpec

private val logger = LBLogger.get<SnackbarState>()

@Stable
abstract class SnackbarState(
    private val snackbarAction: SnackbarAction? = null,
) {
    protected abstract val message: LbcTextSpec
    protected open val duration: SnackbarDuration? = null

    val snackbarVisuals: SnackbarVisuals
        @Composable
        get() = getSnackbarVisuals(LocalContext.current)

    fun getSnackbarVisuals(context: Context): SnackbarVisuals {
        return when (snackbarAction) {
            null -> DefaultSnackbarVisuals(
                message = this@SnackbarState.message.string(context),
                actionLabel = null,
                duration = this@SnackbarState.duration ?: SnackbarDuration.Short,
                withDismissAction = false,
            )
            is SnackbarAction.Default -> ActionSnackbarVisuals(
                message = this@SnackbarState.message.string(context),
                action = snackbarAction.onClick,
                onDismiss = snackbarAction.onDismiss,
                actionLabel = snackbarAction.actionLabel.string(context),
                duration = this@SnackbarState.duration ?: SnackbarDuration.Short,
                withDismissAction = false,
            )
            is SnackbarAction.Navigation -> NavigationSnackbarVisuals(
                message = this@SnackbarState.message.string(context),
                route = this@SnackbarState.snackbarAction.route,
                onDismiss = snackbarAction.onDismiss,
                actionLabel = this@SnackbarState.snackbarAction.actionLabel.string(context),
                duration = this@SnackbarState.duration ?: SnackbarDuration.Short,
                withDismissAction = false,
            )
        }
    }

    /**
     * Show the snackbar using the [snackbarHostState] and trigger the action if applicable (for visuals [SnackbarAction.Default]). Call
     * [onResult] when the snackbar has been dismissed (on showSnackbar return)
     */
    @Composable
    fun LaunchedSnackbarEffect(snackbarHostState: SnackbarHostState, onResult: () -> Unit = {}) {
        val visuals = getSnackbarVisuals(LocalContext.current)
        LaunchedEffect(Unit) {
            val result = snackbarHostState.showSnackbar(visuals)
            onResult()
            when (result) {
                SnackbarResult.Dismissed -> when (visuals) {
                    is ActionSnackbarVisuals -> visuals.onDismiss()
                    is NavigationSnackbarVisuals -> visuals.onDismiss()
                    else -> {
                        /* no-op  : No need to call the onDismiss method */
                    }
                }
                SnackbarResult.ActionPerformed -> {
                    when (visuals) {
                        is ActionSnackbarVisuals -> {
                            visuals.action()
                            visuals.onDismiss()
                        }
                        else -> logger.e("Unexpected snackbar action performed $visuals")
                    }
                }
            }
        }
    }
}
