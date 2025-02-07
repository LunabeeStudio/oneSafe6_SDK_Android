package studio.lunabee.onesafe.feature.settings.personalization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.color.DynamicColors
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.feature.settings.settingcard.impl.AutomationSettingCard
import studio.lunabee.onesafe.feature.settings.settingcard.impl.MaterialYouSettingCard
import studio.lunabee.onesafe.molecule.ElevatedTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.extensions.topAppBarElevation
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewOnSurfaceTheme
import studio.lunabee.onesafe.utils.OsDefaultPreview

@Composable
fun PersonalizationSettingsRoute(
    navigateBack: () -> Unit,
    viewModel: PersonalizationSettingsViewModel = hiltViewModel(),
) {
    val isMaterialYouEnabled by viewModel.isMaterialYouEnabled.collectAsStateWithLifecycle()
    val isAutomationEnabled by viewModel.isAutomationEnabled.collectAsStateWithLifecycle()

    PersonalizationSettingsScreen(
        navigateBack = navigateBack,
        isMaterialYouEnabled = isMaterialYouEnabled,
        isAutomationEnabled = isAutomationEnabled,
        toggleMaterialYou = viewModel::toggleMaterialYouSetting,
        toggleAutomation = viewModel::toggleAutomationSetting,
    )
}

@Composable
fun PersonalizationSettingsScreen(
    navigateBack: () -> Unit,
    isMaterialYouEnabled: Boolean,
    isAutomationEnabled: Boolean,
    toggleMaterialYou: () -> Unit,
    toggleAutomation: () -> Unit,
) {
    val lazyListState: LazyListState = rememberLazyListState()
    OSScreen(
        testTag = UiConstants.TestTag.Screen.Settings,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(top = OSDimens.ItemTopBar.Height)
                .fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = OSDimens.SystemSpacing.Regular,
                vertical = OSDimens.SystemSpacing.ExtraLarge,
            ),
            state = lazyListState,
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            item(
                key = AutomationSectionKey,
            ) {
                AutomationSettingCard(
                    modifier = Modifier.fillMaxWidth(),
                    isAutomationEnabled = isAutomationEnabled,
                    toggleAutomation = toggleAutomation,
                )
            }

            if (DynamicColors.isDynamicColorAvailable()) {
                item(
                    key = ThemeSectionKey,
                ) {
                    MaterialYouSettingCard(
                        modifier = Modifier.fillMaxWidth(),
                        isMaterialYouEnabled = isMaterialYouEnabled,
                        toggleMaterialYou = toggleMaterialYou,
                    )
                }
            }
        }
        ElevatedTopAppBar(
            title = LbcTextSpec.StringResource(OSString.settings_personalization_title),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            elevation = lazyListState.topAppBarElevation,
        )
    }
}

@Composable
@OsDefaultPreview
private fun SecuritySettingPreview() {
    OSPreviewOnSurfaceTheme {
        PersonalizationSettingsScreen(
            navigateBack = { },
            isMaterialYouEnabled = false,
            toggleMaterialYou = {},
            isAutomationEnabled = true,
            toggleAutomation = {},
        )
    }
}

private const val AutomationSectionKey: String = "AutomationSectionKey"
private const val ThemeSectionKey: String = "ThemeSectionKey"
