package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.FeedRef
import fefu.prime.srastem.feed.domain.SwipeDecision
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedSwipeStateTest {
    private fun candidates(count: Int) = (0 until count).map { index ->
        FeedCard(FeedRef(FeedKind.PLACE, "id-$index"), "Card $index", "", "", null, "", "", null)
    }

    @Test
    fun splitsTwentyCandidatesIntoTwoPoolsAndResetsAtBoundary() {
        val all = candidates(20)
        var state = FeedSwipeState.fromCandidates(all)

        assertEquals(10, state.cards.size)
        assertEquals(all.take(10), state.cards)
        assertEquals(10, state.queuedCards.size)
        repeat(9) { state = state.decide(SwipeDecision.SKIP) }
        assertEquals(9, state.index)
        assertTrue(state.canUndo)
        state = state.decide(SwipeDecision.LIKE)

        assertEquals(all.drop(10), state.cards)
        assertTrue(state.queuedCards.isEmpty())
        assertEquals(0, state.index)
        assertEquals(emptyMap(), state.decisions)
        assertFalse(state.canUndo)
        assertEquals(0.1f, state.progress)
        assertEquals("id-10", state.current?.ref?.id)
    }

    @Test
    fun splitPreservesShortOverflowWithoutSkippingOrRepeating() {
        val all = candidates(13)
        var state = FeedSwipeState.fromCandidates(all)
        repeat(10) { state = state.decide(SwipeDecision.SKIP) }
        assertEquals(all.drop(10), state.cards)
        assertTrue(state.queuedCards.isEmpty())
        assertEquals(0, state.index)
        assertEquals(1f / 3f, state.progress)
        repeat(3) { state = state.decide(SwipeDecision.LIKE) }
        assertEquals(null, state.current)
        assertEquals(all.map { it.ref.id }, (candidates(10) + all.drop(10)).map { it.ref.id })
    }

    @Test
    fun loadingNextPoolSuppressesSwipeDecisions() {
        val state = FeedSwipeState.fromCandidates(candidates(10)).copy(loadingNextPool = true)
        assertEquals(state, state.decide(SwipeDecision.LIKE))
        assertEquals("id-0", state.current?.ref?.id)
        assertEquals(0, state.index)
    }

    @Test
    fun delayedPoolCompletionKeepsActionsSuppressedUntilReady() = runTest {
        var state = FeedSwipeState.fromCandidates(candidates(10)).copy(loadingNextPool = true)
        val poolRequest = CompletableDeferred<Unit>()
        val pendingRequest = launch { poolRequest.await(); state = state.copy(loadingNextPool = false) }

        state = state.decide(SwipeDecision.SKIP)
        assertTrue(state.loadingNextPool)
        assertEquals(0, state.index)
        assertEquals("id-0", state.current?.ref?.id)

        poolRequest.complete(Unit)
        pendingRequest.join()
        assertFalse(state.loadingNextPool)
    }
}
