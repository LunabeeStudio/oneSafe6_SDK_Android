package studio.lunabee.onesafe.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import kotlin.test.Test
import studio.lunabee.compose.androidtest.LbcComposeTest
import kotlin.test.assertEquals

class TouchInterceptorBoxTest : LbcComposeTest() {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun intercept_touch_test(): Unit = invoke {
        var hasClick = 0
        var hasInterceptTouch = 0

        setContent {
            CompositionLocalProvider(
                LocalOnTouchWindow.provides {
                    hasInterceptTouch++
                },
            ) {
                TouchInterceptorBox {
                    Box(Modifier.fillMaxSize()) {
                        Button(
                            modifier = Modifier.testTag("button"),
                            onClick = {
                                hasClick++
                            },
                        ) {
                        }
                    }
                }
            }
        }

        onNodeWithTag("button").performClick()
        assertEquals(1, hasClick)
        assertEquals(2, hasInterceptTouch) // click = down + up

        onRoot().performTouchInput {
            this.down(Offset(10f, 10f))
        }

        assertEquals(1, hasClick)
        assertEquals(3, hasInterceptTouch)
    }
}
