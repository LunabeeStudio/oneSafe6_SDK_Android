package studio.lunabee.onesafe.feature.keyboard.screen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.extension.markdown
import studio.lunabee.onesafe.common.utils.settings.UiNotificationHelper
import studio.lunabee.onesafe.commonui.OSDrawable
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationAction
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationActionAttributes
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationStep
import studio.lunabee.onesafe.feature.onboarding.presentation.PresentationStepLayout
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun KeyboardNotificationRoute(
    navigateBack: () -> Unit,
    onDone: () -> Unit,
) {
    val areNotificationsEnabled by UiNotificationHelper.areNotificationsEnabled()
    LaunchedEffect(areNotificationsEnabled) {
        if (areNotificationsEnabled) {
            onDone()
        }
    }

    // If the screen is displayed then it mean that the notifications are not enabled
    KeyboardNotificationScreen(
        navigateBack = navigateBack,
        onDone = onDone,
    )
}

@Composable
fun KeyboardNotificationScreen(
    navigateBack: () -> Unit,
    onDone: () -> Unit,
) {
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* no-op */ }

    val context = LocalContext.current

    OSScreen(
        testTag = UiConstants.TestTag.Screen.OneSafeKAccessibilityScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .padding(
                    top = OSDimens.ItemTopBar.Height,
                    bottom = OSDimens.SystemSpacing.Huge,
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            PresentationStepLayout(
                presentationStep = PresentationStep(
                    title = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_notification_title),
                    description = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_notification_description).markdown(),
                    imageRes = OSDrawable.illustration_controls,
                    actions = listOf(
                        PresentationAction(
                            label = LbcTextSpec.StringResource(OSString.oneSafeK_onboarding_notification_ctaTitle),
                            action = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    context.startActivity(UiNotificationHelper.getSettingIntent(context))
                                }
                            },
                        ),
                        PresentationAction(
                            label = LbcTextSpec.StringResource(OSString.common_skip),
                            action = onDone,
                            attributes = PresentationActionAttributes().notFilled(),
                        ),
                    ),
                ),
            )
        }
        OSTopAppBar(
            title = LbcTextSpec.Raw(""),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
        )
    }
}

@Composable
@OsDefaultPreview
fun KeyboardNotificationScreenPreview() {
    OSPreviewBackgroundTheme {
        KeyboardNotificationScreen(
            navigateBack = {},
            onDone = {},
        )
    }
}
