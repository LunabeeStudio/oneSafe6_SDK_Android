package studio.lunabee.onesafe.feature.itemform.viewmodel.state

import androidx.compose.runtime.Stable
import studio.lunabee.onesafe.feature.camera.model.CameraData
import java.util.UUID

@Stable
sealed interface ItemFormState {

    data object Initializing : ItemFormState

    data class Idle(
        val cameraDataForField: CameraData,
        val cameraDataForIcon: CameraData,
        val onCaptureAnother: (() -> Unit)?,
    ) : ItemFormState

    data object ReOrderField : ItemFormState

    data class Exit(
        val safeItemIdCreated: UUID?,
    ) : ItemFormState
}
