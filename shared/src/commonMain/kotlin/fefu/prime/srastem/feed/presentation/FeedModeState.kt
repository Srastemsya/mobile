package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.FeedCard

data class FeedCategoryOption(val id: String, val label: String, val kinds: List<String>)

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
    val filterOpen: Boolean = false,
    val kind: FeedKind = FeedKind.PLACE,
    val categories: List<FeedCategoryOption> = emptyList(),
    val categoryId: String? = null,
    val localDate: String = "",
    val budgetRubles: String = "",
    val partySizeText: String = "2",
    val fieldError: String? = null,
    val cards: List<FeedCard> = emptyList(),
    val feedError: String? = null,
)

/** Явная заглушка точки входа сценария, который ещё не реализован (FE-F01/FE-F04). */
data class FeedModeStub(val title: String, val message: String)
