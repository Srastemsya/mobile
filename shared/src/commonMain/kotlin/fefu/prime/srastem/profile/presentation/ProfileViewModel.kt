package fefu.prime.srastem.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.profile.domain.ProfileDetails
import fefu.prime.srastem.profile.domain.ProfileEvent
import fefu.prime.srastem.profile.domain.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

enum class ProfilePage { HOME, DETAILS, SAVED, EVENTS }

data class ProfileUiState(
    val page: ProfilePage = ProfilePage.HOME,
    val profile: ProfileDetails = ProfileDetails("Ренат", "Владивосток", "24.08.2001", "", null),
    val savedEvents: List<ProfileEvent> = emptyList(),
    val myEvents: List<ProfileEvent> = emptyList(),
)

class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val profile = repository.getProfile()
            val savedEvents = repository.getSavedEvents()
            val myEvents = try {
                repository.getMyEvents()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
            _state.update {
                it.copy(
                    profile = profile,
                    savedEvents = savedEvents,
                    myEvents = myEvents ?: it.myEvents,
                )
            }
        }
    }

    fun open(page: ProfilePage) {
        _state.update { it.copy(page = page) }
    }

    fun back() = open(ProfilePage.HOME)

    fun updateProfile(transform: (ProfileDetails) -> ProfileDetails) {
        _state.update { it.copy(profile = transform(it.profile)) }
        val profile = _state.value.profile
        viewModelScope.launch { repository.saveProfile(profile) }
    }

    fun removeSaved(id: String) {
        viewModelScope.launch {
            repository.removeSavedEvent(id)
            _state.update { it.copy(savedEvents = repository.getSavedEvents()) }
        }
    }

    fun saveEvent(event: ProfileEvent) {
        viewModelScope.launch {
            repository.saveEvent(event)
            _state.update { it.copy(savedEvents = repository.getSavedEvents()) }
        }
    }

}
