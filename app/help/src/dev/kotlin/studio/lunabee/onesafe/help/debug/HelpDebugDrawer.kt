package studio.lunabee.onesafe.help.debug

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Switch
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.color.DynamicColors
import com.lunabee.lbcore.model.LBResult
import com.lunabee.lbloading.rememberLoadingManager
import com.lunabee.lblogger.LBLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSRegularDivider
import studio.lunabee.onesafe.atom.text.OSText
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.dialog.DefaultAlertDialog
import studio.lunabee.onesafe.commonui.dialog.rememberDialogState
import studio.lunabee.onesafe.commonui.extension.copyToClipBoard
import studio.lunabee.onesafe.commonui.extension.findFragmentActivity
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.commonui.notification.NotificationPermissionRationaleDialogState
import studio.lunabee.onesafe.help.debug.extension.resolveArgsToString
import studio.lunabee.onesafe.help.debug.extension.startLocalBackupWorker
import studio.lunabee.onesafe.help.debug.item.DbgNavRoute
import studio.lunabee.onesafe.help.debug.item.DebugAutoBackup
import studio.lunabee.onesafe.help.debug.item.DebugCloudBackup
import studio.lunabee.onesafe.help.debug.item.DebugLocalBackup
import studio.lunabee.onesafe.help.debug.item.DebugOneTimeAction
import studio.lunabee.onesafe.help.debug.item.DebugOneTimeActionData
import studio.lunabee.onesafe.help.debug.item.DebugSafeItem
import studio.lunabee.onesafe.help.debug.item.DebugSafeItemData
import studio.lunabee.onesafe.help.debug.item.SettingsDebugMenu
import studio.lunabee.onesafe.help.debug.model.HelpDebugUiState
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import java.time.Instant
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.time.Duration.Companion.seconds

private val logger = LBLogger.get("RootDrawer")

