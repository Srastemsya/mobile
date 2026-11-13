package fefu.prime.srastem.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.feed.domain.CheckFeedAccessUseCase
import fefu.prime.srastem.feed.domain.FeedError
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.GetFeedCandidatesUseCase
import fefu.prime.srastem.feed.domain.GetFeedCategoriesUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedModeViewModel(
    private val profileGate: CheckFeedAccessUseCase,
    private val getCategories: GetFeedCategoriesUseCase? = null,
    private val getCandidates: GetFeedCandidatesUseCase? = null,
) : ViewModel() {

    private val _state = MutableStateFlow(FeedModeState())
    val state: StateFlow<FeedModeState> = _state.asStateFlow()

    fun onEvent(event: FeedModeEvent) {
        when (event) {
            FeedModeEvent.OpenedFromTab -> open(FeedKind.PLACE)
            FeedModeEvent.OpenedWithEventsKind -> open(FeedKind.EVENT)
            FeedModeEvent.SingleDeviceSelected -> selectMode(singleDeviceStub())
            FeedModeEvent.MultiDeviceSelected -> selectMode(multiDeviceStub())
            FeedModeEvent.DismissNotice -> _state.update {
                it.copy(stub = null, filterOpen = false, cards = emptyList(), feedError = null)
            }
            FeedModeEvent.RetryProfileCheck -> checkProfile()
            FeedModeEvent.CompleteProfileSelected -> _state.update {
                it
            }
            is FeedModeEvent.KindChanged -> _state.update { it.copy(kind = event.kind, categoryId = null, localDate = "", fieldError = null) }
            is FeedModeEvent.CategoryChanged -> _state.update { it.copy(categoryId = event.categoryId) }
            is FeedModeEvent.DateChanged -> _state.update { it.copy(localDate = event.value) }
            is FeedModeEvent.BudgetChanged -> _state.update { it.copy(budgetRubles = event.value) }
            is FeedModeEvent.PartySizeChanged -> _state.update { it.copy(partySizeText = event.value) }
            FeedModeEvent.SubmitFilters -> submitFilters()
            FeedModeEvent.ChangeFilters -> _state.update { it.copy(filterOpen = true, feedError = null) }
        }
    }

    /** Реактивная обработка ошибок сценариев ленты: `403 PROFILE_INCOMPLETE` открывает завершение регистрации. */
    fun onError(error: FeedError) {
        when (error) {
            is FeedError.ProfileIncomplete -> _state.update {
                it.copy(busy = false, profileIncomplete = true, stub = null, error = null)
            }
            else -> _state.update { it.copy(busy = false, error = error.message) }
        }
    }

    /** Регистрация завершена поверх ленты: перечитать доступ без сброса предвыбранного типа. */
    fun onRegistrationCompleted() {
        if (_state.value.profileIncomplete) checkProfile()
    }

    private fun open(pendingKind: FeedKind) {
        _state.update { FeedModeState(pendingKind = pendingKind) }
        checkProfile()
    }

    private fun selectMode(stub: FeedModeStub) {
        val current = _state.value
        if (current.kind == FeedKind.EVENT && current.localDate.isNotBlank() && !current.localDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            _state.update { it.copy(fieldError = "Укажите дату в формате ГГГГ-ММ-ДД") }; return
        }
        if (current.profileIncomplete || current.busy || current.error != null) return
        if (stub.title.startsWith("FE-F01") && getCategories != null) {
            _state.update { it.copy(stub = null, filterOpen = true, kind = it.pendingKind, error = null) }
            viewModelScope.launch {
                getCategories().onSuccess { categories ->
                    _state.update { state -> state.copy(categories = categories.map { fefu.prime.srastem.feed.presentation.FeedCategoryOption(it.id, it.label, it.kinds.map(FeedKind::name)) }) }
                }.onFailure { error -> _state.update { state -> state.copy(feedError = error.message ?: "Не удалось загрузить категории") } }
            }
        } else _state.update { it.copy(stub = stub, error = null) }
    }

    private fun submitFilters() {
        val current = _state.value
        val budgetText = current.budgetRubles
        val budget = if (budgetText.isBlank()) null else budgetText.toLongOrNull()?.takeIf { it >= 0 }
        if (budgetText.isNotBlank() && budget == null) { _state.update { it.copy(fieldError = "Введите неотрицательный целый бюджет") }; return }
        val party = current.partySizeText.toIntOrNull()
        if (budget != null && (party == null || party < 1)) { _state.update { it.copy(fieldError = "Укажите число человек от 1") }; return }
        val filters = FeedFilters(current.kind, current.categoryId, current.localDate.takeIf { current.kind == FeedKind.EVENT && it.isNotBlank() }, budget?.times(100), if (budget != null) party else null)
        if (getCandidates == null) { _state.update { it.copy(fieldError = "Загрузка подборки недоступна") }; return }
        _state.update { it.copy(busy = true, fieldError = null, feedError = null) }
        viewModelScope.launch {
            getCandidates(filters).onSuccess { page -> _state.update { it.copy(busy = false, filterOpen = false, cards = page.items, feedError = if (page.items.isEmpty()) "Подходящих вариантов пока нет. Измените фильтры." else null) } }
                .onFailure { error -> _state.update { it.copy(busy = false, feedError = error.message ?: "Не удалось загрузить подборку") } }
        }
    }

    private fun checkProfile() {
        _state.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                val complete = profileGate().getOrThrow()
                _state.update { it.copy(busy = false, profileIncomplete = !complete) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update { it.copy(busy = false, error = "Не удалось проверить профиль. Повторите попытку.") }
            }
        }
    }

    private fun singleDeviceStub(): FeedModeStub {
        val kind = _state.value.pendingKind
        return FeedModeStub(
            title = "FE-F01 · Фильтры подборки",
            message = buildString {
                append("Фильтры подборки и общий бюджет для совместного просмотра карточек на одном телефоне.")
                append(if (kind == FeedKind.EVENT) " Предвыбранный тип подборки: события (kind=EVENT)."
                else " Предвыбранный тип подборки: места (kind=PLACE).")
            },
        )
    }

    private fun multiDeviceStub(): FeedModeStub = FeedModeStub(
        title = "FE-F04 · Комната с друзьями",
        message = "Точка входа сценария «несколько устройств» зарезервирована: пакет FE-F04 ещё не реализован. " +
            "Здесь откроются создание комнаты, ссылка-приглашение и запуск общего голосования.",
    )
}
