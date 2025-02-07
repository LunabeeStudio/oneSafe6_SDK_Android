package studio.lunabee.onesafe.feature.autofill

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import studio.lunabee.onesafe.feature.autofill.creation.AfItemCreationDestination
import studio.lunabee.onesafe.feature.autofill.creation.AfItemCreationRoute
import studio.lunabee.onesafe.feature.autofill.itemlist.AfItemListDestination
import studio.lunabee.onesafe.feature.autofill.itemlist.AfItemListRoute
import studio.lunabee.onesafe.feature.autofill.login.AutoFillLoginDestination
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavHost
import studio.lunabee.onesafe.feature.home.model.ItemCreationEntryWithTemplate
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.login.screen.LoginRoute
import studio.lunabee.onesafe.navigation.graph.GraphIdentifier
import studio.lunabee.onesafe.navigation.graph.MultiSafeOnBoardingNavGraphDestination

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AutoFillNavGraph(
    navController: NavHostController,
    onCredentialProvided: (identifier: String, password: String) -> Unit,
    clientDomain: String,
    clientPackage: String,
    finish: () -> Unit,
    saveCredential: Boolean,
    identifier: String?,
    password: String?,
) {
    BreadcrumbNavHost(
        navController = navController,
        startDestination = AutoFillLoginDestination.route,
        isInSearchMode = false,
        graphIdentifier = GraphIdentifier.AutoFillNavGraph,
        onCompositionNav = { null },
    ) {
        composable(
            route = AutoFillLoginDestination.route,
        ) {
            LoginRoute(
                onSuccess = {
                    if (saveCredential) {
                        navController.navigate(
                            AfItemCreationDestination.getRoute(
                                itemType = ItemCreationEntryWithTemplate.Website.template.name,
                                identifier = identifier,
                                password = password,
                                clientPackage = clientPackage,
                                clientDomain = clientDomain,
                            ),
                        )
                    } else {
                        navController.navigate(AfItemListDestination.getRoute(clientDomain, clientPackage)) {
                            popUpTo(LoginDestination.route) { inclusive = true }
                        }
                    }
                },
                onCreateNewSafe = {
                    navController.navigate(MultiSafeOnBoardingNavGraphDestination.route)
                },
            )
        }

        composable(
            route = AfItemListDestination.route,
            arguments = listOf(
                navArgument(AfItemListDestination.ClientDomainArgs) {
                    type = NavType.StringType
                },
                navArgument(AfItemListDestination.ClientPackageArgs) {
                    type = NavType.StringType
                },
            ),
        ) {
            AfItemListRoute(
                navigateBack = finish,
                onCredentialProvided = onCredentialProvided,
            )
        }

        composable(
            route = AfItemCreationDestination.route,
        ) {
            AfItemCreationRoute(
                navigateBack = finish,
            )
        }
    }
}
