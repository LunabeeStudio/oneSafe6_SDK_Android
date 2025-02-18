package studio.lunabee.onesafe.navigation.graph

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.lunabee.lblogger.LBLogger
import studio.lunabee.onesafe.commonui.utils.safeNavigate
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavGraphDestination
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavigation
import studio.lunabee.onesafe.feature.breadcrumb.breadcrumbScreen
import studio.lunabee.onesafe.feature.exportbackup.auth.ExportAuthDestination
import studio.lunabee.onesafe.feature.exportbackup.exportdata.ExportDataDestination
import studio.lunabee.onesafe.feature.exportbackup.getarchive.ExportGetArchiveDestination
import studio.lunabee.onesafe.feature.fileviewer.fileViewerScreen
import studio.lunabee.onesafe.feature.forceupgrade.ForceUpgradeDestination
import studio.lunabee.onesafe.feature.forceupgrade.forceUpgradeGraph
import studio.lunabee.onesafe.feature.home.HomeDestination
import studio.lunabee.onesafe.feature.importbackup.auth.ImportAuthDestination
import studio.lunabee.onesafe.feature.importbackup.bubbleswarning.ImportBubblesWarningDestination
import studio.lunabee.onesafe.feature.importbackup.importGraph
import studio.lunabee.onesafe.feature.importbackup.nofullysupported.NotFullySupportedArchiveDestination
import studio.lunabee.onesafe.feature.importbackup.savedata.ImportSaveDataDestination
import studio.lunabee.onesafe.feature.importbackup.selectdata.ImportSelectDataDestination
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileDestination
import studio.lunabee.onesafe.feature.itemdetails.ItemDetailsDestination
import studio.lunabee.onesafe.feature.itemfielddetail.itemFieldDetailsScreen
import studio.lunabee.onesafe.feature.itemform.destination.itemCreationGraph
import studio.lunabee.onesafe.feature.itemform.destination.itemEditionGraph
import studio.lunabee.onesafe.feature.keyboard.KeyboardOnBoardingDestination
import studio.lunabee.onesafe.feature.keyboard.keyboardOnBoardingNavGraph
import studio.lunabee.onesafe.feature.migration.savedata.migrationSaveDataGraph
import studio.lunabee.onesafe.feature.move.movehost.moveGraph
import studio.lunabee.onesafe.feature.settings.SettingsDestination
import studio.lunabee.onesafe.feature.settings.SettingsNavigation
import studio.lunabee.onesafe.feature.settings.about.CreditsDestination
import studio.lunabee.onesafe.feature.settings.about.aboutGraph
import studio.lunabee.onesafe.feature.settings.autodestruction.autoDestructionSettingsGraph
import studio.lunabee.onesafe.feature.settings.autodestruction.onboarding.AutoDestructionOnBoardingDestination
import studio.lunabee.onesafe.feature.settings.autofill.autofillSettingsGraph
import studio.lunabee.onesafe.feature.settings.libraries.LibrariesDestination
import studio.lunabee.onesafe.feature.settings.libraries.librariesGraph
import studio.lunabee.onesafe.feature.settings.navigation.SecuritySettingsGraphNavigation
import studio.lunabee.onesafe.feature.settings.navigation.bubblesSettingsGraph
import studio.lunabee.onesafe.feature.settings.navigation.exportBackupGraph
import studio.lunabee.onesafe.feature.settings.navigation.securitySettingsGraph
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionBackupDestination
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionKeyDestination
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionSettingDisabledNavGraphNavigation
import studio.lunabee.onesafe.feature.settings.overencryption.OverEncryptionSettingEnabledNavigation
import studio.lunabee.onesafe.feature.settings.panicwidget.panicWidgetSettingsGraph
import studio.lunabee.onesafe.feature.settings.personalization.personalizationGraph
import studio.lunabee.onesafe.feature.settings.security.SecuritySettingNavigation
import studio.lunabee.onesafe.feature.settings.settingsGraph
import studio.lunabee.onesafe.feature.share.shareNavGraph
import studio.lunabee.onesafe.feature.verifypassword.verifyPasswordNavGraph
import studio.lunabee.onesafe.importexport.settings.autoBackupSettingsNavGraph
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.login.screen.loginGraph
import studio.lunabee.onesafe.messaging.ui.messagingNavGraph
import studio.lunabee.onesafe.navigation.extension.popUpToItemId

