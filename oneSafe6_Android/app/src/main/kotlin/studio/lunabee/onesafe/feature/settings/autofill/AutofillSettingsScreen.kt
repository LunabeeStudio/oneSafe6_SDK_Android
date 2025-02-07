package studio.lunabee.onesafe.feature.settings.autofill

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.atom.OSScreen
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.action.topAppBarOptionNavBack
import studio.lunabee.onesafe.molecule.OSTopAppBar
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.res.OSDimens

@Composable
fun AutofillSettingsRoute(
    navigateBack: () -> Unit,
) {
    AutofillSettingsScreen(
        navigateBack = navigateBack,
    )
}

@Composable
fun AutofillSettingsScreen(
    navigateBack: () -> Unit,
) {
    OSScreen(
        testTag = UiConstants.TestTag.Screen.ExtensionSettingsScreen,
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom))
            .fillMaxSize(),
    ) {
        OSTopAppBar(
            modifier = Modifier
                .statusBarsPadding()
                .align(Alignment.TopCenter),
            options = listOf(topAppBarOptionNavBack(navigateBack)),
            title = LbcTextSpec.StringResource(OSString.extension_autofillCard_title),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = OSDimens.ItemTopBar.Height + OSDimens.SystemSpacing.Large,
                    start = OSDimens.SystemSpacing.Regular,
                    end = OSDimens.SystemSpacing.Regular,
                ),
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ExtensionAutofillCard()
            }
        }
    }
}
