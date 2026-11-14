package fefu.prime.srastem.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.home.domain.GetHome
import fefu.prime.srastem.home.domain.HomeCollection
import fefu.prime.srastem.home.domain.HomeContent
import fefu.prime.srastem.profile.domain.EventStatus
import fefu.prime.srastem.profile.domain.ProfileEvent
import fefu.prime.srastem.profile.domain.ProfileRepository
import fefu.prime.srastem.profile.domain.ProfileSavedKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val content: HomeContent? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val savedCollectionIds: Set<String> = emptySet(),
)

class HomeViewModel(
    private val getHome: GetHome,
    private val profileRepository: ProfileRepository? = null,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val content = getHome()
                _state.value = _state.value.copy(content = content, isLoading = false, error = null)
                refreshFavorites()
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

    fun refreshFavorites() {
        val repository = profileRepository ?: return
        viewModelScope.launch {
            runCatching { repository.getSavedEvents() }
                .onSuccess { saved ->
                    _state.value = _state.value.copy(
                        savedCollectionIds = saved.asSequence()
                            .filter { it.kind == ProfileSavedKind.HOME_COLLECTION }
                            .map { it.id.removePrefix(HOME_COLLECTION_ID_PREFIX) }
                            .toSet(),
                    )
                }
        }
    }

    fun toggleFavorite(collection: HomeCollection, coverKey: String) {
        val repository = profileRepository ?: return
        val savedId = HOME_COLLECTION_ID_PREFIX + collection.id
        val wasSaved = collection.id in state.value.savedCollectionIds
        _state.value = _state.value.copy(
            savedCollectionIds = if (wasSaved) {
                state.value.savedCollectionIds - collection.id
            } else {
                state.value.savedCollectionIds + collection.id
            },
        )
        viewModelScope.launch {
            runCatching {
                if (wasSaved) {
                    repository.removeSavedEvent(savedId)
                } else {
                    repository.saveEvent(
                        ProfileEvent(
                            id = savedId,
                            title = collection.title,
                            subtitle = collection.subtitle,
                            category = collection.category,
                            status = EventStatus.UPCOMING,
                            kind = ProfileSavedKind.HOME_COLLECTION,
                            coverKey = coverKey,
                        ),
                    )
                }
            }.onFailure {
                _state.value = _state.value.copy(
                    savedCollectionIds = if (wasSaved) {
                        state.value.savedCollectionIds + collection.id
                    } else {
                        state.value.savedCollectionIds - collection.id
                    },
                )
            }
        }
    }

    private companion object {
        const val HOME_COLLECTION_ID_PREFIX = "home:"
    }
}