private val logger = LBLogger.get("MainNavGraph")

@OptIn(ExperimentalAnimationApi::class)
@Suppress("LongMethod")
@Composable
fun MainNavGraph(
    showSnackBarWithNav: (visuals: SnackbarVisuals, (breadcrumbNavigate: BreadcrumbOnCompositionNav.Navigate) -> Unit) -> Unit,
    mainNavController: NavHostController,
    modifier: Modifier = Modifier,
    navGraphStartDestinationRoute: String,
    isUserSignUp: Boolean,
    setBreadcrumbNavController: ((NavHostController) -> Unit)?,
) {
    val navigateBack = remember(mainNavController) {
        // Safe check https://github.com/google/accompanist/blob/856e9c277f97e5ae0deb7753224292b6ab9df12a/sample/src/main/java/com/google/accompanist/sample/navigation/animation/AnimatedNavHostSample.kt#L304-L309
        {
            if (mainNavController.previousBackStackEntry != null) {
                mainNavController.popBackStack()
            }
        }
    }

    // remember the route in case of config change
    var breadcrumbOnCompositionNav: BreadcrumbOnCompositionNav? = remember { null }

    // breadcrumbNavController is stored here so we can use it (here). In dev, it is also stored in RootContent for debugging purpose
    var breadcrumbNavController: NavHostController? = null

    val showSnackBar: (visuals: SnackbarVisuals) -> Unit = { visuals ->
        showSnackBarWithNav(visuals) { breadcrumbNavigate ->
            if (breadcrumbNavController?.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                breadcrumbNavController?.safeNavigate(breadcrumbNavigate.route)
                    ?: logger.e("Cannot navigate because breadcrumbNavController is null")
            } else {
                breadcrumbOnCompositionNav = breadcrumbNavigate
            }
        }
    }

    MainNavHost(
        navController = mainNavController,
        startDestination = navGraphStartDestinationRoute,
        modifier = modifier,
        builder = {
            forceUpgradeGraph(
                navigateToStart = {
                    val destination = if (isUserSignUp) LoginDestination.route else OnBoardingNavGraphDestination.route
                    mainNavController.safeNavigate(destination) { popUpTo(ForceUpgradeDestination.route) { inclusive = true } }
                },
            )

            onBoardingNavGraph(
                navigateBack = navigateBack,
                navController = mainNavController,
            )

            multiSafeOnBoardingGraph(
                navController = mainNavController,
                navigateBack = navigateBack,
            )

            changePasswordNavGraph(
                navController = mainNavController,
                navigateBack = navigateBack,
                showSnackBar = showSnackBar,
            )

            loginGraph(
                navigateToHome = { restoreState ->
                    mainNavController.safeNavigate(BreadcrumbNavGraphDestination.route) {
                        // FIXME force reset the stored OnBackPressedDispatcher in case of restoration after autolock (fix back nav)
                        try {
                            breadcrumbNavController?.graph // throw if not a restoration
                            breadcrumbNavController?.setOnBackPressedDispatcher(OnBackPressedDispatcher(null))
                        } catch (_: IllegalStateException) {
                            /* no-op */
                        }
                        this.restoreState = restoreState
                        popUpTo(LoginDestination.route) { inclusive = true }
                    }
                },
                unsafeNavigateToHome = {
                    mainNavController.navigate(BreadcrumbNavGraphDestination.route) {
                        popUpTo(LoginDestination.route) { inclusive = true }
                    }
                },
                navigateToMultiSafeOnBoarding = { mainNavController.navigate(MultiSafeOnBoardingNavGraphDestination.route) },
            )

            breadcrumbScreen(
                breadcrumbNavigation = BreadcrumbNavigation(
                    mainNavController = mainNavController,
                    navigateBack = navigateBack,
                    onCompositionNav = {
                        val onCompositionNav = breadcrumbOnCompositionNav
                        breadcrumbOnCompositionNav = null
                        onCompositionNav
                    },
                ),
                setBreadcrumbNavController = { navHostController ->
                    breadcrumbNavController = navHostController
                    setBreadcrumbNavController?.invoke(navHostController)
                },
            )

            itemCreationGraph(
                navigateBack = navigateBack,
                navigateToItemDetails = { safeItemId ->
                    val route = ItemDetailsDestination.getRoute(safeItemId)
                    if (breadcrumbNavController?.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        breadcrumbNavController?.safeNavigate(
                            route = route,
                            bypassLifecycleCheck = true,
                        ) ?: logger.e("Cannot navigateToItemDetails, breadcrumbNavController is null")
                    } else {
                        breadcrumbOnCompositionNav = BreadcrumbOnCompositionNav.Navigate(route)
                    }
                    mainNavController.popBackStack()
                },
            )

            itemEditionGraph(
                navigateBack = navigateBack,
            )

            itemFieldDetailsScreen(
                navigateBack = navigateBack,
            )

            fileViewerScreen(
                navigateBack = navigateBack,
            )

            settingsGraph(
                settingsNavigation = SettingsNavigation(mainNavController, navigateBack),
            )

            aboutGraph(
                navigateBack = navigateBack,
                navigateToCredits = { mainNavController.safeNavigate(CreditsDestination.route) },
                onClickOnLibraries = { mainNavController.safeNavigate(LibrariesDestination.route) },
            )

            exportBackupGraph(
                navigateBack = navigateBack,
                showSnackBar = { showSnackBar(it) },
                navigateBackToSettingsDestination = {
                    mainNavController.popBackStack(
                        route = SettingsDestination.route,
                        inclusive = false,
                    )
                },
                navigateToExportAuthDestination = {
                    mainNavController.popBackStack(
                        route = ExportAuthDestination.route,
                        inclusive = false,
                    )
                },
                navigateToExportDataDestination = { itemCount: Int, contactCount: Int, safeNav: Boolean ->
                    if (safeNav) {
                        mainNavController.safeNavigate(
                            route = ExportDataDestination.getRoute(
                                itemCount = itemCount,
                                contactCount = contactCount,
                            ),
                        )
                    } else {
                        mainNavController.navigate(
                            route = ExportDataDestination.getRoute(
                                itemCount = itemCount,
                                contactCount = contactCount,
                            ),
                        )
                    }
                },
                navigateToExportGetArchiveDestination = {
                    mainNavController.safeNavigate(route = ExportGetArchiveDestination.getRoute(it))
                },
            )

            with(securitySettingsGraphNavigation(navigateBack, mainNavController)) {
                securitySettingsGraph(
                    showSnackBar = showSnackBar,
                    getBackStackEntry = { route -> mainNavController.getBackStackEntry(route) },
                )
            }

            bubblesSettingsGraph(
                navigateBack = navigateBack,
                navigateToKeyboardOnBoarding = { mainNavController.safeNavigate(KeyboardOnBoardingDestination.route) },
            )

            autofillSettingsGraph(
                navigateBack = navigateBack,
            )

            librariesGraph(
                navigateBack = navigateBack,
            )

            personalizationGraph(
                navigateBack = navigateBack,
            )

            importGraph(
                navigateToImportAuthDestination = { mainNavController.navigate(ImportAuthDestination.route) },
                navigateBackToSettings = { mainNavController.popBackStack(ImportFileDestination.route, inclusive = true) },
                navigateToImportSettingsDestination = { mainNavController.safeNavigate(ImportSaveDataDestination.route) },
                navigateBack = navigateBack,
                navigateBackToFileSelection = { mainNavController.popBackStack(ImportFileDestination.route, inclusive = false) },
                showSnackBar = { showSnackBar(it) },
                navigateToWarningNotFullySupportedArchive = { mainNavController.safeNavigate(NotFullySupportedArchiveDestination.route) },
                navigateToSelectImportDataDestination = { mainNavController.safeNavigate(ImportSelectDataDestination.route) },
                navigateToWarningBubbles = { mainNavController.safeNavigate(ImportBubblesWarningDestination.route) },
            )

            autoBackupSettingsNavGraph(
                navigateBack = navigateBack,
                navigateToRestoreBackup = { mainNavController.safeNavigate(ImportFileDestination.getRoute(it, false)) },
            )

            migrationSaveDataGraph(
                navigateToHomeScreen = {
                    val route = HomeDestination.route
                    if (breadcrumbNavController?.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        breadcrumbNavController?.popBackStack(route, inclusive = false)
                            ?: logger.e("Cannot navigateToHomeScreen, breadcrumbNavController is null")
                    } else {
                        breadcrumbOnCompositionNav = BreadcrumbOnCompositionNav.PopToExclusive(route)
                    }
                    mainNavController.popBackStack(BreadcrumbNavGraphDestination.route, inclusive = false)
                },
                navigateBack = navigateBack,
                showSnackBar = showSnackBar,
            )

            moveGraph(
                navigateBack = navigateBack,
                showSnackBar = showSnackBar,
                navigateToHome = {
                    val route = HomeDestination.route
                    if (breadcrumbNavController?.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        breadcrumbNavController?.popBackStack(route, inclusive = false)
                            ?: logger.e("Cannot navigateToHomeScreen, breadcrumbNavController is null")
                    } else {
                        breadcrumbOnCompositionNav = BreadcrumbOnCompositionNav.PopToExclusive(route)
                    }
                    mainNavController.popBackStack(BreadcrumbNavGraphDestination.route, inclusive = false)
                },
                navigateToItem = { itemId ->
                    if (breadcrumbNavController?.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        breadcrumbNavController?.popUpToItemId(itemId)
                            ?: logger.e("Cannot navigateToHomeScreen, breadcrumbNavController is null")
                    } else {
                        breadcrumbOnCompositionNav = BreadcrumbOnCompositionNav.PopToItem(itemId)
                    }
                    mainNavController.popBackStack(BreadcrumbNavGraphDestination.route, inclusive = false)
                },
            )

            shareNavGraph(
                navController = mainNavController,
                navigateBack = navigateBack,
            )

            verifyPasswordNavGraph(
                navController = mainNavController,
                navigateBack = navigateBack,
            )

            keyboardOnBoardingNavGraph(
                navigateBack = navigateBack,
                navController = mainNavController,
            )

            autoDestructionSettingsGraph(
                mainNavController = mainNavController,
                showSnackBar = showSnackBar,
            )

            panicWidgetSettingsGraph(
                navigateBack = navigateBack,
            )

            with(
                MainMessagingGraphNavigation(
                    breadcrumbNavController,
                    mainNavController,
                    { breadcrumbOnCompositionNav = it },
                    navigateBack,
                    showSnackBar,
                ),
            ) {
                messagingNavGraph(
                    navController = mainNavController,
                )
            }
        },
    )
}

private fun securitySettingsGraphNavigation(
    navigateBack: () -> Unit,
    mainNavController: NavHostController,
) = SecuritySettingsGraphNavigation(
    securitySettingNavigation = SecuritySettingNavigation(
        navigateBack = navigateBack,
        navigateToAutoDestructionSetting = { mainNavController.safeNavigate(AutoDestructionOnBoardingDestination.route) },
    ),
    overEncryptionSettingNavGraphNavigation = OverEncryptionSettingDisabledNavGraphNavigation(
        navigateBack = navigateBack,
        navigateToOverEncryptionBackup = {
            mainNavController.safeNavigate(route = OverEncryptionBackupDestination.route)
        },
        navigateToOverEncryptionKey = { doBackup ->
            mainNavController.safeNavigate(route = OverEncryptionKeyDestination.getRoute(doBackup))
        },
    ),
    overEncryptionEnabledNavigation = OverEncryptionSettingEnabledNavigation(
        navigateBack = navigateBack,
    ),
)
