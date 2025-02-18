package studio.lunabee.onesafe.feature.itemform.manager

import dagger.hilt.android.scopes.ViewModelScoped
import studio.lunabee.onesafe.feature.itemform.model.uifield.FieldObserver
import javax.inject.Inject

@ViewModelScoped
class UrlMetadataFieldObserver @Inject constructor(
    private val urlMetadataManager: UrlMetadataManager,
) : FieldObserver {
    override fun onValueChanged(value: String) {
        urlMetadataManager.fetchUrlMetadataIfNeeded(value)
    }

    override fun onRemoved() {
        urlMetadataManager.cancelFetch()
    }
}
