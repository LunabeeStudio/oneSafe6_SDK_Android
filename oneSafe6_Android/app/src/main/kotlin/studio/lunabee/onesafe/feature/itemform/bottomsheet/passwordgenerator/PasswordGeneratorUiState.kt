package studio.lunabee.onesafe.feature.itemform.bottomsheet.passwordgenerator

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import studio.lunabee.onesafe.domain.model.password.PasswordConfig

@Stable
sealed interface PasswordGeneratorUiState {
    object Initializing : PasswordGeneratorUiState

    class Data(
        private val passwordConfig: PasswordConfig,
    ) : PasswordGeneratorUiState {
        var length: Int = passwordConfig.length
        var includeUpperCase: Boolean = passwordConfig.includeUpperCase
        var includeLowerCase: Boolean = passwordConfig.includeLowerCase
        var includeNumber: Boolean = passwordConfig.includeNumber
        var includeSymbol: Boolean = passwordConfig.includeSymbol

        var passwordLength: Float by mutableFloatStateOf(passwordConfig.length.toFloat())

        val upperCaseEnabled: Boolean
            get() = passwordConfig.includeLowerCase || passwordConfig.includeNumber || passwordConfig.includeSymbol

        val lowerCaseEnabled: Boolean
            get() = passwordConfig.includeUpperCase || passwordConfig.includeNumber || passwordConfig.includeSymbol

        val numberEnabled: Boolean
            get() = passwordConfig.includeUpperCase || passwordConfig.includeLowerCase || passwordConfig.includeSymbol

        val symbolEnabled: Boolean
            get() = passwordConfig.includeUpperCase || passwordConfig.includeLowerCase || passwordConfig.includeNumber

        val passwordLengthStepNumber: Int = PasswordConfig.MaxLength - PasswordConfig.MinLength - 1
        val passwordLengthValueRange: ClosedFloatingPointRange<Float> =
            PasswordConfig.MinLength.toFloat()..PasswordConfig.MaxLength.toFloat()

        fun config(): PasswordConfig = PasswordConfig(
            length = length,
            includeUpperCase = includeUpperCase,
            includeLowerCase = includeLowerCase,
            includeNumber = includeNumber,
            includeSymbol = includeSymbol,
        )
    }
}
