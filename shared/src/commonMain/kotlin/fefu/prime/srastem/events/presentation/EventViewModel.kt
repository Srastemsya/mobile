package fefu.prime.srastem.events.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.events.domain.EventDraft
import fefu.prime.srastem.events.domain.EventAdmission
import fefu.prime.srastem.events.domain.EventParticipant
import fefu.prime.srastem.events.domain.EventUseCases
import fefu.prime.srastem.events.domain.SrEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EventPage { LIST, DETAIL, CREATE_LOCATION, CREATE_FORM, CREATED, JOINED, ORGANIZER }

data class EventUiState(
    val page: EventPage = EventPage.LIST,
    val events: List<SrEvent> = emptyList(),
    val selectedEventId: String? = null,
    val currentUserId: String = "",
    val draft: EventDraft = EventDraft(),
    val pendingParticipant: EventParticipant? = null,
    val isBusy: Boolean = false,
    val error: String? = null,
)

class EventViewModel(private val useCases: EventUseCases) : ViewModel() {
    private val _state = MutableStateFlow(EventUiState(currentUserId = useCases.currentUserId))
    val state: StateFlow<EventUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            useCases.observeEvents().catch { error -> showError(error) }
                .collect { events -> _state.update { it.copy(events = events) } }
        }
    }

    fun openEvent(id: String) = _state.update {
        it.copy(page = EventPage.DETAIL, selectedEventId = id, error = null)
    }

    fun openCreate() = _state.update {
        it.copy(page = EventPage.CREATE_LOCATION, draft = EventDraft(), selectedEventId = null, error = null)
    }

    fun chooseLocation(address: String, latitude: Double, longitude: Double) = _state.update {
        it.copy(page = EventPage.CREATE_FORM, draft = it.draft.copy(address = address, latitude = latitude, longitude = longitude), error = null)
    }

    fun updateDraft(transform: (EventDraft) -> EventDraft) = _state.update { it.copy(draft = transform(it.draft), error = null) }
    fun clearError() = _state.update { it.copy(error = null) }

    fun publish() {
        val draft = _state.value.draft
        if (draft.title.isBlank()) return showError(IllegalArgumentException("Укажите название события"))
        if (draft.address.isBlank()) return showError(IllegalArgumentException("Выберите место встречи"))
        if (draft.admission == EventAdmission.BOOKING && (draft.capacity.toIntOrNull() ?: 0) <= 0) {
            return showError(IllegalArgumentException("Количество мест должно быть больше нуля"))
        }
        perform {
            val created = useCases.createEvent(draft)
            _state.update { it.copy(page = EventPage.CREATED, selectedEventId = created.id) }
        }
    }

    fun joinSelectedEvent() {
        val event = selectedEvent() ?: return
        perform {
            useCases.joinEvent(event.id, _state.value.currentUserId)
            _state.update { it.copy(page = EventPage.JOINED) }
        }
    }

    fun leaveSelectedEvent() {
        val event = selectedEvent() ?: return
        perform {
            useCases.leaveEvent(event.id, _state.value.currentUserId)
            _state.update { it.copy(page = EventPage.DETAIL) }
        }
    }

    fun openOrganizer() = _state.update { it.copy(page = EventPage.ORGANIZER, error = null) }

    fun askRemoveParticipant(participant: EventParticipant) = _state.update { it.copy(pendingParticipant = participant) }
    fun cancelRemoveParticipant() = _state.update { it.copy(pendingParticipant = null) }

    fun confirmRemoveParticipant() {
        val event = selectedEvent() ?: return
        val participant = _state.value.pendingParticipant ?: return
        perform {
            useCases.removeParticipant(event.id, _state.value.currentUserId, participant.id)
            _state.update { it.copy(pendingParticipant = null) }
        }
    }

    fun deleteSelectedEvent() {
        val event = selectedEvent() ?: return
        perform {
            useCases.deleteEvent(event.id, _state.value.currentUserId)
            _state.update { it.copy(page = EventPage.LIST, selectedEventId = null) }
        }
    }

    fun back() {
        _state.update { state ->
            when (state.page) {
                EventPage.LIST -> state
                EventPage.DETAIL -> state.copy(page = EventPage.LIST, selectedEventId = null, error = null)
                EventPage.CREATE_LOCATION -> state.copy(page = EventPage.LIST, error = null)
                EventPage.CREATE_FORM -> state.copy(page = EventPage.CREATE_LOCATION, error = null)
                EventPage.CREATED -> state.copy(page = EventPage.LIST, selectedEventId = null, error = null)
                EventPage.JOINED -> state.copy(page = EventPage.DETAIL, error = null)
                EventPage.ORGANIZER -> state.copy(page = EventPage.DETAIL, error = null)
            }
        }
    }

    fun selectedEvent(): SrEvent? = _state.value.events.firstOrNull { it.id == _state.value.selectedEventId }

    private fun perform(action: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isBusy = true, error = null) }
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                showError(error)
            } finally {
                _state.update { it.copy(isBusy = false) }
            }
        }
    }

    private fun showError(error: Throwable) {
        _state.update { it.copy(error = error.message ?: "Не удалось выполнить действие") }
    }
}
