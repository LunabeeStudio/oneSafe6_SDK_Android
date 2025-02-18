package studio.lunabee.onesafe.feature.onboarding

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.commonui.error.codeText
import studio.lunabee.onesafe.commonui.error.description
import studio.lunabee.onesafe.error.OSError
import studio.lunabee.onesafe.feature.password.confirmation.PasswordConfirmationScreen
import studio.lunabee.onesafe.feature.password.confirmation.PasswordConfirmationScreenLabels
import studio.lunabee.onesafe.ui.theme.OSTheme

class PasswordConfirmationScreenTest : LbcComposeTest() {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun error_field_test() {
        mockkStatic(OSError::codeText)
        val errMessage = "error message"
        val error = mockk<OSError> {
            every { description() } returns LbcTextSpec.Raw(errMessage)
        }

        invoke {
            setContent {
                OSTheme {
                    PasswordConfirmationScreen(
                        labels = PasswordConfirmationScreenLabels.ChangePassword,
                        navigateBack = {},
                        confirmClick = {},
                        isConfirmEnabled = false,
                        passwordValue = "",
                        onValueChange = {},
                        isLoading = false,
                        fieldError = error,
                    )
                }
            }

            onNodeWithText(errMessage).assertIsDisplayed()
        }
    }
}
