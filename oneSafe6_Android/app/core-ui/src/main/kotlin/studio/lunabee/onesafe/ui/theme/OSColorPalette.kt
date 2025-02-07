package studio.lunabee.onesafe.ui.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

val LocalColorPalette: ProvidableCompositionLocal<OSColorPalette> = compositionLocalOf { MainOSColorPalette }

@Suppress("VariableNaming")
interface OSColorPalette {
    val Primary01: Color
    val Primary03: Color
    val Primary05: Color
    val Primary10: Color
    val Primary20: Color
    val Primary30: Color
    val Primary40: Color
    val Primary60: Color
    val Primary75: Color
    val Primary85: Color
    val Primary95: Color

    val Neutral00: Color
    val Neutral10: Color
    val Neutral20: Color
    val Neutral30: Color
    val Neutral60: Color
    val Neutral70: Color
    val Neutral80: Color
    val Neutral90: Color
    val Neutral100: Color

    val Alert05: Color
    val Alert20: Color
    val Alert35: Color
    val Alert80: Color

    val Success20: Color
    val Success40: Color

    val Border: Color

    val Recording: Color

    val FeedbackNew10: Color
    val FeedbackProgress10: Color
    val FeedbackWarningStart: Color
    val FeedbackWarningEnd: Color

    val Warning02: Color
    val Warning05: Color
    val Warning90: Color
    val Warning95: Color

    val Error30: Color
}
