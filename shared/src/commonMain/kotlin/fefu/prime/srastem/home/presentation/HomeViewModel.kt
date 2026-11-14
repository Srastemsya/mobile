package fefu.prime.srastem.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.home.domain.GetHome
import fefu.prime.srastem.home.domain.HomeContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val content: HomeContent? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class HomeViewModel(private val getHome: GetHome) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                _state.value = HomeUiState(content = getHome(), isLoading = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message ?: "Не удалось загрузить открытия"
                )
            }
        }
    }
}
