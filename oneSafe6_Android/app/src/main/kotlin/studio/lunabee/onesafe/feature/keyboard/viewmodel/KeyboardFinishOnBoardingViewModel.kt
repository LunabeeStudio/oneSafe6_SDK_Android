package studio.lunabee.onesafe.feature.keyboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import studio.lunabee.onesafe.domain.usecase.settings.SetAppVisitUseCase
import javax.inject.Inject

@HiltViewModel
class KeyboardFinishOnBoardingViewModel @Inject constructor(
    private val setAppVisitUseCase: SetAppVisitUseCase,
) : ViewModel() {
    init {
        viewModelScope.launch {
            setAppVisitUseCase.setHasFinishOneSafeKOnBoarding()
        }
    }
}
