package fefu.prime.srastem.feed.presentation

import fefu.prime.srastem.feed.domain.CheckFeedAccessUseCase
import fefu.prime.srastem.feed.domain.FeedAccessRepository
import fefu.prime.srastem.feed.domain.FeedError
import fefu.prime.srastem.feed.domain.FeedKind
import fefu.prime.srastem.feed.domain.FeedFilters
import fefu.prime.srastem.feed.domain.FeedRepository
import fefu.prime.srastem.feed.domain.FeedPage
import fefu.prime.srastem.feed.domain.FeedCard
import fefu.prime.srastem.feed.domain.FeedRef
import fefu.prime.srastem.feed.domain.GetFeedCandidatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit-тесты ViewModel экрана «Выбираем вместе» (FE-F00): предвыбранный тип подборки (AC-05),
 * гейт завершённости профиля (AC-06), заглушки точек входа FE-F01/FE-F04 и возврат «Назад» (AC-03/AC-04).
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedModeViewModelTest {
    private fun vm(gate: suspend () -> Boolean) = FeedModeViewModel(CheckFeedAccessUseCase(object : FeedAccessRepository {
        override suspend fun isProfileComplete(): Boolean = gate()
    }))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun tabEntryResetsPendingKindToPlaces() = runTest {
        val viewModel = vm { true }

        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        assertEquals(FeedKind.PLACE, viewModel.state.value.pendingKind)
        assertFalse(viewModel.state.value.profileIncomplete)
    }

    @Test
    fun homeSwipeEntryPreselectsEventsKind() = runTest {
        val viewModel = vm { true }

        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)

        assertEquals(FeedKind.EVENT, viewModel.state.value.pendingKind)
    }

    @Test
    fun incompleteProfileBlocksModeCards() = runTest {
        val viewModel = vm { false }

        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)
        assertTrue(viewModel.state.value.profileIncomplete)

        viewModel.onEvent(FeedModeEvent.SingleDeviceSelected)
        assertNull(viewModel.state.value.stub)
    }

    @Test
    fun profileIncompleteErrorFromScenariosOpensCompletionGate() = runTest {
        val viewModel = vm { true }
        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        viewModel.onError(FeedError.ProfileIncomplete())

        assertTrue(viewModel.state.value.profileIncomplete)
        assertNull(viewModel.state.value.stub)

        viewModel.onEvent(FeedModeEvent.CompleteProfileSelected)
        assertTrue(viewModel.state.value.profileIncomplete)
    }

    @Test
    fun transportErrorCanBeRetried() = runTest {
        val viewModel = vm { true }
        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        viewModel.onError(FeedError.Transport("Сервер недоступен"))

        assertEquals("Сервер недоступен", viewModel.state.value.error)
        assertFalse(viewModel.state.value.profileIncomplete)
        viewModel.onEvent(FeedModeEvent.RetryProfileCheck)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun modeCardsOpenDocumentedStubsAndBackReturnsToChoice() = runTest {
        val viewModel = vm { true }
        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)

        viewModel.onEvent(FeedModeEvent.SingleDeviceSelected)
        val single = assertNotNull(viewModel.state.value.stub)
        assertTrue(single.message.contains("kind=EVENT"), "Заглушка FE-F01 обязана нести предвыбранный тип")

        viewModel.onEvent(FeedModeEvent.DismissNotice)
        assertNull(viewModel.state.value.stub)
        assertEquals(FeedKind.EVENT, viewModel.state.value.pendingKind)

        viewModel.onEvent(FeedModeEvent.MultiDeviceSelected)
        assertNotNull(viewModel.state.value.stub)
        viewModel.onEvent(FeedModeEvent.DismissNotice)
        assertNull(viewModel.state.value.stub)
    }

    @Test
    fun registrationCompletionRechecksGateWithoutLosingPendingKind() = runTest {
        var complete = false
        val viewModel = vm { complete }
        viewModel.onEvent(FeedModeEvent.OpenedWithEventsKind)
        assertTrue(viewModel.state.value.profileIncomplete)

        complete = true
        viewModel.onRegistrationCompleted()

        assertFalse(viewModel.state.value.profileIncomplete)
        assertEquals(FeedKind.EVENT, viewModel.state.value.pendingKind)
    }

    @Test
    fun unavailableGateBlocksUntilRetry() = runTest {
        var available = false
        val viewModel = vm { if (!available) error("offline") else true }

        viewModel.onEvent(FeedModeEvent.OpenedFromTab)

        assertNotNull(viewModel.state.value.error)
        viewModel.onEvent(FeedModeEvent.SingleDeviceSelected)
        assertNull(viewModel.state.value.stub)
        available = true
        viewModel.onEvent(FeedModeEvent.RetryProfileCheck)
        viewModel.onEvent(FeedModeEvent.SingleDeviceSelected)
        assertNotNull(viewModel.state.value.stub)
    }

    private class CapturingFeedRepository : FeedRepository {
        val submitted = mutableListOf<FeedFilters>()
        val cursors = mutableListOf<String?>()
        var pages = mutableListOf<FeedPage>()
        override suspend fun getCandidates(filters: FeedFilters, cursor: String?): FeedPage {
            submitted += filters
            cursors += cursor
            return if (pages.isNotEmpty()) pages.removeAt(0)
            else FeedPage(listOf(FeedCard(FeedRef(filters.kind, "id-${submitted.size}"), "Card", "", "", null, "", "", null)), null)
        }
        override suspend fun getCatalogDetails(ref: FeedRef) = fefu.prime.srastem.feed.domain.CatalogDetails(ref, "Title", "Description", "food", "Address", null, null, "Бесплатно", null, null)
    }

    private fun filteringVm(repository: CapturingFeedRepository) = FeedModeViewModel(
        CheckFeedAccessUseCase(object : FeedAccessRepository {
            override suspend fun isProfileComplete(): Boolean = true
        }),
        getCandidates = GetFeedCandidatesUseCase(repository),
    )

    @Test
    fun submittingFiltersMapsAllFieldsAndConvertsRublesToMinorUnits() = runTest {
        val repository = CapturingFeedRepository()
        val viewModel = filteringVm(repository)
        viewModel.onEvent(FeedModeEvent.KindChanged(FeedKind.EVENT))
        viewModel.onEvent(FeedModeEvent.CategoryChanged("music"))
        viewModel.onEvent(FeedModeEvent.DateChanged("2026-11-20"))
        viewModel.onEvent(FeedModeEvent.BudgetChanged("1250"))
        viewModel.onEvent(FeedModeEvent.PartySizeChanged("3"))

        viewModel.onEvent(FeedModeEvent.SubmitFilters)

        assertEquals(listOf(FeedFilters(FeedKind.EVENT, "music", "2026-11-20", 125_000L, 3)), repository.submitted)
        assertFalse(viewModel.state.value.filterOpen)
    }

    @Test
    fun firstLikeImmediatelyShowsChosenResultWithoutRequestingAnotherPage() = runTest {
        val repository = CapturingFeedRepository()
        repository.pages += FeedPage(listOf(FeedCard(FeedRef(FeedKind.EVENT, "event-1"), "Event", "music", "Бесплатно", 0, "Владивосток", "Описание", null)), "next")
        val viewModel = filteringVm(repository)
        viewModel.onEvent(FeedModeEvent.KindChanged(FeedKind.EVENT))
        viewModel.onEvent(FeedModeEvent.SubmitFilters)

        viewModel.onEvent(FeedModeEvent.SwipeDecided(fefu.prime.srastem.feed.domain.SwipeDecision.LIKE))

        assertEquals("event-1", viewModel.state.value.chosen?.ref?.id)
        assertEquals("Event", viewModel.state.value.chosen?.title)
        assertEquals(listOf<String?>(null), repository.cursors)
    }

    @Test
    fun skippingLastTenCandidatesAutomaticallyLoadsNextCursorPage() = runTest {
        val repository = CapturingFeedRepository()
        val first = (0 until 10).map { FeedCard(FeedRef(FeedKind.PLACE, "p$it"), "Place $it", "", "", null, "", "", null) }
        val next = FeedCard(FeedRef(FeedKind.PLACE, "next"), "Next place", "", "", null, "", "", null)
        repository.pages += FeedPage(first, "cursor-next")
        repository.pages += FeedPage(listOf(next), null)
        val viewModel = filteringVm(repository)
        viewModel.onEvent(FeedModeEvent.SubmitFilters)

        repeat(10) { viewModel.onEvent(FeedModeEvent.SwipeDecided(fefu.prime.srastem.feed.domain.SwipeDecision.SKIP)) }

        assertEquals(listOf(null, "cursor-next"), repository.cursors)
        assertEquals("next", viewModel.state.value.swipe.current?.ref?.id)
        assertEquals(0, viewModel.state.value.swipe.index)
    }

    @Test
    fun placeSubmissionOmitsDateAndUnbudgetedPartySize() = runTest {
        val repository = CapturingFeedRepository()
        val viewModel = filteringVm(repository)
        viewModel.onEvent(FeedModeEvent.DateChanged("2026-11-20"))
        viewModel.onEvent(FeedModeEvent.SubmitFilters)

        assertEquals(listOf(FeedFilters(FeedKind.PLACE)), repository.submitted)
    }

    @Test
    fun invalidBudgetAndPartySizeBlockSubmissionWithFieldError() = runTest {
        val repository = CapturingFeedRepository()
        val viewModel = filteringVm(repository)
        viewModel.onEvent(FeedModeEvent.BudgetChanged("-1"))
        viewModel.onEvent(FeedModeEvent.SubmitFilters)
        assertTrue(viewModel.state.value.fieldError?.contains("бюджет") == true)
        assertTrue(repository.submitted.isEmpty())

        viewModel.onEvent(FeedModeEvent.BudgetChanged("500"))
        viewModel.onEvent(FeedModeEvent.PartySizeChanged("0"))
        viewModel.onEvent(FeedModeEvent.SubmitFilters)
        assertTrue(viewModel.state.value.fieldError?.contains("человек") == true)
        assertTrue(repository.submitted.isEmpty())
    }

    @Test
    fun changingFiltersKeepsValuesAndSubmitsUpdatedFreshQuery() = runTest {
        val repository = CapturingFeedRepository()
        val viewModel = filteringVm(repository)
        viewModel.onEvent(FeedModeEvent.KindChanged(FeedKind.EVENT))
        viewModel.onEvent(FeedModeEvent.CategoryChanged("music"))
        viewModel.onEvent(FeedModeEvent.DateChanged("2026-11-20"))
        viewModel.onEvent(FeedModeEvent.BudgetChanged("100"))
        viewModel.onEvent(FeedModeEvent.SubmitFilters)
        viewModel.onEvent(FeedModeEvent.ChangeFilters)

        assertTrue(viewModel.state.value.filterOpen)
        assertEquals("music", viewModel.state.value.categoryId)
        assertEquals("2026-11-20", viewModel.state.value.localDate)
        assertEquals("100", viewModel.state.value.budgetRubles)
        viewModel.onEvent(FeedModeEvent.CategoryChanged("theatre"))
        viewModel.onEvent(FeedModeEvent.SubmitFilters)

        assertEquals(2, repository.submitted.size)
        assertEquals(FeedFilters(FeedKind.EVENT, "theatre", "2026-11-20", 10_000L, 2), repository.submitted.last())
    }
}
