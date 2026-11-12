package fefu.prime.srastem.party.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.party.domain.PartyCategory
import fefu.prime.srastem.party.domain.PartyFeedType
import fefu.prime.srastem.party.domain.PartyFilters
import fefu.prime.srastem.party.domain.PartyPhase
import fefu.prime.srastem.party.domain.PartyRepository
import fefu.prime.srastem.party.domain.PartySession
import fefu.prime.srastem.party.domain.PartyVote
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class PartyPage { MODE, CREATE, JOIN, LOBBY, FILTERS, FEED, WAITING, RESULT }

data class PartyUiState(
    val page: PartyPage = PartyPage.MODE,
    val roomName: String = "Вечер пятницы",
    val participantName: String = "Алекс",
    val roomCode: String = "",
    val session: PartySession? = null,
    val filters: PartyFilters = PartyFilters(),
    val isSingleDevice: Boolean = false,
    val localItems: List<fefu.prime.srastem.party.domain.PartyItem> = emptyList(),
    val selectedItem: fefu.prime.srastem.party.domain.PartyItem? = null,
    val itemIndex: Int = 0,
    val error: String? = null,
    val busy: Boolean = false,
    val copied: Boolean = false,
)

class PartyViewModel(private val repository: PartyRepository) : ViewModel() {
    private val _state = MutableStateFlow(PartyUiState())
    val state: StateFlow<PartyUiState> = _state.asStateFlow()
    private var roomObservation: Job? = null

    fun openCreate() {
        roomObservation?.cancel()
        _state.update { it.copy(page = PartyPage.CREATE, session = null, isSingleDevice = false, localItems = emptyList(), selectedItem = null, error = null) }
    }
    fun openJoin() {
        roomObservation?.cancel()
        _state.update { it.copy(page = PartyPage.JOIN, session = null, isSingleDevice = false, localItems = emptyList(), selectedItem = null, error = null) }
    }
    fun openSingleDevice() {
        roomObservation?.cancel()
        _state.update {
            it.copy(
                page = PartyPage.FILTERS,
                session = null,
                isSingleDevice = true,
                localItems = emptyList(),
                selectedItem = null,
                itemIndex = 0,
                error = null,
            )
        }
    }
    fun back() = _state.update {
        it.copy(page = when (it.page) {
            PartyPage.CREATE, PartyPage.JOIN -> PartyPage.MODE
            PartyPage.LOBBY -> if (it.session?.room?.hostId == it.session?.participantId) PartyPage.MODE else PartyPage.JOIN
            PartyPage.FILTERS -> if (it.isSingleDevice) PartyPage.MODE else PartyPage.LOBBY
            PartyPage.FEED, PartyPage.WAITING, PartyPage.RESULT -> if (it.isSingleDevice) PartyPage.MODE else PartyPage.LOBBY
            PartyPage.MODE -> PartyPage.MODE
        }, error = null)
    }

    fun setRoomName(value: String) = _state.update { it.copy(roomName = value) }
    fun setParticipantName(value: String) = _state.update { it.copy(participantName = value) }
    fun setRoomCode(value: String) = _state.update { it.copy(roomCode = value.trim().take(128)) }

    fun acceptInvite(link: String) {
        roomObservation?.cancel()
        _state.update { it.copy(roomCode = link, page = PartyPage.JOIN, isSingleDevice = false) }
        joinRoom()
    }

    fun createRoom() = perform {
        val session = repository.createRoom(_state.value.roomName.trim(), _state.value.participantName.trim().ifBlank { "Гость" })
        attach(session, PartyPage.LOBBY)
    }

    fun joinRoom() = perform {
        val session = repository.joinRoom(_state.value.roomCode.trim(), _state.value.participantName.trim().ifBlank { "Гость" })
        attach(session, when (session.room.phase) {
            PartyPhase.LOBBY -> PartyPage.LOBBY
            PartyPhase.FEED -> PartyPage.FEED
            PartyPhase.WAITING -> PartyPage.WAITING
            PartyPhase.MATCH, PartyPhase.FINISHED -> PartyPage.RESULT
        })
    }

    fun updateFilters(transform: (PartyFilters) -> PartyFilters) {
        _state.update { it.copy(filters = transform(it.filters)) }
    }

