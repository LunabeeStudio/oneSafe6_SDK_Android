package studio.lunabee.onesafe.commonui.beta

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import kotlin.test.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.ui.UiConstants

class AppBetaVersionChipTest : LbcComposeTest() {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun show_bottomSheet_test() {
        invoke {
            setContent {
                AppBetaVersionChip()
            }

            hasText(getString(OSString.appBetaVersion_chip))
                .waitUntilExactlyOneExists()
                .performClick()
            hasTestTag(UiConstants.TestTag.BottomSheet.AppBetaVersionBottomSheet)
                .waitUntilExactlyOneExists()
        }
    }
}
