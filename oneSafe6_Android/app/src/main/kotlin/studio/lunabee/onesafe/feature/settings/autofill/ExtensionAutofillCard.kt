package studio.lunabee.onesafe.feature.settings.autofill

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.commonui.settings.SettingsCard
import studio.lunabee.onesafe.commonui.settings.TextSettingsAction
import studio.lunabee.onesafe.feature.settings.settingcard.action.CardSettingsActionEnableAutofillAction
import studio.lunabee.onesafe.ui.res.OSDimens
import studio.lunabee.onesafe.ui.theme.OSPreviewBackgroundTheme

@SuppressLint("WrongConstant")
@Composable
@RequiresApi(Build.VERSION_CODES.O)
fun ExtensionAutofillCard() {
    val context = LocalContext.current
    val autofillManager: AutofillManager =
        remember { context.getSystemService(AutofillManagerService) as AutofillManager }
    var isAutofillEnableForOS: Boolean by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isAutofillEnableForOS = autofillManager.hasEnabledAutofillServices()
    }

    val enableAutoFillLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            if (it.resultCode == Activity.RESULT_OK) {
                isAutofillEnableForOS = autofillManager.hasEnabledAutofillServices()
            }
        },
    )
    SettingsCard(
        actions = if (isAutofillEnableForOS) {
            listOf(TextSettingsAction.AutofillEnableLabel())
        } else {
            listOf(
                CardSettingsActionEnableAutofillAction(
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE)
                        intent.data = Uri.parse("package:${context.packageName}")
                        enableAutoFillLauncher.launch(intent)
                    },
                ),
            )
        },
        footer = LbcTextSpec.StringResource(OSString.autofill_settings_info),
    )
}

@Composable
@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
fun ExtensionAutofillCardPreview() {
    OSPreviewBackgroundTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(OSDimens.SystemSpacing.Regular),
            modifier = Modifier.padding(OSDimens.SystemSpacing.Regular),
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ExtensionAutofillCard()
            }
        }
    }
}

// Cannot fetch the value directly from Context (unresolved reference)
private const val AutofillManagerService: String = "autofill"
