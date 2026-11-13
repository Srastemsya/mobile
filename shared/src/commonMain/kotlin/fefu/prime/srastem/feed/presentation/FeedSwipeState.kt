package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.SwipeDecision

/** Process-scoped swipe state. History stores the decision that advanced from each card. */
data class FeedSwipeState(
    val cards: List<FeedCard> = emptyList(),
    val queuedCards: List<FeedCard> = emptyList(),
    val index: Int = 0,
    val decisions: Map<Int, SwipeDecision> = emptyMap(),
    val loadingNextPool: Boolean = false,
    val lastDecision: SwipeDecision = SwipeDecision.SKIP,
) {
    val current: FeedCard? get() = cards.getOrNull(index)
    val canUndo: Boolean get() = index > 0
    val progress: Float get() = if (cards.isEmpty()) 0f else (index + 1).toFloat() / cards.size

    companion object {
        const val LOCAL_POOL_SIZE = 10

        fun fromCandidates(candidates: List<FeedCard>): FeedSwipeState = FeedSwipeState(
            cards = candidates.take(LOCAL_POOL_SIZE),
            queuedCards = candidates.drop(LOCAL_POOL_SIZE),
        )
    }
}

enum class FeedSwipeDirection { LEFT, RIGHT }

fun FeedSwipeState.decide(decision: SwipeDecision): FeedSwipeState {
    if (current == null || loadingNextPool) return this
    val updatedDecisions = decisions + (index to decision)
    val nextIndex = index + 1
    if (nextIndex < cards.size) return copy(decisions = updatedDecisions, index = nextIndex, lastDecision = decision)
    if (queuedCards.isNotEmpty()) {
        return copy(
            cards = queuedCards.take(FeedSwipeState.LOCAL_POOL_SIZE),
            queuedCards = queuedCards.drop(FeedSwipeState.LOCAL_POOL_SIZE),
            index = 0,
            decisions = emptyMap(),
            lastDecision = decision,
        )
    }
    return copy(decisions = updatedDecisions, index = nextIndex, lastDecision = decision)
}

fun FeedSwipeState.undo(): FeedSwipeState = if (!canUndo) this else {
    val previous = index - 1
    copy(index = previous, decisions = decisions - previous, lastDecision = decisions[previous] ?: SwipeDecision.SKIP)
}