    fun openFilters() = _state.update { it.copy(page = PartyPage.FILTERS, filters = it.session?.room?.filters ?: it.filters) }

    fun saveFiltersAndStart() = perform {
        val session = _state.value.session ?: return@perform
        repository.updateFilters(session.room.id, session.participantId, _state.value.filters)
        repository.startFeed(session.room.id, session.participantId)
        _state.update { it.copy(page = PartyPage.FEED, itemIndex = 0) }
    }

    fun startSingleDeviceFeed() = perform {
        val items = repository.getFeedItems(_state.value.filters)
        _state.update {
            it.copy(
                page = if (items.isEmpty()) PartyPage.RESULT else PartyPage.FEED,
                localItems = items,
                itemIndex = 0,
                selectedItem = null,
                error = null,
            )
        }
    }

    fun startFeed() = perform {
        val session = _state.value.session ?: return@perform
        repository.startFeed(session.room.id, session.participantId)
        _state.update { it.copy(page = PartyPage.FEED, itemIndex = 0) }
    }

    fun vote(vote: PartyVote) {
        if (_state.value.isSingleDevice) {
            val current = _state.value
            val item = current.localItems.getOrNull(current.itemIndex) ?: return
            if (vote == PartyVote.LIKE) {
                _state.update { it.copy(page = PartyPage.RESULT, selectedItem = item) }
            } else {
                val nextIndex = current.itemIndex + 1
                _state.update {
                    it.copy(
                        itemIndex = nextIndex,
                        page = if (nextIndex >= it.localItems.size) PartyPage.RESULT else PartyPage.FEED,
                        selectedItem = null,
                    )
                }
            }
            return
        }
        perform {
        val session = _state.value.session ?: return@perform
        val items = session.room.items
        val item = items.getOrNull(_state.value.itemIndex) ?: return@perform
        repository.submitVote(session.room.id, session.participantId, item.id, vote)
        val nextIndex = _state.value.itemIndex + 1
        val latestRoom = if (items.size - nextIndex <= 2) {
            repository.loadMoreItems(session.room.id, session.participantId, nextIndex)
        } else {
            null
        }
        _state.update { current ->
            current.copy(
                session = if (latestRoom == null) current.session else current.session?.copy(room = latestRoom),
                itemIndex = nextIndex,
            )
        }
        val availableItems = latestRoom?.items ?: _state.value.session?.room?.items.orEmpty()
        if (nextIndex >= availableItems.size) {
            repository.completeFeed(session.room.id, session.participantId)
            _state.update { it.copy(page = PartyPage.WAITING) }
        }
        }
    }

    fun copyInvite() = _state.update { it.copy(copied = true) }
    fun undoVote() {
        val current = _state.value
        if (!current.isSingleDevice || current.page != PartyPage.FEED || current.itemIndex <= 0) return
        _state.update { it.copy(itemIndex = it.itemIndex - 1, error = null) }
    }
    fun retry() = _state.update { it.copy(error = null, page = PartyPage.MODE) }

    private fun attach(session: PartySession, page: PartyPage) {
        roomObservation?.cancel()
        _state.update { it.copy(session = session, roomCode = session.room.id, filters = session.room.filters, page = page, error = null) }
        roomObservation = viewModelScope.launch {
            repository.observeRoom(session.room.id).collect { room ->
                _state.update { current ->
                    val updated = session.copy(room = room)
                    current.copy(
                        session = updated,
                        filters = if (current.page == PartyPage.FILTERS) current.filters else room.filters,
                        page = when (room.phase) {
                            PartyPhase.LOBBY -> if (current.page == PartyPage.JOIN) PartyPage.LOBBY else current.page
                            PartyPhase.FEED -> if (current.page == PartyPage.WAITING || current.page == PartyPage.LOBBY) PartyPage.FEED else current.page
                            PartyPhase.WAITING -> if (current.page == PartyPage.FEED) PartyPage.WAITING else current.page
                            PartyPhase.MATCH, PartyPhase.FINISHED -> PartyPage.RESULT
                        },
                    )
                }
            }
        }
    }

    private fun perform(block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(busy = true, error = null) }
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                _state.update { it.copy(error = error.message ?: "Не удалось выполнить действие") }
            } finally {
                _state.update { it.copy(busy = false) }
            }
        }
    }
}
