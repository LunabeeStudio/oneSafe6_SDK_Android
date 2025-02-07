package studio.lunabee.onesafe.feature.settings.overencryption

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import studio.lunabee.onesafe.domain.model.crypto.DatabaseKey
import studio.lunabee.onesafe.domain.usecase.authentication.CreateDatabaseKeyUseCase
import javax.inject.Inject

@HiltViewModel
class OverEncryptionSettingDisabledViewModel @Inject constructor(
    createDatabaseKeyUseCase: CreateDatabaseKeyUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val key: DatabaseKey = savedStateHandle.get<ByteArray>(SavedKeyKey)?.let { DatabaseKey(it) } ?: createDatabaseKeyUseCase()

    init {
        if (!savedStateHandle.contains(SavedKeyKey)) {
            savedStateHandle[SavedKeyKey] = key.raw
        }
    }

    companion object {
        private const val SavedKeyKey: String = "94d42d77-c4d3-4d6b-8fe4-e0ff2225332b"
    }
}