@OptIn(ExperimentalComposeUiApi::class, ExperimentalPermissionsApi::class)
@Composable
internal fun HelpRootDrawer(
    navController: NavController,
    viewModel: HelpDebugViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var permissionDialogState by rememberDialogState()
    permissionDialogState?.DefaultAlertDialog()

    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS) {
            // Launch anyway (works w/wo notification)
            WorkManager.getInstance(context).startLocalBackupWorker()
        }
    } else {
        null // always granted
    }

    val hasGoogleApi = remember {
        GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    ModalNavigationDrawer(
        modifier = Modifier.semantics {
            testTagsAsResourceId = true
        },
        drawerState = drawerState,
        drawerContent = {
            val closeDrawer: () -> Unit = { coroutineScope.launch { drawerState.close() } }
            val debugCloudBackup = if (hasGoogleApi) {
                DebugCloudBackup(
                    fetchBackups = viewModel::fetchBackupList,
                    uploadBackup = viewModel::uploadBackup,
                    deleteBackup = viewModel::deleteBackup,
                    synchronizeBackups = viewModel::synchronizeBackups,
                    getOneSafeFolderUri = {
                        val uri = viewModel.getOneSafeFolderUri()
                        if (uri != null) {
                            context.copyToClipBoard(uri.toString(), LbcTextSpec.Raw("debug"))
                            Toast.makeText(context, "Url copied to clipboard", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Url not found", Toast.LENGTH_SHORT).show()
                        }
                    },
                )
            } else {
                null
            }
            val debugLocalBackup = DebugLocalBackup(
                localBackup = {
                    when (notificationPermissionState?.status) {
                        is PermissionStatus.Denied -> {
                            if (notificationPermissionState.status.shouldShowRationale) {
                                permissionDialogState = NotificationPermissionRationaleDialogState(
                                    launchPermissionRequest = {
                                        notificationPermissionState.launchPermissionRequest()
                                        permissionDialogState = null
                                    },
                                    dismiss = { permissionDialogState = null },
                                )
                            } else {
                                notificationPermissionState.launchPermissionRequest()
                            }
                        }
                        PermissionStatus.Granted,
                        null,
                        -> WorkManager.getInstance(context).startLocalBackupWorker()
                    }
                },
                deleteLocalBackups = viewModel::deleteLocalBackups,
            )
            HelpDebugMenuContent(
                uiState = uiState,
                toggleMaterialYouSetting = viewModel::toggleMaterialYouSetting,
                mainNavController = navController,
                closeDrawer = closeDrawer,
                clearClipboard = viewModel::clearClipboard,
                changePassword = viewModel::changePassword,
                autolock = viewModel::autolock,
                debugLocalBackup = debugLocalBackup,
                debugCloudBackup = debugCloudBackup,
                hasGoogleApi = hasGoogleApi,
                setSetting = { setting ->
                    coroutineScope.launch {
                        drawerState.close()
                        viewModel.setSetting(setting)
                    }
                },
                createContact = viewModel::createContact,
                debugOneTimeActionData = DebugOneTimeActionData(
                    closeDrawer = closeDrawer,
                    resetBackupCta = viewModel::resetAutoBackupEnabled,
                    forceShowSupportOs = viewModel::showSupportOS,
                    resetOSKTutorial = viewModel::resetTutorialOSk,
                    resetTips = viewModel::resetTips,
                    resetOSKOnboarding = viewModel::resetOnboardingOSk,
                    resetCameraTips = viewModel::resetCameraTips,
                ),
                debugSafeItemData = DebugSafeItemData(
                    createRecursiveItem = viewModel::createRecursiveItem,
                    removeAllItems = viewModel::removeAllItems,
                    corruptFile = { viewModel.corruptFile() },
                ),
                toggleLoading = viewModel::toggleLoading,
                debugAutoBackup = DebugAutoBackup(
                    storeAutoBackupError = viewModel::errorAutoBackup,
                    autoBackupError = uiState.autoBackupError,
                    cancelAutoBackup = viewModel::cancelAutoBackup,
                ),
                onWipeDatabaseKey = viewModel::wipeDatabaseKey,
            )
        },
        content = content,
    )
}

@Suppress("LongMethod")
@SuppressLint("NonCoreUiComposable")
@Composable
private fun HelpDebugMenuContent(
    uiState: HelpDebugUiState,
    toggleMaterialYouSetting: () -> Unit,
    mainNavController: NavController,
    closeDrawer: () -> Unit,
    clearClipboard: () -> Unit,
    changePassword: () -> Unit,
    autolock: () -> Unit,
    debugLocalBackup: DebugLocalBackup,
    debugCloudBackup: DebugCloudBackup?,
    hasGoogleApi: Boolean,
    setSetting: (Any) -> Unit,
    createContact: () -> Unit,
    debugOneTimeActionData: DebugOneTimeActionData,
    debugSafeItemData: DebugSafeItemData,
    toggleLoading: () -> Unit,
    debugAutoBackup: DebugAutoBackup,
    onWipeDatabaseKey: () -> Unit,
) {
    val context = LocalContext.current
    val mainBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val loadingManager = rememberLoadingManager()

    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        val defaultModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = OSDimens.SystemSpacing.Regular, vertical = OSDimens.SystemSpacing.Regular / 2)

        val scope = rememberCoroutineScope()

        Spacer(
            modifier = Modifier.statusBarsPadding(),
        )

        mainBackStackEntry?.let { entry ->
            DbgNavRoute(
                title = "Main",
                entry = entry,
                modifier = defaultModifier,
            )
        }

        val colorScheme = MaterialTheme.colorScheme
        val palette = remember(colorScheme) {
            getPalette(colorScheme)
        }
        LazyRow {
            items(
                items = palette,
                key = { it.first },
            ) { (name, color) ->
                BoxWithColorHex(
                    color = color,
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(all = 8.dp),
                    name = LbcTextSpec.Raw(name),
                )
            }
        }

        AnimatedListTestItem("ℹ️ Global info", defaultModifier) {
            GlobalInfo(defaultModifier, hasGoogleApi, uiState.mainDatabaseSize)
        }

        AnimatedListTestItem("⚙️ Settings", defaultModifier) {
            SettingsDebugMenu(
                modifier = defaultModifier,
                itemOrder = uiState.itemOrder,
                itemsLayoutSetting = uiState.itemsLayoutSetting,
                cameraSystem = uiState.cameraSystem,
                setSetting = setSetting,
                databaseEncryptionSettings = uiState.databaseEncryptionSettings,
            )
        }

        AnimatedListTestItem("💾 Backups", defaultModifier) {
            debugAutoBackup.Composable(modifier = defaultModifier)
            debugLocalBackup.Composable(modifier = defaultModifier)
            debugCloudBackup?.Composable(modifier = defaultModifier)
        }

        if (DynamicColors.isDynamicColorAvailable()) {
            Row(
                modifier = defaultModifier.clickable { toggleMaterialYouSetting() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OSText(LbcTextSpec.Raw("Material You 🎨"))
                Switch(checked = uiState.isMaterialYouEnabled, onCheckedChange = { toggleMaterialYouSetting() })
            }
        }

        Button(
            onClick = {
                context.findFragmentActivity().recreate()
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Config change"))
        }

        DebugOneTimeAction(
            modifier = defaultModifier,
            data = debugOneTimeActionData,
        )

        DebugSafeItem(
            modifier = defaultModifier,
            data = debugSafeItemData,
        )

        Button(
            onClick = {
                clearClipboard()
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Clear clipboard"))
        }

        Button(
            onClick = changePassword,
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Change password"))
        }

        Button(
            onClick = {
                scope.launch {
                    closeDrawer()
                    delay(5.seconds)
                    autolock()
                }
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Autolock after 5sec"))
        }

        Button(
            onClick = {
                scope.launch {
                    closeDrawer()
                }
                toggleLoading()
            },
            modifier = defaultModifier,
        ) {
            if (loadingManager.loadingState.value.isBlocking) {
                OSText(LbcTextSpec.Raw("Stop loading"))
            } else {
                OSText(LbcTextSpec.Raw("Start loading"))
            }
        }

        Button(
            onClick = {
                val navLog = buildNavigationDebugString(mainNavController, context)
                logger.d(navLog)
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Print navigation"))
        }

        OSRegularDivider(
            modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
        )

        OSText(
            text = LbcTextSpec
                .Raw("**oneSafe K**:")
                .markdown(),
            modifier = Modifier.padding(horizontal = OSDimens.SystemSpacing.Regular),
        )

        Button(
            onClick = {
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Enqueued message notification"))
        }

        Button(
            onClick = {
                createContact()
                closeDrawer()
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Create dummy contact 🫧"))
        }

        OSRegularDivider(
            modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
        )

        Button(
            onClick = {
                val intent = context.packageManager.getLaunchIntentForPackage("com.lunabee.onesafe.dev")
                context.startActivity(intent!!)
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Launch oS 5 dev"))
        }

        Button(
            onClick = {
                val intent = context.packageManager.getLaunchIntentForPackage("com.lunabee.onesafe")
                context.startActivity(intent!!)
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Launch oS 5"))
        }

        Button(
            onClick = {
                LBLogger.get<HelpDebugViewModel>().v("Verbose log at ${Instant.now()}")
            },
            modifier = defaultModifier,
        ) {
            OSText(LbcTextSpec.Raw("Logger test 💬"))
        }

        Button(
            onClick = onWipeDatabaseKey,
            modifier = defaultModifier,
            colors = ButtonDefaults.textButtonColors(containerColor = Color.Red),
        ) {
            OSText(LbcTextSpec.Raw("🧹 Clear database key"))
        }

        Button(
            onClick = {
                error("Crash at ${Instant.now()}")
            },
            modifier = defaultModifier,
            colors = ButtonDefaults.textButtonColors(containerColor = Color.Red),
        ) {
            OSText(LbcTextSpec.Raw("Crash 💣"))
        }
    }
}

@Composable
private fun GlobalInfo(modifier: Modifier, hasGoogleApi: Boolean, mainDatabaseSize: Long) {
    val languageTag = stringResource(OSString.locale_lang)
    val info: Map<String, Any> = mapOf(
        "device locale" to Locale.current,
        "app locale language" to "$languageTag -> ${Locale(languageTag).language}",
        "Google service" to hasGoogleApi,
        "Main DB size" to LbcTextSpec.Raw(mainDatabaseSize.toString()),
    )
    val infoText = buildString {
        info.forEach { (key, value) ->
            appendLine("\t• $key = $value")
        }
    }.trimIndent()
    OSText(
        text = LbcTextSpec.Raw(infoText),
        modifier = modifier,
    )
}

@SuppressLint("RestrictedApi")
private fun buildNavigationDebugString(
    mainNavController: NavController,
    context: Context,
): String {
    val navLog = StringBuilder()
    fun formatEntry(entry: NavBackStackEntry) {
        navLog.appendLine("\t• ${entry.resolveArgsToString()} [${entry.lifecycle.currentState}] [${entry.destination.id}]")
    }
    navLog.appendLine("Main nav")
    mainNavController.currentBackStack.value.forEach(::formatEntry)
    val onBackPressedDispatcher = context.findFragmentActivity().onBackPressedDispatcher
    val field = onBackPressedDispatcher::class.declaredMemberProperties.find {
        it.name == "onBackPressedCallbacks"
    }.apply { this?.isAccessible = true }

    @Suppress("UNCHECKED_CAST")
    val callbacks: ArrayDeque<OnBackPressedCallback> = field?.getter?.call(onBackPressedDispatcher) as ArrayDeque<OnBackPressedCallback>
    navLog.appendLine()
    navLog.appendLine("Back dispatchers")
    callbacks.forEach { callback ->
        val associatedGraph = (
            callback::class.java.declaredFields.first()
                ?.apply { isAccessible = true }
                ?.get(callback) as? NavController
            )?.graph?.route
        navLog.append("\t• $callback")
        if (associatedGraph != null) {
            navLog.append(", graph = $associatedGraph)")
        }
        navLog.append(", enabled = ${callback.isEnabled}")
        navLog.appendLine()
    }
    return navLog.toString()
}

@Composable
fun AnimatedListTestItem(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isShowing: Boolean by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable {
                isShowing = !isShowing
            }
            .then(modifier)
            .padding(vertical = OSDimens.SystemSpacing.Small),
    ) {
        OSText(
            modifier = Modifier
                .weight(1f),
            text = LbcTextSpec.Raw("**$text**").markdown(),
        )

        val angle by animateFloatAsState(
            targetValue = if (isShowing) 180f else 0f,
            label = "rotate $text icon",
        )
        OSText(
            text = LbcTextSpec.Raw("🔼"),
            modifier = Modifier.rotate(angle),
        )
    }
    AnimatedVisibility(
        visible = isShowing,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom),
    ) {
        Column(
            modifier = Modifier
                .clickable {
                    isShowing = !isShowing
                }
                .fillMaxWidth()
                .padding(horizontal = OSDimens.SystemSpacing.Regular),
            content = content,
        )
    }
}

private fun getPalette(colorScheme: ColorScheme): List<Pair<String, Color>> {
    val colorMap = mutableListOf<Pair<String, Color>>()
    ColorScheme::class.java.declaredFields.mapNotNullTo(colorMap) { field ->
        field.isAccessible = true
        val color = runCatching { Color((field.get(colorScheme) as Long).toULong()) }.getOrNull()
        val colorName = field.name.substringBefore('$')
        color?.let { colorName to color }
    }
    val primary = colorMap.firstOrNull { it.first.equals("primary", ignoreCase = true) }
    val secondary = colorMap.firstOrNull { it.first.equals("secondary", ignoreCase = true) }
    colorMap.remove(primary)
    colorMap.remove(secondary)
    return colorMap
}

@Preview
@Composable
private fun RootDrawerPreview() {
    OSPreviewOnSurfaceTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(),
        ) {
            HelpDebugMenuContent(
                uiState = HelpDebugUiState.default,
                toggleMaterialYouSetting = {},
                mainNavController = rememberNavController(),
                closeDrawer = {},
                clearClipboard = {},
                changePassword = { LBResult.Success("") },
                autolock = {},
                debugLocalBackup = DebugLocalBackup({}, {}),
                debugCloudBackup = DebugCloudBackup({}, {}, {}, {}, {}),
                hasGoogleApi = true,
                setSetting = {},
                createContact = {},
                debugOneTimeActionData = DebugOneTimeActionData({}, {}, {}, {}, {}, {}, {}),
                debugSafeItemData = DebugSafeItemData({}, {}, {}),
                toggleLoading = {},
                debugAutoBackup = DebugAutoBackup({}, null, {}),
                onWipeDatabaseKey = {},
            )
        }
    }
}
