package studio.lunabee.onesafe.common.extensions

import studio.lunabee.onesafe.commonui.OSString
import java.text.DecimalFormat
import kotlin.math.pow

fun Long.byteToHumanReadable(): Pair<String, Int> {
    val b = this.toDouble()
    val k = this / BytesNbr
    val m = this / (BytesNbr.pow(2))
    val g = this / BytesNbr.pow(3)
    val t = this / BytesNbr.pow(4)
    val dec = DecimalFormat("0")
    return if (t >= 1) {
        dec.format(t) to OSString.fileSize_tera
    } else if (g >= 1) {
        dec.format(g) to OSString.fileSize_giga
    } else if (m >= 1) {
        dec.format(m) to OSString.fileSize_mega
    } else if (k >= 1) {
        dec.format(k) to OSString.fileSize_kilo
    } else {
        dec.format(b) to OSString.fileSize_bytes
    }
}

private const val BytesNbr: Double = 1024.0
