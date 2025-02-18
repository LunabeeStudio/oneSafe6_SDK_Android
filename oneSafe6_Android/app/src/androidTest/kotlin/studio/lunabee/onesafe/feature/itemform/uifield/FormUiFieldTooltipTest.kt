package studio.lunabee.onesafe.feature.itemform.uifield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import studio.lunabee.compose.androidtest.extension.waitUntilDoesNotExist
import studio.lunabee.compose.androidtest.extension.waitUntilExactlyOneExists
import studio.lunabee.compose.core.LbcTextSpec
import studio.lunabee.onesafe.feature.itemform.model.uifield.FormUiFieldTooltip
import studio.lunabee.onesafe.feature.itemform.model.uifield.TipsUiField
import studio.lunabee.onesafe.tooltip.OSTooltipAction
import studio.lunabee.onesafe.tooltip.OSTooltipContent
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class FormUiFieldTooltipTest : LbcComposeTest() {
    @Test
    fun dismiss_action_test() {
        var hasDismissed = false
        val dismiss = "dismiss"
        val tipsUiField = TipsUiField(
            tooltipContent = OSTooltipContent(
                title = LbcTextSpec.Raw("Title"),
                description = LbcTextSpec.Raw("Description"),
                actions = listOf(OSTooltipAction(text = LbcTextSpec.Raw(dismiss), onClick = {})),
            ),
            onDismiss = {
                hasDismissed = true
            },
        )

        invoke {
            setTooltipsContent(tipsUiField)
            hasText(dismiss)
                .waitUntilExactlyOneExists()
                .performClick()
            hasText(dismiss)
                .waitUntilDoesNotExist()
            assertTrue(hasDismissed)
        }
    }

    @Test
    fun dismiss_outside_test() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        var hasDismissed = false
        val dismiss = "dismiss"
        val tipsUiField = TipsUiField(
            tooltipContent = OSTooltipContent(
                title = LbcTextSpec.Raw("Title"),
                description = LbcTextSpec.Raw("Description"),
                actions = listOf(OSTooltipAction(text = LbcTextSpec.Raw(dismiss), onClick = {})),
            ),
            onDismiss = {
                hasDismissed = true
            },
        )

        invoke {
            setTooltipsContent(tipsUiField)
            hasText(dismiss)
                .waitUntilExactlyOneExists()
            device.click(10, device.displayHeight / 2)
            hasText(dismiss)
                .waitUntilDoesNotExist()
            assertTrue(hasDismissed)
        }
    }

    private fun ComposeUiTest.setTooltipsContent(tipsUiField: TipsUiField) {
        setContent {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red),
            ) {
                FormUiFieldTooltip(
                    tipsUiField = tipsUiField,
                ) {
                    Box(
                        Modifier
                            .size(100.dp)
                            .background(Color.Green),
                    )
                }
            }
        }
    }
}
