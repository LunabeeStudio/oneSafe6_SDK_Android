package studio.lunabee.onesafe.feature.settings.bubbles

import android.Manifest
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.common.utils.observeIsOSKImeEnabledAsStateWithLifecycle
import studio.lunabee.onesafe.common.utils.observeIsOSKImeSelectedAsState
import studio.lunabee.onesafe.common.utils.rememberOSKImeSettings
import studio.lunabee.onesafe.common.utils.settings.UiNotificationHelper
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.AutoLockBackgroundDelay
import studio.lunabee.onesafe.commonui.settings.AutoLockInactivityDelay
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.SwitchSettingAction
import studio.lunabee.onesafe.ime.OSFlorisImeService
import studio.lunabee.onesafe.ime.ui.settings.AutoLockOSKHiddenDelayBottomSheet
import studio.lunabee.onesafe.ime.ui.settings.AutoLockOSKInactivityDelayBottomSheet
import studio.lunabee.onesafe.ime.ui.settings.CardSettingsActionAutoLockOSKHiddenAction
import studio.lunabee.onesafe.ime.ui.settings.CardSettingsActionAutoLockOSKInactivityAction
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun ExtensionOneSafeKConfigureCard(
    onSelectAutoLockInactivityDelay: (delay: AutoLockInactivityDelay) -> Unit,
    onSelectAutoLockHiddenDelay: (delay: AutoLockBackgroundDelay) -> Unit,
    inactivityDelay: AutoLockInactivityDelay,
    hiddenDelay: AutoLockBackgroundDelay,
    featureFlagFlorisBoard: Boolean,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val oskImeSettings = rememberOSKImeSettings { oSKEnabled ->
        if (oSKEnabled) {
            coroutineScope.launch {
                delay(DelayForKeyboardSelection)
                context.getSystemService(InputMethodManager::class.java).showInputMethodPicker()
            }
        }
    }
    val isOSKeyboardEnabled by observeIsOSKImeEnabledAsStateWithLifecycle()
    val isOSKeyboardSelected by observeIsOSKImeSelectedAsState(foregroundOnly = true)
    val areNotificationsEnabled by UiNotificationHelper.areNotificationsEnabled()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* no-op */ }

    var isAutoLockInactivityDelayBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }
    var isAutoLockHiddenDelayBottomSheetVisible by rememberSaveable { mutableStateOf(value = false) }

    AutoLockOSKInactivityDelayBottomSheet(
        isVisible = isAutoLockInactivityDelayBottomSheetVisible,
        onSelect = { delay ->
            onSelectAutoLockInactivityDelay(delay)
            if (featureFlagFlorisBoard) {
                killOSImeService(context)
            }
        },
        onBottomSheetClosed = { isAutoLockInactivityDelayBottomSheetVisible = false },
        selectedAutoLockInactivityDelay = inactivityDelay,
    )

    AutoLockOSKHiddenDelayBottomSheet(
        isVisible = isAutoLockHiddenDelayBottomSheetVisible,
        onSelect = { delay ->
            onSelectAutoLockHiddenDelay(delay)
            if (featureFlagFlorisBoard) {
                killOSImeService(context)
            }
        },
        onBottomSheetClosed = { isAutoLockHiddenDelayBottomSheetVisible = false },
        selectedAutoLockAppChangeDelay = hiddenDelay,
    )

    ExtensionOneSafeKCardContent(
        isKeyboardSelected = isOSKeyboardSelected,
        areNotificationsEnabled = areNotificationsEnabled,
        toggleKeyboardSelected = {
            if (isOSKeyboardEnabled) {
                context.getSystemService(InputMethodManager::class.java).showInputMethodPicker()
            } else {
                oskImeSettings.requestEnableOSKIme()
            }
        },
        toggleNotification = {
            if (!areNotificationsEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                context.startActivity(UiNotificationHelper.getSettingIntent(context))
            }
        },
        onAutoLockInactivityDelayActionClick = { isAutoLockInactivityDelayBottomSheetVisible = true },
        onAutoLockHiddenDelayActionClick = { isAutoLockHiddenDelayBottomSheetVisible = true },
        inactivityDelay = inactivityDelay,
        hiddenDelay = hiddenDelay,
    )
}

@Composable
private fun ExtensionOneSafeKCardContent(
    isKeyboardSelected: Boolean,
    toggleKeyboardSelected: () -> Unit,
    areNotificationsEnabled: Boolean,
    toggleNotification: () -> Unit,
    onAutoLockInactivityDelayActionClick: () -> Unit,
    onAutoLockHiddenDelayActionClick: () -> Unit,
    inactivityDelay: AutoLockInactivityDelay,
    hiddenDelay: AutoLockBackgroundDelay,
) {
    SettingsCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(UiConstants.TestTag.Item.OneSafeKConfigurationCard),
        title = LbcTextSpec.StringResource(OSString.oneSafeK_extension_title),
        actions = listOf(
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.oneSafeK_extension_configuration_keyboardSelected),
                isChecked = isKeyboardSelected,
                onValueChange = { toggleKeyboardSelected() },
            ),
            SwitchSettingAction(
                label = LbcTextSpec.StringResource(OSString.oneSafeK_extension_configuration_notificationEnabled),
                isChecked = areNotificationsEnabled,
                onValueChange = { toggleNotification() },
            ),
            CardSettingsActionAutoLockOSKInactivityAction(
                delay = inactivityDelay,
                onClick = onAutoLockInactivityDelayActionClick,
            ),
            CardSettingsActionAutoLockOSKHiddenAction(
                delay = hiddenDelay,
                onClick = onAutoLockHiddenDelayActionClick,
            ),
        ),
        footer = LbcTextSpec.StringResource(OSString.oneSafeK_extension_startOnBoarding_footer),
    )
}

private fun killOSImeService(context: Context) {
    // FIXME Kill the ime process to make it restart and get new datastore values
    //  Datastore 1.1.0 (alpha) support multiprocessing but got some trouble to use import it
    //  https://issuetracker.google.com/issues/297914986
    OSFlorisImeService.kill(context)
}

@Composable
@OsDefaultPreview
fun ExtensionOneSafeKConfigureCardPreview() {
    OSPreviewBackgroundTheme {
        ExtensionOneSafeKCardContent(
            isKeyboardSelected = false,
            toggleKeyboardSelected = {},
            areNotificationsEnabled = true,
            toggleNotification = {},
            onAutoLockInactivityDelayActionClick = {},
            onAutoLockHiddenDelayActionClick = {},
            inactivityDelay = AutoLockInactivityDelay.ONE_MINUTE,
            hiddenDelay = AutoLockBackgroundDelay.IMMEDIATELY,
        )
    }
}

private const val DelayForKeyboardSelection: Long = 300
