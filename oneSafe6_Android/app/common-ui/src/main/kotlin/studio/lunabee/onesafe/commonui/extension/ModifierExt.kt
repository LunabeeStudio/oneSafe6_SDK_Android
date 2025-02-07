package studio.lunabee.onesafe.commonui.extension

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.unit.LayoutDirection
import studio.lunabee.onesafe.ui.UiConstants

fun Modifier.disableCanvas(): Modifier =
    drawWithContent {
        drawIntoCanvas { composeCanvas ->
            val greyscaleMatrix = ColorMatrix(
                floatArrayOf(
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0.33f, 0.33f, 0.33f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f,
                ),
            )
            val paint = Paint()
            paint.colorFilter = ColorFilter.colorMatrix(greyscaleMatrix)
            composeCanvas.withSaveLayer(size.toRect(), paint) {
                drawContent()
            }
        }
    }.alpha(UiConstants.Alpha.Disable)

fun Modifier.rtl(layoutDirection: LayoutDirection): Modifier {
    return when (layoutDirection) {
        LayoutDirection.Rtl -> scale(-1f, 1f)
        else -> this
    }
}
