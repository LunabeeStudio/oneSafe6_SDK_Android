package studio.lunabee.onesafe.ui.theme

import androidx.compose.ui.graphics.Color
import studio.lunabee.onesafe.ui.res.OSColorValue

/**
 * OneSafe colors are available at https://www.figma.com/file/4BXpDBVU9mT11uquBLvvnj/%F0%9F%A6%AF-Design-System?node-id=0%3A1
 */
object MainOSColorPalette : OSColorPalette {
    override val Primary01: Color = OSColorValue.Purple01
    override val Primary03: Color = OSColorValue.Purple03
    override val Primary05: Color = OSColorValue.Purple05
    override val Primary10: Color = OSColorValue.Purple10
    override val Primary20: Color = OSColorValue.Purple20
    override val Primary30: Color = OSColorValue.Purple30
    override val Primary40: Color = OSColorValue.Purple40
    override val Primary60: Color = OSColorValue.Purple60
    override val Primary75: Color = OSColorValue.Purple75
    override val Primary85: Color = OSColorValue.Purple85
    override val Primary95: Color = OSColorValue.Purple95

    override val Neutral00: Color = OSColorValue.White
    override val Neutral10: Color = OSColorValue.Gray10
    override val Neutral20: Color = OSColorValue.Gray20
    override val Neutral30: Color = OSColorValue.Gray30
    override val Neutral60: Color = OSColorValue.Gray60
    override val Neutral70: Color = OSColorValue.Gray70
    override val Neutral80: Color = OSColorValue.Gray80
    override val Neutral90: Color = OSColorValue.Gray90
    override val Neutral100: Color = OSColorValue.Black

    override val Alert05: Color = OSColorValue.Red05
    override val Alert20: Color = OSColorValue.Red20
    override val Alert35: Color = OSColorValue.Red35
    override val Alert80: Color = OSColorValue.Red80

    override val Success20: Color = OSColorValue.Green20
    override val Success40: Color = OSColorValue.Green40

    override val Border: Color = OSColorValue.Black05
    override val Recording: Color = OSColorValue.RecordingRed

    override val FeedbackNew10: Color = OSColorValue.Yellow10
    override val FeedbackProgress10: Color = OSColorValue.Gray10
    override val FeedbackWarningStart: Color = OSColorValue.WarningRed
    override val FeedbackWarningEnd: Color = OSColorValue.WarningRedVariant
    override val Warning02: Color = OSColorValue.Beige02
    override val Warning05: Color = OSColorValue.Beige05
    override val Warning90: Color = OSColorValue.Beige90
    override val Warning95: Color = OSColorValue.Beige95

    override val Error30: Color = OSColorValue.Error30
}
