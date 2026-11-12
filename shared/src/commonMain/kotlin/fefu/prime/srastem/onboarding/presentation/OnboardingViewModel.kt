package fefu.prime.srastem.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.onboarding.domain.CompleteOnboarding
import fefu.prime.srastem.onboarding.domain.GetOnboardingCompleted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val isVisible: Boolean = false,
    val currentSlide: Int = 0,
)

class OnboardingViewModel(
    private val getOnboardingCompleted: GetOnboardingCompleted,
    private val completeOnboarding: CompleteOnboarding,
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val completed = getOnboardingCompleted()
            _uiState.value = OnboardingUiState(
                isLoading = false,
                isVisible = !completed,
            )
        }
    }

    fun onSlideChanged(index: Int) {
        if (index in 0 until SLIDE_COUNT && _uiState.value.isVisible) {
            _uiState.value = _uiState.value.copy(currentSlide = index)
        }
    }

    fun onContinue() {
        val state = _uiState.value
        if (state.isLoading || !state.isVisible) return

        if (state.currentSlide < SLIDE_COUNT - 1) {
            _uiState.value = state.copy(currentSlide = state.currentSlide + 1)
        } else {
            viewModelScope.launch {
                completeOnboarding()
                _uiState.value = state.copy(isVisible = false)
            }
        }
    }

    private companion object {
        const val SLIDE_COUNT = 4
    }
}
