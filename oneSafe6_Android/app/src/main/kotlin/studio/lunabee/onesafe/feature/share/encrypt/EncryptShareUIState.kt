package studio.lunabee.onesafe.feature.share.encrypt

import studio.lunabee.onesafe.domain.model.share.SharingData

interface EncryptShareUIState {

    object Idle : EncryptShareUIState

    data class Encrypting(
        val itemsNbr: Int,
    ) : EncryptShareUIState

    data class ReadyToShare(
        val sharingData: SharingData,
    ) : EncryptShareUIState

    object Error : EncryptShareUIState
}
