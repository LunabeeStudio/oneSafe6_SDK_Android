package studio.lunabee.onesafe.feature.camera.model

import android.view.Surface

enum class Orientation(val degrees: Float, val surfaceRotation: Int) {
    ROTATION_270(degrees = 270f, surfaceRotation = Surface.ROTATION_270),
    ROTATION_180(degrees = 180f, surfaceRotation = Surface.ROTATION_180),
    ROTATION_90(degrees = 90f, surfaceRotation = Surface.ROTATION_90),
    ROTATION_0(degrees = 0f, surfaceRotation = Surface.ROTATION_0),
    ;

    companion object {
        fun getOrientation(orientation: Int): Orientation {
            return when (orientation) {
                in 45..134 -> ROTATION_270
                in 135..224 -> ROTATION_180
                in 225..314 -> ROTATION_90
                else -> ROTATION_0
            }
        }
    }
}
