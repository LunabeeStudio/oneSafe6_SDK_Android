package studio.lunabee.onesafe.utils

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Day mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Night mode")
annotation class OsDefaultPreview
