package studio.lunabee.onesafe.feature.settings.autodestruction.confirmpassword

import android.net.Uri
import androidx.compose.material3.SnackbarVisuals
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import kotlinx.serialization.Serializable
import studio.lunabee.onesafe.commonui.OSDestination

@Serializable
object AutoDestructionPasswordConfirmationDestination : OSDestination {
    const val PasswordHashArg: String = "PasswordHash"
    const val SaltArg: String = "Salt"
    const val path: String = "AutoDestructionPasswordConfirmation"
    override val route: String = "$path?$PasswordHashArg={$PasswordHashArg}&$SaltArg={$SaltArg}"

    fun getRoute(password: String, salt: String): String {
        return Uri.Builder().apply {
            path(path)
            appendQueryParameter(PasswordHashArg, password)
            appendQueryParameter(SaltArg, salt)
        }.build().toString()
    }
}

interface AutoDestructionPasswordConfirmationNavScope {
    val navigateBack: () -> Unit
    val showSnackBar: (visuals: SnackbarVisuals) -> Unit
    val navigateBackToSettings: () -> Unit
}

fun NavGraphBuilder.autoDestructionPasswordConfirmationGraph(
    navScope: AutoDestructionPasswordConfirmationNavScope,
) {
    composable(
        route = AutoDestructionPasswordConfirmationDestination.route,
        arguments = listOf(
            navArgument(AutoDestructionPasswordConfirmationDestination.PasswordHashArg) {
                type = NavType.StringType
            },
            navArgument(AutoDestructionPasswordConfirmationDestination.SaltArg) {
                type = NavType.StringType
            },
        ),
    ) {
        AutoDestructionPasswordConfirmationRoute(
            navigateBack = navScope.navigateBack,
            showSnackBar = navScope.showSnackBar,
            navigateBackToSettings = navScope.navigateBackToSettings,
        )
    }
}
