package studio.lunabee.onesafe

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.net.toFile
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import com.lunabee.lblogger.LBLogger
import com.lunabee.lblogger.e
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.messaging.domain.usecase.ManagingIncomingMessageResultData
import studio.lunabee.onesafe.bubbles.ui.contact.form.frominvitation.CreateContactFromInvitationDestination
import studio.lunabee.onesafe.bubbles.ui.extension.getBase64FromMessage
import studio.lunabee.onesafe.common.extensions.isAutoBackup
import studio.lunabee.onesafe.commonui.CommonUiConstants
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.SplashScreenManager
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.commonui.extension.hasUriReadPermission
import studio.lunabee.onesafe.commonui.extension.isTest
import studio.lunabee.onesafe.commonui.localprovider.LocalItemStyle
import studio.lunabee.onesafe.crashlytics.CrashlyticsCustomKeys
import studio.lunabee.onesafe.crashlytics.CrashlyticsHelper
import studio.lunabee.onesafe.crashlytics.CrashlyticsUnknown
import studio.lunabee.onesafe.domain.utils.FileHelper.extension
import studio.lunabee.onesafe.feature.autolock.AndroidAutoLockAppChangeManager
import studio.lunabee.onesafe.feature.autolock.AndroidAutoLockAppClosedManager
import studio.lunabee.onesafe.feature.autolock.AndroidAutoLockInactivityManager
import studio.lunabee.onesafe.feature.autolock.DeleteOldSentMessageManager
import studio.lunabee.onesafe.feature.breadcrumb.BreadcrumbNavGraphDestination
import studio.lunabee.onesafe.feature.congratulation.destination.CongratulationOnBoardingDestination
import studio.lunabee.onesafe.feature.importbackup.selectfile.ImportFileDestination
import studio.lunabee.onesafe.feature.itemform.destination.ItemCreationDestination
import studio.lunabee.onesafe.feature.migration.MigrationManager
import studio.lunabee.onesafe.feature.migration.savedata.MigrationSaveDataDestination
import studio.lunabee.onesafe.feature.settings.bubbles.BubblesSettingsDestination
import studio.lunabee.onesafe.feature.settings.security.SecureScreenManager
import studio.lunabee.onesafe.help.main.HelpActivity
import studio.lunabee.onesafe.importexport.model.ImportExportConstant
import studio.lunabee.onesafe.login.screen.LoginDestination
import studio.lunabee.onesafe.messaging.writemessage.destination.WriteMessageDestination
import studio.lunabee.onesafe.ui.theme.OSTheme
import studio.lunabee.onesafe.utils.oSDefaultEnableEdgeToEdge
import studio.lunabee.onesafe.window.LocalOnTouchWindow
import studio.lunabee.onesafe.window.LocalWindow
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private val logger = LBLogger.get<MainActivity>()

@AndroidEntryPoint
open class MainActivity : FragmentActivity() {

    @Inject lateinit var autoLockInactivityManager: AndroidAutoLockInactivityManager

    @Inject lateinit var autoLockAppChangeManager: AndroidAutoLockAppChangeManager

    @Inject lateinit var autoLockAppClosedManager: AndroidAutoLockAppClosedManager

    @Inject lateinit var deleteOldSentMessageManager: DeleteOldSentMessageManager

    @Inject lateinit var secureScreenManager: SecureScreenManager

    @Inject lateinit var splashScreenManager: SplashScreenManager

    val viewModel: MainActivityViewModel by viewModels()

