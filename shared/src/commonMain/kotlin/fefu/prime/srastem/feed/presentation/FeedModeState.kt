package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.FeedKind

/**
 * UI-состояние экрана «Выбираем вместе» (FE-F00).
 * [pendingKind] — предвыбранный тип подборки: `EVENT` при входе «Свайпать события» с Главной,
 * пробрасывается в сценарий FE-F01 при выборе режима.
 */
data class FeedModeState(
    val pendingKind: FeedKind = FeedKind.PLACE,
    val busy: Boolean = false,
    val profileIncomplete: Boolean = false,
    val error: String? = null,
    val stub: FeedModeStub? = null,
)

/** Явная заглушка точки входа сценария, который ещё не реализован (FE-F01/FE-F04). */
data class FeedModeStub(val title: String, val message: String)
