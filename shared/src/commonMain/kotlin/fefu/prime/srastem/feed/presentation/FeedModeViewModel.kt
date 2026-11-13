package fefu.prime.srastem.feed.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fefu.prime.srastem.feed.domain.FeedError
import fefu.prime.srastem.feed.domain.FeedKind
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel экрана «Выбираем вместе» (FE-F00).
 *
 * [profileGate] — проверка завершённости профиля до входа в сценарии ленты
 * (AC-06): незавершённый профиль блокирует карточки режимов и ведёт на
 * завершение регистрации. `null` означает «проверка недоступна» (fail-open):
 * реактивную защиту даёт маппинг `403 PROFILE_INCOMPLETE` в [FeedError.ProfileIncomplete].
 */
class FeedModeViewModel(
    private val profileGate: (suspend () -> Boolean)? = null,
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
                it.copy(stub = null, error = null)
            }
            FeedModeEvent.CompleteProfileSelected -> _state.update {
                it.copy(profileIncomplete = false, busy = false)
            }
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
        if (current.profileIncomplete || current.busy) return
        _state.update { it.copy(stub = stub, error = null) }
    }

    private fun checkProfile() {
        val gate = profileGate ?: return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            try {
                val complete = gate()
                _state.update { it.copy(busy = false, profileIncomplete = !complete) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Проверка недоступна (офлайн): не блокируем вход, реактивную защиту даёт 403 PROFILE_INCOMPLETE.
                _state.update { it.copy(busy = false) }
            }
        }
    }

    private fun singleDeviceStub(): FeedModeStub {
        val kind = _state.value.pendingKind
        return FeedModeStub(
            title = "FE-F01 · Фильтры подборки",
            message = buildString {
                append("Точка входа сценария «одно устройство» зарезервирована: пакет FE-F01 ещё не реализован. ")
                append("Здесь откроются фильтры и общий бюджет для совместного просмотра карточек на одном телефоне.")
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
