package studio.lunabee.onesafe.feature.itemform.model.uifield

import javax.inject.Inject

interface FieldObserver {
    fun onValueChanged(value: String)
    fun onRemoved()
}

interface ObservableField {
    fun setObserver(observer: FieldObserver)
    fun removeObserver()
}

class DefaultObservableField @Inject constructor() : ObservableField {
    var textObserver: FieldObserver? = null

    override fun setObserver(observer: FieldObserver) {
        textObserver = observer
    }

    override fun removeObserver() {
        textObserver?.onRemoved()
        textObserver = null
    }
}
