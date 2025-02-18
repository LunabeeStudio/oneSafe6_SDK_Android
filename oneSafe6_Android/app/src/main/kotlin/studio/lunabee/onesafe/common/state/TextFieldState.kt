package studio.lunabee.onesafe.common.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Suppress("LongParameterList")
@Stable
class TextFieldState(
    val isSecured: Boolean,
    val isAPassword: Boolean,
    val isMandatory: Boolean,
    initialValue: String,
    currentValue: String,
    isSecuredValueVisible: Boolean,
    isEditedByUser: Boolean,
    hasBeenRemovedByUser: Boolean,
) {
    var initialValue: String by mutableStateOf(value = initialValue)
    var currentValue: String by mutableStateOf(value = currentValue)
    var isSecuredValueVisible: Boolean by mutableStateOf(value = isSecuredValueVisible)
    var isEditedByUser: Boolean by mutableStateOf(value = isEditedByUser)
    var hasBeenRemovedByUser: Boolean by mutableStateOf(value = hasBeenRemovedByUser)

    val isModified: Boolean by derivedStateOf {
        this.initialValue != this.currentValue
    }

    companion object {
        val Saver: Saver<TextFieldState, Any> = mapSaver(
            save = { state ->
                mapOf(
                    CurrentValueKey to state.currentValue,
                    InitialValueKey to state.initialValue,
                    IsSecuredKey to state.isSecured,
                    IsSecuredValueVisibleKey to state.isSecuredValueVisible,
                    IsMandatoryKey to state.isMandatory,
                    WasEditedByUserKey to state.isEditedByUser,
                    IsPasswordKey to state.isAPassword,
                    RemovedKey to state.hasBeenRemovedByUser,
                )
            },
            restore = { restoredMap ->
                TextFieldState(
                    currentValue = restoredMap[CurrentValueKey] as String,
                    initialValue = restoredMap[InitialValueKey] as String,
                    isSecured = restoredMap[IsSecuredKey] as Boolean,
                    isSecuredValueVisible = restoredMap[IsSecuredValueVisibleKey] as Boolean,
                    isMandatory = restoredMap[IsMandatoryKey] as Boolean,
                    isEditedByUser = restoredMap[WasEditedByUserKey] as Boolean,
                    isAPassword = restoredMap[IsPasswordKey] as Boolean,
                    hasBeenRemovedByUser = restoredMap[RemovedKey] as Boolean,
                )
            },
        )

        private const val CurrentValueKey: String = "currentValueKey"
        private const val InitialValueKey: String = "initialValueKey"
        private const val IsSecuredKey: String = "isSecuredKey"
        private const val IsSecuredValueVisibleKey: String = "isSecuredValueVisibleKey"
        private const val IsMandatoryKey: String = "isMandatoryKey"
        private const val WasEditedByUserKey: String = "wasEditedByUserKey"
        private const val IsPasswordKey: String = "IsPasswordKey"
        private const val RemovedKey = "RemovedKey"
    }
}

// TODO remove unused
@Composable
fun rememberTextFieldState(
    initialValue: String, // set as mandatory to avoid forgetting it!
    isSecured: Boolean = false,
    isAPassword: Boolean = false,
    isMandatory: Boolean = false,
    currentValue: String = "",
    isSecuredValueVisible: Boolean = !isSecured,
    wasEditedByUser: Boolean = false,
    hasBeenRemovedByUser: Boolean = false,
    key: String? = null,
    inputs: Array<Any> = emptyArray(),
): TextFieldState {
    return rememberSaveable(key = key, inputs = inputs, saver = TextFieldState.Saver) {
        TextFieldState(
            isSecured = isSecured,
            isMandatory = isMandatory,
            initialValue = initialValue,
            currentValue = currentValue,
            isSecuredValueVisible = isSecuredValueVisible,
            isEditedByUser = wasEditedByUser,
            isAPassword = isAPassword,
            hasBeenRemovedByUser = hasBeenRemovedByUser,
        )
    }
}
