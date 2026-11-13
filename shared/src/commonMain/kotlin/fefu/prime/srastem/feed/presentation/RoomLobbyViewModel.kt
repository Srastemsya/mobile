package fefu.prime.srastem.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.feed.domain.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** FE-F04 room creation, invite, and lobby. */
data class RoomLobbyState(
    val name: String = "",
    val room: RoomSnapshot? = null,
    val invite: RoomInvite? = null,
    val ownUserId: String = "",
    val busy: Boolean = false,
    val copied: Boolean = false,
    val error: String? = null,
    val joined: Boolean = false,
    val filtersOpen: Boolean = false,
    val kind: FeedKind = FeedKind.PLACE,
    val budget: String = "",
    val localDate: String = "",
    val started: Boolean = false,
) {
    val isOwner: Boolean get() = room != null && room.ownerId == ownUserId
    val canStart: Boolean get() = isOwner && room?.phase == RoomPhase.LOBBY && (room?.members?.size ?: 0) >= 2
}

class RoomLobbyViewModel(
    private val createRoom: CreateRoomUseCase,
    private val createInvite: CreateInviteUseCase,
    private val joinRoom: JoinRoomUseCase,
    private val observeRoom: ObserveRoomUseCase,
    private val startRoom: StartRoomUseCase,
    private val ownUserId: () -> String,
) : ViewModel() {
    private val _state = MutableStateFlow(RoomLobbyState())
    val state: StateFlow<RoomLobbyState> = _state.asStateFlow()
    private var observation: Job? = null

    fun openCreate() { observation?.cancel(); _state.value = RoomLobbyState(ownUserId = ownUserId()) }
    fun setName(value: String) { _state.update { it.copy(name = value, error = null) } }
    fun create() {
        val name = state.value.name.trim()
        if (name.isEmpty()) { _state.update { it.copy(error = "Введите название комнаты") }; return }
        if (state.value.busy) return
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            createRoom(name).onSuccess { room ->
                attach(room, joined = false)
                createInvite(room.id).onSuccess { invite -> _state.update { it.copy(invite = invite) } }
                    .onFailure { error -> _state.update { it.copy(error = error.message ?: "Не удалось создать ссылку") } }
            }
                .onFailure { error -> _state.update { it.copy(error = error.message ?: "Не удалось создать комнату") } }
            _state.update { it.copy(busy = false) }
        }
    }
    fun acceptInvite(link: String) {
        val token = Regex("(?:[?&]token=)([^&#]+)").find(link)?.groupValues?.get(1)
            ?: link.takeIf { it.isNotBlank() && !it.contains("://") }
        observation?.cancel()
        _state.value = RoomLobbyState(ownUserId = ownUserId(), busy = true, joined = true)
        if (token == null) { _state.update { it.copy(busy = false, error = "Недействительная ссылка. Попросите новую ссылку") }; return }
        viewModelScope.launch {
            joinRoom(token).onSuccess { attach(it, joined = true) }
                .onFailure { error -> _state.update { it.copy(error = if (error is FeedError.InviteExpired) error.message else error.message ?: "Не удалось войти в комнату") } }
            _state.update { it.copy(busy = false) }
        }
    }
    fun issueInvite() {
        val room = state.value.room ?: return
        if (!state.value.isOwner || state.value.busy) return
        _state.update { it.copy(busy = true, error = null, copied = false) }
        viewModelScope.launch {
            createInvite(room.id).onSuccess { invite -> _state.update { it.copy(invite = invite) } }
                .onFailure { error -> _state.update { it.copy(error = error.message ?: "Не удалось создать ссылку") } }
            _state.update { it.copy(busy = false) }
        }
    }
    fun openFilters() { if (state.value.canStart) _state.update { it.copy(filtersOpen = true, error = null) } }
    fun closeFilters() { _state.update { it.copy(filtersOpen = false, error = null) } }
    fun setKind(kind: FeedKind) { _state.update { it.copy(kind = kind, localDate = "") } }
    fun setBudget(value: String) { _state.update { it.copy(budget = value, error = null) } }
    fun setDate(value: String) { _state.update { it.copy(localDate = value, error = null) } }
    fun start() {
        val current = state.value
        val room = current.room ?: return
        if (!current.canStart || current.busy) return
        val budget = current.budget.trim().takeIf { it.isNotEmpty() }?.toLongOrNull()
        if (current.budget.isNotBlank() && (budget == null || budget < 0 || budget > Long.MAX_VALUE / 100)) {
            _state.update { it.copy(error = "Введите неотрицательный целый бюджет") }; return
        }
        if (current.kind == FeedKind.EVENT && current.localDate.isNotBlank() && !current.localDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            _state.update { it.copy(error = "Укажите дату в формате ГГГГ-ММ-ДД") }; return
        }
        val filters = FeedFilters(current.kind, localDate = current.localDate.trim().takeIf { current.kind == FeedKind.EVENT && it.isNotEmpty() }, budgetTotalMinor = budget?.times(100), partySize = null)
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            startRoom(room.id, filters).onSuccess { next -> _state.update { it.copy(room = next, filtersOpen = false, started = true) } }
                .onFailure { error -> _state.update { it.copy(error = error.message ?: "Не удалось начать выбор") } }
            _state.update { it.copy(busy = false) }
        }
    }
    fun copied() { _state.update { it.copy(copied = true) } }
    fun leave() { observation?.cancel(); observation = null; _state.value = RoomLobbyState(ownUserId = ownUserId()) }
    private fun attach(room: RoomSnapshot, joined: Boolean) {
        observation?.cancel()
        _state.update { it.copy(room = room, joined = joined, name = room.name.orEmpty(), error = null, ownUserId = ownUserId()) }
        observation = viewModelScope.launch {
            observeRoom(room.id).collect { next ->
                _state.update { current -> if (next.version >= (current.room?.version ?: -1L)) current.copy(room = next) else current }
            }
        }
    }
}