    private val mainNavController: MutableStateFlow<NavHostController?> = MutableStateFlow(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        setupSplashScreen()
        oSDefaultEnableEdgeToEdge()
        super.onCreate(savedInstanceState)
        checkDatabaseAccess()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        startService(Intent(this, ClearFileCacheService::class.java))

        lifecycle.addObserver(autoLockInactivityManager)
        lifecycle.addObserver(autoLockAppChangeManager)
        if (!intent.isTest) {
            lifecycle.addObserver(autoLockAppClosedManager)
        }
        lifecycle.addObserver(deleteOldSentMessageManager)

        // Auto remove old items on launch
        viewModel.removeOldItems()

        try {
            // FIXME
            //  https://issuetracker.google.com/issues/226665301
            //  https://stackoverflow.com/q/68949292/10935947
            Class
                .forName(AppConstants.Reflection.tabRowKt)
                .getDeclaredField(AppConstants.Reflection.scrollableTabRowMinimumTabWidth).apply {
                    isAccessible = true
                }.set(null, 0f)
        } catch (e: Exception) {
            e.printStackTrace()
            logger.e(e)
        }

        // Check savedInstanceState != null <> activity recreation
        if (savedInstanceState == null) {
            handleIntent(intent)
        }

        setContent {
            val mainNavController = rememberNavController().also {
                this.mainNavController.value = it
            }

            val state by mainNavController.currentBackStackEntryAsState()
            state?.let {
                val route = kotlin.runCatching { it.destination.route }.getOrNull()
                LaunchedEffect(route) {
                    CrashlyticsHelper.setCustomKey(
                        CrashlyticsCustomKeys.MainNavScreen,
                        route ?: CrashlyticsUnknown,
                    )
                }
            }

            val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
            val navigation by viewModel.navigation.collectAsStateWithLifecycle()
            dialogState?.DefaultAlertDialog()

            val snackbarHostState = remember { SnackbarHostState() }
            val snackbarState by viewModel.snackbarState.collectAsStateWithLifecycle()
            snackbarState?.LaunchedSnackbarEffect(snackbarHostState, viewModel::resetSnackbarState)

            LaunchedEffect(navigation) {
                navigation?.let { navigation ->
                    when (navigation) {
                        is MainActivityNavigation.BubblesSettings -> mainNavController.navigate(BubblesSettingsDestination.route) {
                            popUpTo(BubblesSettingsDestination.route) { inclusive = true }
                        }
                        is MainActivityNavigation.ItemCreationFromFileUrl -> mainNavController.navigate(
                            route = ItemCreationDestination.getRouteFromFileUri(
                                uriList = listOf(navigation.uri),
                                itemParentId = null,
                                color = null,
                            ),
                        )
                        is MainActivityNavigation.WriteMessage -> mainNavController.navigate(
                            WriteMessageDestination.getRouteFromDecryptResult(
                                navigation.result,
                            ),
                        )
                    }
                    viewModel.consumeNavigation()
                }
            }

            val isMaterialYouEnabled by viewModel.isMaterialYouEnabled.collectAsStateWithLifecycle()
            val isSafeReady by viewModel.isSafeReady.collectAsStateWithLifecycle()
            val itemStyleHolder by viewModel.itemStyleHolder.collectAsStateWithLifecycle()

            LaunchedEffect(key1 = isSafeReady) {
                secureScreenManager(this@MainActivity)
            }

            CompositionLocalProvider(
                LocalOnTouchWindow.provides(autoLockInactivityManager::refreshLastUserInteraction),
                LocalWindow provides window,
                LocalItemStyle provides itemStyleHolder,
            ) {
                RootView(
                    isMaterialYouSettingsEnabled = isMaterialYouEnabled,
                    navController = mainNavController,
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }

    private fun setupSplashScreen() {
        val timeout = System.currentTimeMillis() + 10_000
        installSplashScreen().setKeepOnScreenCondition {
            !splashScreenManager.isAppReady && System.currentTimeMillis() < timeout
        }
    }

    override fun onCreateView(parent: View?, name: String, context: Context, attrs: AttributeSet): View? {
        parent?.accessibilityDelegate = object : View.AccessibilityDelegate() {
            override fun onRequestSendAccessibilityEvent(host: ViewGroup, child: View, event: AccessibilityEvent): Boolean {
                autoLockInactivityManager.refreshLastUserInteraction()
                return super.onRequestSendAccessibilityEvent(host, child, event)
            }
        }
        return super.onCreateView(parent, name, context, attrs)
    }

    private fun checkDatabaseAccess() {
        runBlocking {
            if (!viewModel.checkDatabaseAccess()) {
                HelpActivity.launch(this@MainActivity, intent)
                finish()
            }
        }
    }

    override fun onPause() {
        // Always try to hide screen (flaky on slow devices but better than nothing? 🤷)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        super.onPause()
    }

    override fun onResume() {
        secureScreenManager(this)
        super.onResume()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        autoLockInactivityManager.refreshLastUserInteraction()
    }

    private fun waitForCryptoLoadedAndMigrate(archiveUri: Uri) {
        waitForCryptoLoadedAndPerformNavigation { controller ->
            controller.navigate(
                route = MigrationSaveDataDestination.getRoute(archiveUri),
                navOptions = navOptions {
                    // FIXME using `launchSingleTop = true` lead to crash -> try to reproduce and create issue
                    popUpTo(MigrationSaveDataDestination.route) {
                        inclusive = true
                    }
                },
            )
        }
    }

    @OptIn(FlowPreview::class)
    private fun waitForCryptoLoadedAndPerformNavigation(navigate: suspend (NavHostController) -> Unit) {
        lifecycleScope.launch {
            viewModel.waitCryptoDataReadyInMemory()
            try {
                mainNavController.filterNotNull().timeout(10.seconds).first() // Wait the nav controller
            } catch (e: TimeoutCancellationException) {
                logger.e(e, "mainNavController is null")
                null
            }?.let { controller ->
                // Wait until Home is in backstack
                // Ideally we would use controller.currentBackStack, but restricted to lib
                controller.currentBackStackEntryFlow.firstOrNull { stackEntry ->
                    stackEntry.destination.route != LoginDestination.route &&
                        stackEntry.destination.route != CongratulationOnBoardingDestination.route // in case of deeplink + onboarding
                }

                // Nav to import (do not use safeNavigate)
                navigate(controller)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    // TODO remove navigation anim on deeplink nav
    private fun handleIntent(newIntent: Intent?) {
        val uri = newIntent?.data ?: return
        when {
            MigrationManager.isAllowedMigrationIntent(this, newIntent) -> {
                waitForCryptoLoadedAndMigrate(uri)
            }
            uri.isAutoBackup() || containsOSFile(newIntent) -> {
                handleImportBackupIntent(newIntent.extras, uri)
            }
            uri.scheme == CommonUiConstants.Deeplink.MAIN_NAV_SCHEME -> {
                waitForCryptoLoadedAndPerformNavigation { controller ->
                    uri.authority?.let { authority ->
                        val route = authority + uri.toString().substringAfter(authority)
                        logger.i("Deeplink to $route")
                        controller.navigate(
                            route = route,
                        ) {
                            popUpTo(route = route) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
            uri.toString().startsWith(CommonUiConstants.Deeplink.BubblesDeeplinkUrl.toString()) && !uri.fragment.isNullOrBlank() -> {
                waitForCryptoLoadedAndPerformNavigation { controller ->
                    viewModel.handleBubblesDeeplink(uri)?.let { result ->
                        when (result) {
                            is ManagingIncomingMessageResultData.SafeItem -> error("should not append")
                            is ManagingIncomingMessageResultData.Invitation -> {
                                val messageString = uri.getBase64FromMessage()
                                controller.navigate(CreateContactFromInvitationDestination.getRoute(messageString)) {
                                    popUpTo(BreadcrumbNavGraphDestination.route) { inclusive = false }
                                }
                            }
                            is ManagingIncomingMessageResultData.Message -> {
                                result.decryptResult.error?.let {
                                    viewModel.displaySnackbarMessage(
                                        it.error.description(LbcTextSpec.StringResource(OSString.error_defaultMessage)),
                                    )
                                } ?: controller.navigate(
                                    WriteMessageDestination.getRouteFromDecryptResult(result.decryptResult),
                                ) { popUpTo(BreadcrumbNavGraphDestination.route) { inclusive = false } }
                            }
                        }
                    }
                }
            }
            newIntent.hasUriReadPermission -> viewModel.handleFile(uri)
            else -> {
                logger.e("Missing permission to read $uri")
                viewModel.displaySnackbarMessage(LbcTextSpec.StringResource(OSString.error_file_cannotOpen_message, uri.toString()))
            }
        }
        // Consume the data of the intent to avoid multiple handling (in conjunction with savedInstanceState nullity check)
        // FIXME do not consume if running instrumented test https://github.com/android/android-test/issues/1939
        if (!intent.isTest) {
            intent.data = null
        }
    }

    private fun containsOSFile(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        val localFile = try {
            uri.toFile().takeIf { it.exists() }
        } catch (_: Exception) {
            null
        }

        val extension = localFile?.extension ?: if (intent.hasUriReadPermission) {
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null).use { cursor ->
                cursor?.let {
                    cursor.moveToFirst()
                    cursor.getString(0).extension()
                }
            }
        } else {
            null
        }
        return extension?.let { ImportExportConstant.isOS6Extension(it) } ?: false
    }

    private fun handleImportBackupIntent(extra: Bundle?, uri: Uri) {
        waitForCryptoLoadedAndPerformNavigation { controller ->
            val deleteOnComplete = extra?.getBoolean(CommonUiConstants.AppLaunch.DeleteOnImportExtraKey, false) ?: false
            if (deleteOnComplete) {
                runCatching { uri.toFile().deleteOnExit() }
            }
            controller.navigate(
                route = ImportFileDestination.getRoute(
                    data = uri,
                    deleteOnComplete = deleteOnComplete,
                ),
            )
        }
    }
}

@Composable
private fun RootView(
    isMaterialYouSettingsEnabled: Boolean,
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
) {
    OSTheme(isMaterialYouSettingsEnabled = isMaterialYouSettingsEnabled) {
        RootContent(
            navController = navController,
            snackbarHostState = snackbarHostState,
        )
    }
}
