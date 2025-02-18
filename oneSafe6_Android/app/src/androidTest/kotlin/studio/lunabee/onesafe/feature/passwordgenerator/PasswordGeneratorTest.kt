package studio.lunabee.onesafe.feature.passwordgenerator

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.dp
import io.mockk.spyk
import io.mockk.verify
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.onesafe.commonui.OSString
import studio.lunabee.onesafe.domain.model.password.GeneratedPassword
import studio.lunabee.onesafe.domain.model.password.PasswordConfig
import studio.lunabee.onesafe.domain.model.password.PasswordStrength
import studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator.PasswordGeneratorLayout
import studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator.PasswordGeneratorUiState
import studio.lunabee.onesafe.ui.UiConstants
import studio.lunabee.onesafe.ui.theme.OSTheme

@OptIn(ExperimentalTestApi::class)
class PasswordGeneratorTest : LbcComposeTest() {
    private val cancel: () -> Unit = spyk({ })
    private val confirm: (String) -> Unit = spyk({ })
    private val generateNewPassword: (PasswordGeneratorUiState.Data) -> Unit = spyk({ })

    @Test
    fun password_generator_button_test() {
        setLayoutContent(
            state = PasswordGeneratorUiState.Data(PasswordConfig.default()),
            generatedPassword = GeneratedPassword("", PasswordStrength.VeryStrong),
        ) {
            onNodeWithText(getString(OSString.common_cancel)).performClick()
            verify(exactly = 1) { cancel.invoke() }
            onNodeWithText(getString(OSString.common_confirm)).performClick()
            verify(exactly = 1) { confirm.invoke("") }
            onNodeWithText(getString(OSString.passwordStrength_veryStrong)).assertIsDisplayed()
        }
    }

    @Test
    fun password_generation_length_change_test() {
        val state = PasswordGeneratorUiState.Data(PasswordConfig.default())
        setLayoutContent(
            state = state,
        ) {
            onNodeWithTag(UiConstants.TestTag.Item.Slider).performTouchInput {
                swipeLeft()
            }
            verify(atLeast = 1) { generateNewPassword(state) } // called on every slider step
        }
    }

    @Test
    fun password_generation_upper_case_change_test() {
        val state = PasswordGeneratorUiState.Data(PasswordConfig.default())
        setLayoutContent(state = state) {
            onNodeWithText(getString(OSString.passwordGenerator_accessibility_criteria_uppercase))
                .performClick()
            verify(exactly = 1) { generateNewPassword(state) }
        }
    }

    @Test
    fun password_generation_lower_case_change_test() {
        val state = PasswordGeneratorUiState.Data(PasswordConfig.default())
        setLayoutContent(state = state) {
            onNodeWithText(getString(OSString.passwordGenerator_accessibility_criteria_lowercase))
                .performClick()
            verify(exactly = 1) { generateNewPassword(state) }
        }
    }

    @Test
    fun test_password_generation_number_case_change() {
        val state = PasswordGeneratorUiState.Data(PasswordConfig.default())
        setLayoutContent(state = state) {
            onNodeWithText(getString(OSString.passwordGenerator_accessibility_criteria_number))
                .performClick()
            verify(exactly = 1) { generateNewPassword(state) }
        }
    }

    @Test
    fun test_password_generation_symbol_case_change() {
        val state = PasswordGeneratorUiState.Data(PasswordConfig.default())
        setLayoutContent(state = state) {
            onNodeWithText(getString(OSString.passwordGenerator_accessibility_criteria_symbol))
                .performClick()
            verify(exactly = 1) { generateNewPassword(state) }
        }
    }

    @Test
    fun test_strength_label_display() {
        val state = PasswordGeneratorUiState.Data(PasswordConfig.default())
        setLayoutContent(state = state, generatedPassword = GeneratedPassword("", PasswordStrength.VeryWeak)) {
            onNodeWithTag(UiConstants.TestTag.Item.PasswordStrengthText).assertIsDisplayed()
        }
        setLayoutContent(state = state, generatedPassword = GeneratedPassword("", PasswordStrength.Unknown)) {
            onNodeWithTag(UiConstants.TestTag.Item.PasswordStrengthText).assertDoesNotExist()
        }
    }

    private fun setLayoutContent(
        generatedPassword: GeneratedPassword = GeneratedPassword.default(),
        state: PasswordGeneratorUiState.Data,
        block: ComposeUiTest.() -> Unit,
    ) {
        invoke {
            setContent {
                OSTheme {
                    PasswordGeneratorLayout(
                        paddingValues = PaddingValues(0.dp),
                        password = generatedPassword,
                        generateNewPassword = generateNewPassword,
                        onConfirm = { confirm("") },
                        onCancel = cancel,
                        passwordGeneratorUiState = state,
                    )
                }
            }
            block()
        }
    }
}
